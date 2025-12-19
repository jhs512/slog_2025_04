"use client";

import { useCallback, useEffect, useState } from "react";

import { components } from "@/lib/backend/apiV1/schema";

import { Button } from "@/components/ui/button";

import {
  ChevronLeft,
  ChevronRight,
  Home,
} from "lucide-react";

export default function ClientPage({
  post,
  html,
  css,
}: {
  post: components["schemas"]["PostWithContentDto"];
  html: string;
  css: string;
}) {
  // 해시에서 초기 슬라이드 인덱스 가져오기
  const getInitialSlide = () => {
    if (typeof window !== "undefined") {
      const hash = window.location.hash;
      if (hash) {
        const slideNum = parseInt(hash.substring(1));
        if (!isNaN(slideNum) && slideNum > 0) {
          return slideNum - 1;
        }
      }
    }
    return 0;
  };

  const [currentSlide, setCurrentSlide] = useState(getInitialSlide);
  const [isInitialized, setIsInitialized] = useState(false);

  // HTML에서 슬라이드 개수 추출
  const slideCount = (html.match(/<section/g) || []).length;

  const goToSlide = useCallback(
    (index: number) => {
      if (index >= 0 && index < slideCount) {
        setCurrentSlide(index);
      }
    },
    [slideCount]
  );

  const nextSlide = useCallback(() => {
    goToSlide(currentSlide + 1);
  }, [currentSlide, goToSlide]);

  const prevSlide = useCallback(() => {
    goToSlide(currentSlide - 1);
  }, [currentSlide, goToSlide]);



  // 키보드 네비게이션
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      switch (e.key) {
        case "ArrowRight":
        case " ":
        case "PageDown":
          e.preventDefault();
          nextSlide();
          break;
        case "ArrowLeft":
        case "PageUp":
          e.preventDefault();
          prevSlide();
          break;
        case "Home":
          e.preventDefault();
          goToSlide(0);
          break;
        case "End":
          e.preventDefault();
          goToSlide(slideCount - 1);
          break;
      }
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [nextSlide, prevSlide, goToSlide, slideCount]);

  // 초기 로드 시 해시 확인
  useEffect(() => {
    const initialSlide = getInitialSlide();
    if (initialSlide >= 0 && initialSlide < slideCount) {
      setCurrentSlide(initialSlide);
    }
    setIsInitialized(true);
  }, [slideCount]);

  // 슬라이드 변경 시 해시 업데이트
  useEffect(() => {
    if (isInitialized) {
      const newHash = `#${currentSlide + 1}`;
      if (window.location.hash !== newHash) {
        window.history.replaceState(null, "", newHash);
      }
    }
  }, [currentSlide, isInitialized]);

  // 해시 변경 감지 (뒤로가기/앞으로가기 지원)
  useEffect(() => {
    const handleHashChange = () => {
      const slideNum = getInitialSlide();
      if (slideNum !== currentSlide && slideNum >= 0 && slideNum < slideCount) {
        setCurrentSlide(slideNum);
      }
    };

    window.addEventListener("hashchange", handleHashChange);
    return () => window.removeEventListener("hashchange", handleHashChange);
  }, [currentSlide, slideCount]);

  const handleContainerClick = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      // 대화형 요소(버튼, 링크 등) 클릭 시 무시
      const target = e.target as HTMLElement;
      if (
        target.tagName === "BUTTON" ||
        target.tagName === "A" ||
        target.closest("button") ||
        target.closest("a")
      ) {
        return;
      }

      const { clientX } = e;
      const { innerWidth } = window;

      if (clientX < innerWidth / 2) {
        prevSlide();
      } else {
        nextSlide();
      }
    },
    [nextSlide, prevSlide]
  );

  return (
    <div
      className="fixed inset-0 bg-black flex flex-col"
      onClick={handleContainerClick}
    >
      {/* 컨트롤 바 */}
      <div className="absolute top-0 left-0 right-0 z-10 bg-black/50 text-white p-2 flex items-center justify-between opacity-0 hover:opacity-100 transition-opacity">
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="icon" asChild>
            <a href={`/p/${post.id}`}>
              <Home className="w-5 h-5" />
            </a>
          </Button>
          <span className="text-sm">{post.title}</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-sm">
            {currentSlide + 1} / {slideCount}
          </span>
        </div>
      </div>

      {/* 슬라이드 영역 */}
      <div className="flex-1 flex items-center justify-center overflow-hidden">
        <style dangerouslySetInnerHTML={{ __html: css }} />
        <style dangerouslySetInnerHTML={{
          __html: `
          .marpit {
            display: flex;
            width: 100%;
            height: 100%;
            align-items: center;
            justify-content: center;
          }
          .marpit > svg {
            width: 100%;
            height: 100%;
            box-shadow: 0 5px 15px rgba(0,0,0,0.5);
            display: none;
          }
          .marpit > svg:nth-of-type(${currentSlide + 1}) {
            display: block;
          }
          
          /* Force Light Theme for Code Blocks in Marp */
          .marpit code[class*="language-"],
          .marpit pre[class*="language-"] {
            color: black !important;
            text-shadow: 0 1px white !important;
            background: #f5f2f0 !important;
          }

          .marpit :not(pre) > code[class*="language-"],
          .marpit pre[class*="language-"] {
            background: #f5f2f0 !important;
          }

          .marpit .token.comment,
          .marpit .token.prolog,
          .marpit .token.doctype,
          .marpit .token.cdata {
            color: slategray !important;
          }

          .marpit .token.punctuation {
            color: #999 !important;
          }

          .marpit .token.namespace {
            opacity: 0.7 !important;
          }

          .marpit .token.property,
          .marpit .token.tag,
          .marpit .token.boolean,
          .marpit .token.number,
          .marpit .token.constant,
          .marpit .token.symbol,
          .marpit .token.deleted {
            color: #905 !important;
          }

          .marpit .token.selector,
          .marpit .token.attr-name,
          .marpit .token.string,
          .marpit .token.char,
          .marpit .token.builtin,
          .marpit .token.inserted {
            color: #690 !important;
          }

          .marpit .token.operator,
          .marpit .token.entity,
          .marpit .token.url,
          .marpit .language-css .token.string,
          .marpit .style .token.string {
            color: #9a6e3a !important;
            background: hsla(0, 0%, 100%, 0.5) !important;
          }

          .marpit .token.atrule,
          .marpit .token.attr-value,
          .marpit .token.keyword {
            color: #07a !important;
          }

          .marpit .token.function,
          .marpit .token.class-name {
            color: #dd4a68 !important;
          }

          .marpit .token.regex,
          .marpit .token.important,
          .marpit .token.variable {
            color: #e90 !important;
          }
        ` }} />
        <div
          className="marp-slides"
          style={{
            width: "100%",
            height: "100%",
          }}
          dangerouslySetInnerHTML={{ __html: html }}
        />
      </div>

      {/* 네비게이션 버튼 */}
      <div className="absolute bottom-4 left-0 right-0 flex justify-center gap-4 opacity-0 hover:opacity-100 transition-opacity">
        <Button
          variant="secondary"
          size="icon"
          onClick={prevSlide}
          disabled={currentSlide === 0}
        >
          <ChevronLeft className="w-5 h-5" />
        </Button>
        <Button
          variant="secondary"
          size="icon"
          onClick={nextSlide}
          disabled={currentSlide === slideCount - 1}
        >
          <ChevronRight className="w-5 h-5" />
        </Button>
      </div>

      {/* 진행률 바 */}
      <div className="absolute bottom-0 left-0 right-0 h-1 bg-gray-800 z-50">
        <div
          className="h-full bg-primary transition-all duration-300 ease-out"
          style={{ width: `${((currentSlide + 1) / slideCount) * 100}%` }}
        />
      </div>
    </div>
  );
}
