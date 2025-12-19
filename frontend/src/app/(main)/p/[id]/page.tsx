import { Suspense } from "react";

import type { Metadata } from "next";

import { cookies } from "next/headers";

import client from "@/lib/backend/client";

import {
  getSummaryFromContent,
  processMarkdownContent,
  stripMarkdown,
} from "@/lib/business/utils";

import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

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

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }>;
}): Promise<Metadata> {
  const { id } = await params;
  const postResponse = await getPost(id);

  if (postResponse.error) {
    return {
      title: postResponse.error.msg,
      description: postResponse.error.msg,
    };
  }

  const post = postResponse.data;

  const summary = getSummaryFromContent(post.content);

  return {
    title: post.title,
    description: summary || stripMarkdown(post.content),
  };
}

function PostSkeleton() {
  return (
    <main className="container mt-2 mx-auto px-2">
      <Card>
        <CardHeader>
          <div className="mb-4 flex items-center gap-2">
            <Skeleton className="h-6 w-12" />
            <Skeleton className="h-6 w-64" />
          </div>
          <div className="flex items-center gap-4 flex-wrap">
            <div className="flex items-center gap-4 w-full sm:w-auto">
              <Skeleton className="w-[40px] h-[40px] rounded-full" />
              <div className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-4 w-32" />
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-3/4" />
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-5/6" />
          <Skeleton className="h-32 w-full" />
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-2/3" />
        </CardContent>
        <CardFooter>
          <div className="space-y-2">
            <Skeleton className="h-4 w-48" />
          </div>
        </CardFooter>
      </Card>
    </main>
  );
}

async function PostContent({ id }: { id: string }) {
  const postResponse = await getPost(id);

  if (postResponse.error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        {postResponse.error.msg}
      </div>
    );
  }

  const post = postResponse.data;

  // PPT Details 내용을 링크로 변환
  post.content = processMarkdownContent(post.content, post.id);

  const genFilesResponse = await client.GET("/api/v1/posts/{postId}/genFiles", {
    params: { path: { postId: post.id } },
    headers: {
      cookie: (await cookies()).toString(),
    },
  });

  if (genFilesResponse.error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        {genFilesResponse.error.msg}
      </div>
    );
  }

  const genFiles = genFilesResponse.data;

  return <ClientPage post={post} genFiles={genFiles} />;
}

export default async function Page({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  return (
    <Suspense fallback={<PostSkeleton />}>
      <PostContent id={id} />
    </Suspense>
  );
}
