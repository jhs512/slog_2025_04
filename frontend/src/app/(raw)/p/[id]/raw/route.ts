import { cookies } from "next/headers";
import client from "@/lib/backend/client";

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

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const postResponse = await getPost(id);

    if (postResponse.error) {
      return new Response(postResponse.error.msg || "Post not found", {
        status: 404,
        headers: {
          "Content-Type": "text/plain; charset=utf-8",
        },
      });
    }

    const post = postResponse.data;
    const filename = `Doc ${id} - ${post.title}.md`;

    return new Response(post.content, {
      headers: {
        "Content-Type": "text/plain; charset=utf-8",
        "Content-Disposition": `inline; filename*=UTF-8''${encodeURIComponent(filename)}`,
      },
    });
  } catch (error: any) {
    console.error("Raw Route Error:", error);
    return new Response(error.message || "Internal Server Error", {
      status: 500,
      headers: {
        "Content-Type": "text/plain; charset=utf-8",
      },
    });
  }
}
