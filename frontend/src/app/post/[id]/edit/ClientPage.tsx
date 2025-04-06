"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import html2canvas from "html2canvas-pro";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import Link from "next/link";
import { useRouter } from "next/navigation";

import client from "@/lib/backend/client";

import { components } from "@/lib/backend/apiV1/schema";
import {
  getThumbnailTextFromContent,
  getUplodableInputAccept,
} from "@/lib/business/utils";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";

const writeFormSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, "제목을 입력해주세요.")
    .min(2, "제목을 2자 이상이여야 합니다.")
    .max(50, "제목은 50자 이하여야 합니다."),
  content: z
    .string()
    .trim()
    .min(1, "내용을 입력해주세요.")
    .min(2, "내용은 2자 이상이어야 합니다.")
    .max(10_000_000, "내용은 1,000만자 이하여야 합니다."),
  published: z.boolean(),
  listed: z.boolean(),
  attachment_0: z.array(z.instanceof(File)).optional(),
});

type WriteFormInputs = z.infer<typeof writeFormSchema>;

export default function ClientPage({
  post: _post,
}: {
  post: components["schemas"]["PostWithContentDto"];
}) {
  const router = useRouter();

  const [attachmentInputKey, setAttachmentInputKey] = useState(0);
  const [post, setPost] = useState(_post);
  const originThumbnailText = getThumbnailTextFromContent(post.content);

  useEffect(() => {
    setPost(_post);
  }, [_post]);

  useEffect(() => {
    const needToRefresh = window.sessionStorage.getItem("needToRefresh");

    if (needToRefresh === "true") {
      window.sessionStorage.removeItem("needToRefresh");
      router.refresh();
    }
  }, [router]);

  const form = useForm<WriteFormInputs>({
    resolver: zodResolver(writeFormSchema),
    defaultValues: {
      title: post.title,
      content: post.content,
      published: post.published,
      listed: post.listed,
    },
  });

  useEffect(() => {
    form.reset({
      title: post.title,
      content: post.content,
      published: post.published,
      listed: post.listed,
    });
  }, [form, post]);

  const handleThumbnailUpload = async (content: string, postId: number) => {
    const tempDiv = document.createElement("div");
    Object.assign(tempDiv.style, {
      width: "1200px",
      height: "1200px",
      position: "absolute",
      left: "-9999px",
      whiteSpace: "pre-wrap",
      wordBreak: "break-word",
      fontSize: "60px",
      fontWeight: "500",
      fontFamily: "Pretendard",
      padding: "0 10px",
      display: "flex",
      flexWrap: "wrap",
      alignItems: "center",
      justifyContent: "center",
      color: "black",
    });

    const thumbnailText = getThumbnailTextFromContent(content);

    tempDiv.innerText = thumbnailText;
    document.body.appendChild(tempDiv);

    try {
      const canvas = await html2canvas(tempDiv, {
        width: 1200,
        height: 1200,
        backgroundColor: null,
      });

      const blob = await new Promise<Blob>((resolve) => {
        canvas.toBlob((blob) => resolve(blob!), "image/png", 1.0);
      });

      const file = new File([blob], `${postId}-thumbnail.png`, {
        type: "image/png",
        lastModified: Date.now(),
      });

      const formData = new FormData();
      formData.append("file", file);

      return await client.PUT(
        "/api/v1/posts/{postId}/genFiles/{typeCode}/{fileNo}",
        {
          params: {
            path: {
              postId,
              typeCode: "thumbnail",
              fileNo: 1,
            },
            query: {
              metaStr: "darkInvertible=1",
            },
          },
          body: formData as any, // eslint-disable-line @typescript-eslint/no-explicit-any
        },
      );
    } finally {
      document.body.removeChild(tempDiv);
    }
  };

  const handleAttachmentUpload = async (files: File[], postId: number) => {
    const formData = new FormData();
    files.forEach((file) => formData.append("files", file));

    return await client.POST("/api/v1/posts/{postId}/genFiles/{typeCode}", {
      params: {
        path: {
          postId,
          typeCode: "attachment",
        },
      },
      body: formData as any, // eslint-disable-line @typescript-eslint/no-explicit-any
    });
  };

  const onSubmit = async (data: WriteFormInputs) => {
    // 데이터가 변경되었는지 확인
    const isPostDataChanged =
      data.title !== post.title ||
      data.content !== post.content ||
      data.published !== post.published ||
      data.listed !== post.listed;

    const isThumbnailTextChanged =
      originThumbnailText !== getThumbnailTextFromContent(data.content);

    if (isPostDataChanged) {
      const response = await client.PUT("/api/v1/posts/{id}", {
        params: {
          path: {
            id: post.id,
          },
        },
        body: {
          title: data.title,
          content: data.content,
          published: data.published,
          listed: data.listed,
        },
      });

      if (response.error) {
        toast.error(response.error.msg);
        return;
      }

      setPost({
        ...post,
        title: data.title,
        content: data.content,
        published: data.published,
        listed: data.listed,
      });

      toast(response.data.msg, {
        action: {
          label: "글 보기",
          onClick: () => router.push(`/post/${post.id}`),
        },
      });
    }

    if (isThumbnailTextChanged) {
      const thumbnailResponse = await handleThumbnailUpload(
        data.content,
        post.id,
      );

      if (thumbnailResponse.error) {
        toast.error(thumbnailResponse.error.msg);

        return;
      }
    }

    if (data.attachment_0) {
      const uploadResponse = await handleAttachmentUpload(
        data.attachment_0,
        post.id,
      );

      if (uploadResponse.error) {
        toast.error(uploadResponse.error.msg);
        return;
      }

      // 파일 업로드 성공 후 input 초기화
      form.reset({
        ...form.getValues(),
        attachment_0: undefined,
      });

      setAttachmentInputKey((prev) => prev + 1);

      toast(uploadResponse.data.msg);
    }
  };

  return (
    <div className="container mx-auto px-4">
      <h1 className="text-2xl font-bold my-4 flex items-center gap-2 justify-center">
        <div className="flex-1" />
        <span>{post.id}번 글 수정</span>
        <div className="flex-1">
          <Button variant="outline" asChild className="float-right">
            <Link href={`/post/${post.id}/edit/monaco`}>VS CODE로 편집</Link>
          </Button>
        </div>
      </h1>

      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex flex-col gap-4"
        >
          <FormField
            control={form.control}
            name="title"
            render={({ field }) => (
              <FormItem>
                <FormLabel>제목</FormLabel>
                <FormControl>
                  <Input
                    {...field}
                    type="text"
                    placeholder={post.title}
                    autoComplete="off"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <div className="flex gap-4">
            <label className="flex items-center space-x-2 cursor-pointer">
              <Checkbox
                checked={form.watch("published")}
                onCheckedChange={(checked) =>
                  form.setValue("published", checked === true)
                }
              />
              <span className="text-sm font-medium leading-none">공개</span>
            </label>
            <label className="flex items-center space-x-2 cursor-pointer">
              <Checkbox
                checked={form.watch("listed")}
                onCheckedChange={(checked) =>
                  form.setValue("listed", checked === true)
                }
              />
              <span className="text-sm font-medium leading-none">검색</span>
            </label>
            <Badge variant="outline">작성자 : {post.authorName}</Badge>
          </div>
          <FormField
            control={form.control}
            name="attachment_0"
            render={({ field: { onChange, ...field } }) => (
              <FormItem>
                <FormLabel>
                  첨부파일(드래그 앤 드롭 가능, 다중 업로드 최대 5개 가능)
                </FormLabel>
                <FormControl>
                  <Input
                    key={attachmentInputKey}
                    type="file"
                    multiple
                    accept={getUplodableInputAccept()}
                    onChange={(e) => {
                      const files = Array.from(e.target.files || []);
                      onChange(files);
                    }}
                    {...field}
                    value={undefined}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <Button variant="outline" asChild>
            <Link href={`/post/${post.id}/genFile/listForEdit`}>
              기존 첨부파일 변경/삭제
            </Link>
          </Button>
          <FormField
            control={form.control}
            name="content"
            render={({ field }) => (
              <FormItem>
                <FormLabel>내용</FormLabel>
                <FormControl>
                  <Textarea
                    {...field}
                    autoFocus
                    className="h-[calc(100dvh-520px)] min-h-[300px]"
                    placeholder={post.content}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <Button
            type="submit"
            disabled={form.formState.isSubmitting}
            className="mt-2"
          >
            {form.formState.isSubmitting ? "수정 중..." : "수정"}
          </Button>
        </form>
      </Form>
    </div>
  );
}
