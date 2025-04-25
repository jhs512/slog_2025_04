"use client";

import { components } from "@/lib/backend/apiV1/schema";

export default function ClientPage({
  post: _post,
}: {
  post: components["schemas"]["PostWithContentDto"];
}) {
  return (
    <div className="whitespace-pre-wrap">
      <div className="text-sm text-muted-foreground">{_post.content}</div>
    </div>
  );
}
