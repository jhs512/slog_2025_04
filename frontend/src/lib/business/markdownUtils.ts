export function processMarkdownContent(
  content: string,
  currentPostId: string | number
): string {
  let processedContent = content;

  // 1. [text](surl:ppt/ID) -> [text](/p/{currentPostId}/ppt/ID)
  // Support optional hash: surl:ppt/ID#HASH
  processedContent = processedContent.replace(
    /\[([^\]]+)\]\(surl:ppt\/([^)#\s]+)(?:#([^)]+))?\)/g,
    (_, text, id, hash) => {
      const hashPart = hash ? `#${hash}` : "";
      return `[${text}](/p/${currentPostId}/ppt/${id}${hashPart})`;
    }
  );

  // 2. [text](surl:POSTID/ppt/ID) -> [text](/p/{POSTID}/ppt/ID)
  // Support optional hash: surl:POSTID/ppt/ID#HASH
  processedContent = processedContent.replace(
    /\[([^\]]+)\]\(surl:(\d+)\/ppt\/([^)#\s]+)(?:#([^)]+))?\)/g,
    (_, text, postId, id, hash) => {
      const hashPart = hash ? `#${hash}` : "";
      return `[${text}](/p/${postId}/ppt/${id}${hashPart})`;
    }
  );

  // 3. [text](surl:raw/ID) -> [text](/p/{currentPostId}/raw/ID)
  processedContent = processedContent.replace(
    /\[([^\]]+)\]\(surl:raw\/([^)\s]+)\)/g,
    (_, text, id) => {
      return `[${text}](/p/${currentPostId}/raw/${id})`;
    }
  );

  // 4. [text](surl:POSTID/raw/ID) -> [text](/p/{POSTID}/raw/ID)
  processedContent = processedContent.replace(
    /\[([^\]]+)\]\(surl:(\d+)\/raw\/([^)\s]+)\)/g,
    (_, text, postId, id) => {
      return `[${text}](/p/${postId}/raw/${id})`;
    }
  );

  // 5. [text](surl:POSTID) or [text](surl:POSTID#HASH) -> [text](/p/{POSTID}#HASH)
  processedContent = processedContent.replace(
    /\[([^\]]+)\]\(surl:(\d+)(?:#([^)]+))?\)/g,
    (_, text, postId, hash) => {
      const hashPart = hash ? `#${hash}` : "";
      return `[${text}](/p/${postId}${hashPart})`;
    }
  );

  return processedContent;
}
