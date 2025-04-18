import { cookies } from "next/headers";

import client from "@/lib/backend/client";

import ClientPage from "./ClientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<{
    searchKeywordType?: "all" | "title" | "content" | "author";
    searchKeyword?: string;
    pageSize?: number;
    page?: number;
  }>;
}) {
  const {
    searchKeyword = "",
    searchKeywordType = "all",
    pageSize = 30,
    page = 1,
  } = await searchParams;

  const response = await client.GET("/api/v1/posts", {
    params: {
      query: {
        searchKeyword,
        searchKeywordType,
        pageSize,
        page,
      },
    },
    headers: {
      cookie: (await cookies()).toString(),
    },
  });

  const itemPage = response.data!;

  return (
    <>
      <ClientPage
        searchKeyword={searchKeyword}
        searchKeywordType={searchKeywordType}
        page={page}
        pageSize={pageSize}
        itemPage={itemPage}
      />
    </>
  );
}
