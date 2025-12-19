import client from "@/lib/backend/client";

import RequireAuthenticated from "@/lib/auth/components/RequireAuthenticated";

import ClientPage from "./ClientPage";

export default async function Page({
  params,
}: {
  params: Promise<{ id: string; genFileId: string }>;
}) {
  const { id, genFileId } = await params;

  const genFileResponse = await client.GET(
    "/api/v1/posts/{postId}/genFiles/{id}",
    {
      params: {
        path: {
          postId: Number(id),
          id: Number(genFileId),
        },
      },
    },
  );

  if (genFileResponse.error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        {genFileResponse.error.msg}
      </div>
    );
  }

  return (
    <RequireAuthenticated>
      <ClientPage id={id} genFile={genFileResponse.data} />
    </RequireAuthenticated>
  );
}
