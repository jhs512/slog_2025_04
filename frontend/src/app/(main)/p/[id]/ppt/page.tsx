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
  const searchParamsValue = await searchParams;
  const pptId = (searchParamsValue.ppt_id || searchParamsValue.id) as
    | string
    | undefined;

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
      // 1. <details[^>]*ppt-id=["']${targetId}["'][^>]*>: Finds <details> tag with specific ppt-id attribute.
      // 2. <summary>\s*(.*?)\s*<\/summary>: Captures the summary content as Group 1 (Title).
      // 3. [\s\S]*?: Non-greedy match.
      // 4. <div[^>]*markdown=["']1["'][^>]*>: Finds the specific div with markdown="1".
      // 5. ([\s\S]*?): Capturing group for the content inside the div (Group 2).
      // 6. <\/div>: Closing div tag.
      const regex = new RegExp(
        `<details[^>]*ppt-id=["']${targetId}["'][^>]*>[\\s\\S]*?<summary>\\s*(.*?)\\s*<\\/summary>[\\s\\S]*?<div[^>]*markdown=["']1["'][^>]*>([\\s\\S]*?)<\\/div>`,
        "i",
      );
      return content.match(regex);
    };

    let match = extractContent(pptId);

    // If no match, try replacing hyphens with spaces
    if (!match) {
      match = extractContent(pptId.replace(/-/g, " "));
    }

    if (match && match[2]) {
      // Group 1 is title, Group 2 is content
      if (match[1]) {
        postResponse.data.title = match[1].trim();
      }
      content = match[2].trim();
    } else {
      // If valid ID provided but no content found, show error instead of full post
      return (
        <div className="flex-1 flex items-center justify-center text-red-500">
          PPT Content not found for ID: {pptId}
        </div>
      );
    }
  } else {
    // If no PPT ID provided, show instruction or error
    return (
      <div className="flex-1 flex items-center justify-center text-muted-foreground">
        PPT ID is required. (e.g. ?ppt_id=1)
      </div>
    );
  }

  // Marp로 HTML 변환
  const marp = new Marp();
  const { html, css } = marp.render(content);

  return <ClientPage post={postResponse.data} html={html} css={css} />;
}
