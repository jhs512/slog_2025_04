"use client";

import { toast } from "sonner";

import { useTheme } from "next-themes";

import client from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";
import MonacoEditor from "@/lib/business/components/MonacoEditor";

interface Config {
  title?: string;
  published?: boolean;
  listed?: boolean;
  [key: string]: string | boolean | undefined;
}

function parseConfig(content: string): {
  title: string;
  published: boolean;
  listed: boolean;
  content: string;
} {
  // config 섹션이 있는지 확인
  if (content.startsWith("$$config")) {
    const configEndIndex = content.indexOf("$$", 2);

    if (configEndIndex === -1) {
      return {
        title: "",
        published: false,
        listed: false,
        content: content.trim(),
      };
    }

    const configSection = content.substring(8, configEndIndex);
    const mainContent = content.substring(configEndIndex + 4);

    // config 파싱
    const configLines = configSection.split("\n");
    const config: Config = {};

    configLines.forEach((line) => {
      const [key, value] = line.split(": ").map((s) => s.trim());
      if (key === "published" || key === "listed") {
        config[key] = value === "true";
      } else {
        config[key] = value;
      }
    });

    return {
      title: config.title?.trim() || "",
      published: config.published || false,
      listed: config.listed || false,
      content: mainContent.trim(),
    };
  }

  // config 섹션이 없는 경우
  return {
    title: "",
    published: false,
    listed: false,
    content: content.trim(),
  };
}

export default function ClientPage({
  post,
}: {
  post: components["schemas"]["PostWithContentDto"];
}) {
  const { resolvedTheme } = useTheme();
  const savePost = async (value: string) => {
    try {
      const { title, published, listed, content } = parseConfig(value.trim());

      const response = await client.PUT("/api/v1/posts/{id}", {
        params: {
          path: {
            id: post.id,
          },
        },
        body: {
          title: title,
          content: content,
          published: published,
          listed: listed,
        },
      });

      if (response.error) {
        toast.error(response.error.msg);

        return;
      }

      if (response.data) {
        toast(response.data.msg);

        sessionStorage.setItem("needToRefresh", "true");
      }
    } catch {
      toast.error("저장 실패");
    }
  };

  return (
    <div className="flex-1 flex">
      <MonacoEditor
        theme={resolvedTheme as "light" | "dark"}
        initialValue={`$$config
title: ${post.title}
published: ${post.published}
listed: ${post.listed}
$$

${post.content || ""}`.trim()}
        onSave={savePost}
        className="flex-1 border"
      />
    </div>
  );
}
