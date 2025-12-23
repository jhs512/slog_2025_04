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
  { params }: { params: Promise<{ id: string; rawId: string }> }
) {
  try {
    const { id, rawId } = await params;
    const postResponse = await getPost(id);

    if (postResponse.error) {
      return new Response(postResponse.error.msg || "Post not found", {
        status: 404,
      });
    }

    const content = postResponse.data.content;
    let title = postResponse.data.title;

    const extractContent = (targetId: string) => {
      // Matches both ppt-id or raw-id
      const regex = new RegExp(
        `<details[^>]*(?:ppt-id|raw-id)=["']${targetId}["'][^>]*>[\\s\\S]*?<summary>\\s*(.*?)\\s*<\\/summary>[\\s\\S]*?<div[^>]*markdown=["']1["'][^>]*>([\\s\\S]*?)<\\/div>`,
        "i"
      );
      return content.match(regex);
    };

    let match = extractContent(rawId);

    // If no match, try replacing hyphens with spaces
    if (!match) {
      match = extractContent(rawId.replace(/-/g, " "));
    }

    if (!match || !match[2]) {
      return new Response(`Raw content not found for ID: ${rawId}`, {
        status: 404,
      });
    }

    if (match[1]) {
      title = match[1].trim();
    }

    let rawContent = match[2].trim();

    // Code block detection
    const codeBlockRegex = /^[\s]*```(\w+)?\s*([\s\S]*?)[\s]*```[\s]*$/;
    const codeBlockMatch = rawContent.match(codeBlockRegex);

    if (codeBlockMatch) {
      const language = codeBlockMatch[1]?.toLowerCase() || "plain";
      const body = codeBlockMatch[2].trim();

      const contentTypeMap: Record<string, string> = {
        json: "application/json",
        xml: "application/xml",
        html: "text/html",
        css: "text/css",
        javascript: "application/javascript",
        js: "application/javascript",
        typescript: "application/x-typescript",
        ts: "application/x-typescript",
        yaml: "application/yaml",
        yml: "application/yaml",
        sql: "application/sql",
        python: "application/x-python",
        py: "application/x-python",
        rust: "text/x-rustsrc",
        rs: "text/x-rustsrc",
        go: "text/x-gosrc",
        text: "text/plain",
        plain: "text/plain",
        markdown: "text/markdown",
        md: "text/markdown",
      };

      const contentType = contentTypeMap[language] || "text/plain";
      const extensionMap: Record<string, string> = {
        json: "json",
        xml: "xml",
        html: "html",
        css: "css",
        javascript: "js",
        js: "js",
        typescript: "ts",
        ts: "ts",
        yaml: "yaml",
        yml: "yaml",
        sql: "sql",
        python: "py",
        py: "py",
        rust: "rs",
        rs: "rs",
        go: "go",
      };
      const extension = extensionMap[language] || "txt";

      const filename = `Doc ${id} - ${title}.${extension}`;

      return new Response(body, {
        headers: {
          "Content-Type": `${contentType}; charset=utf-8`,
          "Content-Disposition": `inline; filename*=UTF-8''${encodeURIComponent(filename)}`,
        },
      });
    }

    // If no code block, return as text/plain (Markdown source)
    const filename = `Doc ${id} - ${title}.md`;
    return new Response(rawContent, {
      headers: {
        "Content-Type": "text/plain; charset=utf-8",
        "Content-Disposition": `inline; filename*=UTF-8''${encodeURIComponent(filename)}`,
      },
    });
  } catch (error: any) {
    console.error("Raw Route Error:", error);
    return new Response(error.message || "Internal Server Error", {
      status: 500,
    });
  }
}
