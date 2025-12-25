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
import {
  getDateHr,
  getFileSizeHr,
  processMarkdownContent,
} from "@/lib/business/utils";

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

const HASH_SCROLL_DURATION = 15000; // 해시 스크롤 추적 지속 시간 (15초)
const HASH_SCROLL_DEBOUNCE = 100; // 레이아웃 변화 후 스크롤까지 대기 시간

export default function ClientPage({
  post: initialPost,
  genFiles,
}: {
  post: components["schemas"]["PostWithContentDto"];
  genFiles: components["schemas"]["PostGenFileDto"][];
}) {
  const { resolvedTheme } = useTheme();
  const { loginMember, isAdmin } = use(LoginMemberContext);

  const [post, setPost] = useState({
    ...initialPost,
    content: processMarkdownContent(initialPost.content, initialPost.id),
  });

  const toastUiEditorViewerRef = useRef<Editor>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  const lastModifyDateAfterRef = useRef(post.modifyDate);

  const POLLING_INTERVAL = 10000;

  // 해시 스크롤 - 레이아웃 변화 추적
  useEffect(() => {
    const hash = decodeURIComponent(window.location.hash.substring(1));
    if (!hash) return;

    let debounceTimer: NodeJS.Timeout;
    let isActive = true;
    let lastScrollY = 0;
    let targetElement: HTMLElement | null = null;

    const scrollToTarget = () => {
      if (!isActive || !targetElement) return;

      const currentY = targetElement.getBoundingClientRect().top + window.scrollY;

      // 위치가 변했을 때만 스크롤 (불필요한 스크롤 방지)
      if (Math.abs(currentY - lastScrollY) > 10) {
        lastScrollY = currentY;
        targetElement.scrollIntoView({ behavior: "smooth" });
      }
    };

    const debouncedScroll = () => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(scrollToTarget, HASH_SCROLL_DEBOUNCE);
    };

    // 타겟 엘리먼트 찾기 (최대 5초간 시도)
    const findTargetElement = () => {
      return new Promise<HTMLElement | null>((resolve) => {
        const element = document.getElementById(hash);
        if (element) {
          resolve(element);
          return;
        }

        let attempts = 0;
        const maxAttempts = 10;
        const interval = setInterval(() => {
          const el = document.getElementById(hash);
          if (el || attempts >= maxAttempts) {
            clearInterval(interval);
            resolve(el);
          }
          attempts++;
        }, 500);
      });
    };

    // ResizeObserver - 콘텐츠 영역 크기 변화 감지
    const resizeObserver = new ResizeObserver(() => {
      if (isActive && targetElement) {
        debouncedScroll();
      }
    });

    // MutationObserver - DOM 변화 감지 (이미지, iframe 추가 등)
    const mutationObserver = new MutationObserver((mutations) => {
      if (!isActive || !targetElement) return;

      const hasLayoutChange = mutations.some((mutation) => {
        // 노드 추가/삭제
        if (mutation.addedNodes.length > 0 || mutation.removedNodes.length > 0) {
          return true;
        }
        // 속성 변화 (이미지 src, width, height 등)
        if (mutation.type === "attributes") {
          const attr = mutation.attributeName;
          return ["src", "width", "height", "style"].includes(attr || "");
        }
        return false;
      });

      if (hasLayoutChange) {
        debouncedScroll();
      }
    });

    // 이미지 로드 완료 감지
    const handleImageLoad = () => {
      if (isActive && targetElement) {
        debouncedScroll();
      }
    };

    const setupObservers = async () => {
      targetElement = await findTargetElement();
      if (!targetElement || !isActive) return;

      // 초기 스크롤
      scrollToTarget();

      const contentEl = contentRef.current;
      if (contentEl) {
        // ResizeObserver로 콘텐츠 영역 감시
        resizeObserver.observe(contentEl);

        // MutationObserver로 DOM 변화 감시
        mutationObserver.observe(contentEl, {
          childList: true,
          subtree: true,
          attributes: true,
          attributeFilter: ["src", "width", "height", "style"],
        });

        // 모든 이미지에 load 이벤트 리스너 추가
        const images = contentEl.querySelectorAll("img");
        images.forEach((img) => {
          if (!img.complete) {
            img.addEventListener("load", handleImageLoad);
          }
        });

        // 동적으로 추가되는 이미지 처리
        const imageObserver = new MutationObserver((mutations) => {
          mutations.forEach((mutation) => {
            mutation.addedNodes.forEach((node) => {
              if (node instanceof HTMLImageElement) {
                if (!node.complete) {
                  node.addEventListener("load", handleImageLoad);
                }
              } else if (node instanceof HTMLElement) {
                const imgs = node.querySelectorAll?.("img");
                imgs?.forEach((img) => {
                  if (!img.complete) {
                    img.addEventListener("load", handleImageLoad);
                  }
                });
              }
            });
          });
        });

        imageObserver.observe(contentEl, {
          childList: true,
          subtree: true,
        });

        // 15초 후 모든 관찰자 해제
        setTimeout(() => {
          isActive = false;
          resizeObserver.disconnect();
          mutationObserver.disconnect();
          imageObserver.disconnect();
          clearTimeout(debounceTimer);
        }, HASH_SCROLL_DURATION);
      }
    };

    setupObservers();

    return () => {
      isActive = false;
      clearTimeout(debounceTimer);
      resizeObserver.disconnect();
      mutationObserver.disconnect();
    };
  }, [post.id]);

  useEffect(() => {
    let timeoutId: NodeJS.Timeout;
    let isComponentMounted = true;
    let isPollingInProgress = false;

    const checkForUpdates = async () => {
      // 컴포넌트가 언마운트되었거나 문서가 숨겨져 있으면 폴링 중지
      if (!isComponentMounted || document.hidden) {
        return;
      }

      // 이미 폴링 중이면 중복 실행 방지
      if (isPollingInProgress) {
        return;
      }

      isPollingInProgress = true;

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
              .setMarkdown(
                processMarkdownContent(res.data.content, res.data.id),
              );
          }

          setPost((prev) => ({
            ...prev,
            title: res.data.title,
            modifyDate: res.data.modifyDate,
            content: processMarkdownContent(res.data.content, res.data.id),
          }));

          toast("문서 업데이트", {
            description: "새로운 내용으로 업데이트되었습니다.",
          });
        }
      } catch (error) {
        console.error("문서 업데이트 중 오류 발생:", error);
      } finally {
        isPollingInProgress = false;
      }

      // 컴포넌트가 마운트된 상태일 때만 다음 폴링 예약
      if (isComponentMounted) {
        scheduleNextPoll();
      }
    };

    const scheduleNextPoll = () => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(checkForUpdates, POLLING_INTERVAL);
    };

    const handleVisibilityChange = () => {
      if (!document.hidden && isComponentMounted) {
        // 탭이 다시 보일 때 즉시 업데이트 확인
        clearTimeout(timeoutId);
        checkForUpdates();
      }
    };

    const handleFocus = () => {
      // 다른 앱에서 브라우저로 돌아왔을 때
      if (isComponentMounted && !document.hidden) {
        clearTimeout(timeoutId);
        checkForUpdates();
      }
    };

    const handleOnline = () => {
      // 네트워크가 다시 연결되면 즉시 업데이트 확인
      if (isComponentMounted && !document.hidden) {
        clearTimeout(timeoutId);
        checkForUpdates();
      }
    };

    // 초기 폴링 시작
    scheduleNextPoll();

    document.addEventListener("visibilitychange", handleVisibilityChange);
    window.addEventListener("focus", handleFocus);
    window.addEventListener("online", handleOnline);

    // 클린업 함수
    return () => {
      isComponentMounted = false;
      clearTimeout(timeoutId);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
      window.removeEventListener("focus", handleFocus);
      window.removeEventListener("online", handleOnline);
    };
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
        <CardContent ref={contentRef}>
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
