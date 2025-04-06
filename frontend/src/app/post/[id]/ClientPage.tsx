"use client";

import { Editor } from "@toast-ui/react-editor";
import { use, useEffect, useRef, useState } from "react";
import { toast } from "sonner";

import { useTheme } from "next-themes";

import Image from "next/image";
import Link from "next/link";

import client from "@/lib/backend/client";

import { components } from "@/lib/backend/apiV1/schema";
import ToastUIEditorViewer from "@/lib/business/components/ToastUIEditorViewer";
import { getDateHr, getFileSizeHr } from "@/lib/business/utils";

import { LoginMemberContext } from "@/stores/auth/loginMember";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

import { Download, Eye, ListX, Lock } from "lucide-react";

export default function ClientPage({
  post: initialPost,
  genFiles,
}: {
  post: components["schemas"]["PostWithContentDto"];
  genFiles: components["schemas"]["PostGenFileDto"][];
}) {
  const { resolvedTheme } = useTheme();
  const { loginMember, isAdmin } = use(LoginMemberContext);

  const [post, setPost] = useState(initialPost);

  // any 타입을 Editor로 변경
  const toastUiEditorViewerRef = useRef<Editor>(null);
  const lastModifyDateAfterRef = useRef(post.modifyDate);

  const POLLING_INTERVAL = 10000; // 폴링 간격을 상수로 정의 (밀리초)

  useEffect(() => {
    const checkAndScrollToElement = () => {
      const hash = decodeURIComponent(window.location.hash.substring(1));
      const element = document.getElementById(hash);
      if (element) {
        element.scrollIntoView({ behavior: "smooth" });
        return true; // 엘리먼트를 찾았음
      }
      return false; // 엘리먼트를 찾지 못함
    };

    let attempts = 0;

    const maxAttempts = 20; // 10초 / 0.5초 = 20회

    const interval = setInterval(() => {
      if (checkAndScrollToElement() || attempts >= maxAttempts) {
        clearInterval(interval); // 엘리먼트를 찾았거나 최대 시도 횟수에 도달하면 중단
      }
      attempts++;
    }, 500);

    return () => clearInterval(interval);
  }, [post.id]);

  useEffect(() => {
    const hash = decodeURIComponent(window.location.hash.substring(1));

    if (hash !== "f") return;

    let timeoutId: NodeJS.Timeout;
    let isComponentMounted = true;

    const checkForUpdates = async () => {
      // 컴포넌트가 언마운트되었거나 문서가 숨겨져 있으면 폴링 중지
      if (!isComponentMounted || document.hidden) {
        return;
      }

      try {
        const res = await client.GET("/api/v1/posts/{id}", {
          params: {
            path: {
              id: post.id,
            },
            query: {
              lastModifyDateAfter: lastModifyDateAfterRef.current,
            },
          },
        });

        // 컴포넌트가 여전히 마운트된 상태인지 확인
        if (!isComponentMounted) return;

        if (res.response.status === 200 && res.data) {
          lastModifyDateAfterRef.current = res.data.modifyDate;

          if (toastUiEditorViewerRef.current?.getInstance) {
            toastUiEditorViewerRef.current
              .getInstance()
              .setMarkdown(res.data.content);
          }

          setPost((prev) => ({
            ...prev,
            title: res.data.title,
            modifyDate: res.data.modifyDate,
            content: res.data.content,
          }));

          toast("문서 업데이트", {
            description: "새로운 내용으로 업데이트되었습니다.",
          });
        }
      } catch (error) {
        // 에러 처리
        console.error("문서 업데이트 중 오류 발생:", error);
      }

      // 컴포넌트가 마운트된 상태일 때만 다음 폴링 예약
      if (isComponentMounted) {
        timeoutId = setTimeout(checkForUpdates, POLLING_INTERVAL);
      }
    };

    const handleVisibilityChange = () => {
      if (!document.hidden && isComponentMounted) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(checkForUpdates, POLLING_INTERVAL);
      }
    };

    // 초기 폴링 시작
    timeoutId = setTimeout(checkForUpdates, POLLING_INTERVAL);

    document.addEventListener("visibilitychange", handleVisibilityChange);

    // 클린업 함수
    return () => {
      isComponentMounted = false; // 컴포넌트 언마운트 표시
      clearTimeout(timeoutId);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [post.id]); // toast 의존성 추가

  return (
    <main className="container mt-2 mx-auto px-2">
      <Card>
        <CardHeader>
          <CardTitle className="mb-4 flex items-center gap-2">
            <Badge variant="outline">{post.id}</Badge>
            <div>{post.title}</div>
            {!post.published && <Lock className="w-4 h-4 flex-shrink-0" />}
            {!post.listed && <ListX className="w-4 h-4 flex-shrink-0" />}
          </CardTitle>
          <CardDescription className="sr-only">게시물 상세내용</CardDescription>
          <div className="flex items-center gap-4 flex-wrap">
            <div className="flex items-center gap-4 w-full sm:w-auto">
              <Image
                src={post.authorProfileImgUrl}
                alt={post.authorName}
                width={40}
                height={40}
                quality={100}
                className="w-[40px] h-[40px] object-cover rounded-full ring-2 ring-primary/10"
              />
              <div>
                <div className="text-sm font-medium text-foreground">
                  {post.authorName}
                </div>
                <div className="text-sm text-muted-foreground">
                  {getDateHr(post.createDate)}
                </div>
              </div>
            </div>
            <div className="flex-grow"></div>
            <div className="flex items-center gap-2">
              {loginMember.id === post.authorId && (
                <Button asChild variant="outline">
                  <Link href={`/post/${post.id}/edit`}>수정</Link>
                </Button>
              )}
              {(isAdmin || loginMember.id === post.authorId) && (
                <Button asChild variant="outline">
                  <Link href={`/post/${post.id}/delete`}>삭제</Link>
                </Button>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <ToastUIEditorViewer
            key={resolvedTheme}
            initialValue={post.content}
            theme={resolvedTheme as "dark" | "light"}
            ref={toastUiEditorViewerRef}
          />

          {post.createDate != post.modifyDate && (
            <div className="mt-4 text-sm text-muted-foreground">
              수정 : {getDateHr(post.modifyDate)}
            </div>
          )}
        </CardContent>
        <CardFooter>
          <div className="grid gap-4">
            {genFiles
              .filter((genFile) => genFile.typeCode === "attachment")
              .map((genFile) => (
                <div key={genFile.id} className="grid">
                  <Button variant="link" asChild className="justify-start">
                    <a
                      href={genFile.downloadUrl}
                      className="flex items-center gap-2"
                    >
                      <Download />

                      <span>
                        {genFile.originalFileName}
                        <br />({getFileSizeHr(genFile.fileSize)}) 다운로드
                      </span>
                    </a>
                  </Button>

                  <Button variant="link" className="justify-start" asChild>
                    <Link
                      href={`/post/${post.id}/genFile/${genFile.id}/preview`}
                    >
                      <Eye />
                      <span>미리보기</span>
                    </Link>
                  </Button>
                </div>
              ))}
          </div>
        </CardFooter>
      </Card>
    </main>
  );
}
