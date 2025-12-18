import { cookies } from "next/headers";

import Marp from "@marp-team/marp-core";

import client from "@/lib/backend/client";

import ClientPage from "./ClientPage";

async function getPost(id: string) {
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

export default async function Page({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const { id } = await params;
  const { id: pptId } = await searchParams; // Query param id
  const postResponse = await getPost(id);

  if (postResponse.error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        {postResponse.error.msg}
      </div>
    );
  }

  let content = postResponse.data.content;

  if (typeof pptId === "string" && pptId) {
    // Helper to extract content by ID
    const extractContent = (targetId: string) => {
      // Regex explanation:
      // 1. <details[^>]*id="${targetId}"[^>]*>: Finds <details> tag with specific id attribute.
      // 2. [\s\S]*?: Non-greedy match for any content until the inner div.
      // 3. <div[^>]*markdown="1"[^>]*>: Finds the specific div with markdown="1".
      // 4. ([\s\S]*?): Capturing group for the content inside the div.
      // 5. <\/div>: Closing div tag.
      const regex = new RegExp(
        `<details[^>]*id="${targetId}"[^>]*>[\\s\\S]*?<div[^>]*markdown="1"[^>]*>([\\s\\S]*?)<\\/div>`,
        "i"
      );
      return content.match(regex);
    };

    let match = extractContent(pptId);

    // If no match, try replacing hyphens with spaces (common URL slug pattern)
    if (!match) {
      match = extractContent(pptId.replace(/-/g, " "));
    }

    if (match && match[1]) {
      content = match[1].trim();
    }
  }

  // Marp로 HTML 변환
  const marp = new Marp();
  const { html, css } = marp.render(content);

  return <ClientPage post={postResponse.data} html={html} css={css} />;
}
