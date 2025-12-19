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

export default async function Page({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const postResponse = await getPost(id);

  if (postResponse.error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        {postResponse.error.msg}
      </div>
    );
  }

  const post = postResponse.data;
  //  raw/ 안에서는 그냥 아무것도 안나오고 하얀화면에 코드만 나오도록 소스코드 그대로 보여줬으면 좋겠어 엔터나 이런것들도 마치 깃허브의 raw 처럼
  return (
    <pre
      style={{
        whiteSpace: "pre-wrap",
        wordBreak: "break-word",
        fontFamily: "monospace",
        padding: "1rem",
        margin: 0,
        backgroundColor: "white",
        color: "black",
        minHeight: "100vh",
        fontSize: "14px",
        lineHeight: "1.5",
      }}
    >
      {post.content}
    </pre>
  );
}
