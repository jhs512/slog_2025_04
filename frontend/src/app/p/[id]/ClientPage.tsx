"use client";

import { Editor } from "@toast-ui/react-editor";
import { use, useEffect, useRef } from "react";

import { useTheme } from "next-themes";

import Image from "next/image";
import Link from "next/link";

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
  post,
  genFiles,
}: {
  post: components["schemas"]["PostWithContentDto"];
  genFiles: components["schemas"]["PostGenFileDto"][];
}) {
  const { resolvedTheme } = useTheme();
  const { loginMember, isAdmin } = use(LoginMemberContext);

  // any 타입을 Editor로 변경
  const toastUiEditorViewerRef = useRef<Editor>(null);

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
