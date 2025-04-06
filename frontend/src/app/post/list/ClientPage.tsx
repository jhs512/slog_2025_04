"use client";

import type { components } from "@/lib/backend/apiV1/schema";
import PostList from "@/lib/business/components/PostList";

export default function ClientPage({
  searchKeyword,
  searchKeywordType,
  pageSize,
  itemPage,
}: {
  searchKeyword: string;
  searchKeywordType: "all" | "title" | "content" | "author";
  page: number;
  pageSize: number;
  itemPage: components["schemas"]["PageDtoPostDto"];
}) {
  return (
    <>
      <div className="container mx-auto px-4">
        <h1 className="text-2xl font-bold text-center my-4">공개글</h1>

        <PostList
          searchKeyword={searchKeyword}
          searchKeywordType={searchKeywordType}
          pageSize={pageSize}
          itemPage={itemPage}
        />
      </div>
    </>
  );
}
