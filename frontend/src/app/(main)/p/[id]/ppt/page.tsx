import { cookies } from "next/headers";

import Marp from "@marp-team/marp-core";

import client from "@/lib/backend/client";

import ClientPage from "./ClientPage";

async function getPost(id: string) {
  // Walkthrough - UI Isolation and PPT Link Improvements
  //
  // This walkthrough covers the refactoring for the raw content view and the improvements made to PPT navigation.
  //
  // #### 3. Raw Route Group `(raw)`
  // - Created `src/app/(raw)/p/[id]/raw/page.tsx`.
  // - This page inherits only the minimal root layout, resulting in a pure "source code" view.
  //
  // ---
  //
  // ### PPT URL and Hash Improvements
  //
  // I updated the PPT viewing logic to provide a cleaner URL and more reliable navigation.
  //
  // #### 1. Query Parameter Rename (`ppt_id` → `id`)
  // Updated `src/app/(main)/p/[id]/ppt/page.tsx` to prioritize the `id` query parameter for selecting specific PPT content. Error messages now properly suggest `?id=1`.
  //
  // #### 2. Enhanced Markdown Link Transformation
  // Improved the `slog-link-to` regex in `src/lib/business/markdownUtils.ts`:
  // - Supports both numeric IDs and slugs (e.g., `ppt-my-slide-id`).
  // - Automatically transforms links to use the cleaner `?id=` parameter.
  // - **Strict Hash Preservation**: Ensures that hashes like `#3` are correctly passed through to the final URL.
  //
  // #### 3. Reliable Slide Initialization
  // Refined `src/app/(main)/p/[id]/ppt/ClientPage.tsx` to initialize the slide state directly from the URL hash on mounting. This ensures the correct slide is displayed immediately without flickering or jumping.
  const res = await client.GET("/api/v1/posts/{id}", {
    params: {
      path: {
        id: parseInt(id),
      },
    },
    headers: {
      cookie: (await cookies()).toString(),
    },
  });

  return res;
}

export async function generateMetadata({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const { id } = await params;
  const searchParamsValue = await searchParams;
  const pptId = (searchParamsValue.id || searchParamsValue.ppt_id) as
    | string
    | undefined;

  const postResponse = await getPost(id);

  if (postResponse.error) {
    return {
      title: "Error - PPT",
    };
  }

  let title = postResponse.data.title;

  if (typeof pptId === "string" && pptId) {
    const regex = new RegExp(
      `<details[^>]*ppt-id=["']${pptId}["'][^>]*>[\\s\\S]*?<summary>\\s*(.*?)\\s*<\\/summary>`,
      "i"
    );
    const match = postResponse.data.content.match(regex);
    if (match && match[1]) {
      title = match[1].trim();
    }
  }

  return {
    title: `Doc ${id} - ${title}`,
  };
}

export default async function Page({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  return (
    <div className="flex-1 flex items-center justify-center text-muted-foreground p-10 text-center">
      PPT ID가 필요합니다. <br />
      주소 형식: /p/{id}/ppt/[PPT_ID]
    </div>
  );
}
