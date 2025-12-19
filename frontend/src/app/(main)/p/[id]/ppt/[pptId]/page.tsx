import { cookies } from "next/headers";
import Marp from "@marp-team/marp-core";
import client from "@/lib/backend/client";
import ClientPage from "../ClientPage";

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

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string; pptId: string }>;
}) {
  const { id, pptId } = await params;

  const postResponse = await getPost(id);

  if (postResponse.error) {
    return {
      title: "Error - PPT",
    };
  }

  let title = postResponse.data.title;

  if (pptId) {
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
  params: Promise<{ id: string; pptId: string }>;
}) {
  const { id, pptId } = await params;

  const postResponse = await getPost(id);

  if (postResponse.error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        {postResponse.error.msg}
      </div>
    );
  }

  let content = postResponse.data.content;

  if (pptId) {
    // Helper to extract content by ID
    const extractContent = (targetId: string) => {
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
  }

  // Marp로 HTML 변환
  const marp = new Marp();
  const { html, css } = marp.render(content);

  return <ClientPage post={postResponse.data} html={html} css={css} />;
}
