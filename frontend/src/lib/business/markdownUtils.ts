export function processMarkdownContent(
  content: string,
  currentPostId: string | number
): string {
  let processedContent = content;

  // 1. [text](slog-link-to=ppt-ID) -> [text](/p/{currentPostId}/ppt?ppt_id=ID)
  // Support optional hash: slog-link-to=ppt-ID#HASH
  processedContent = processedContent.replace(
    /\[([^\]]+)\]\(slog-link-to=ppt-(\d+)(?:#([^)]+))?\)/g,
    (_, text, id, hash) => {
      const hashPart = hash ? `#${hash}` : "";
      return `[${text}](/p/${currentPostId}/ppt?ppt_id=${id}${hashPart})`;
    }
  );

  // 2. [text](slog-link-to=POSTID-ppt-ID) -> [text](/p/{POSTID}/ppt?ppt_id=ID)
  // Support optional hash: slog-link-to=POSTID-ppt-ID#HASH
  processedContent = processedContent.replace(
    /\[([^\]]+)\]\(slog-link-to=(\d+)-ppt-(\d+)(?:#([^)]+))?\)/g,
    (_, text, postId, id, hash) => {
      const hashPart = hash ? `#${hash}` : "";
      return `[${text}](/p/${postId}/ppt?ppt_id=${id}${hashPart})`;
    }
  );

  // 3. [text](slog-link-to=POSTID#HASH) or [text](slog-link-to=POSTID) -> [text](/p/{POSTID}#HASH)
  processedContent = processedContent.replace(
    /\[([^\]]+)\]\(slog-link-to=(\d+)(?:#([^)]+))?\)/g,
    (_, text, postId, hash) => {
      const hashPart = hash ? `#${hash}` : "";
      return `[${text}](/p/${postId}${hashPart})`;
    }
  );

  return processedContent;
}
