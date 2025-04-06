interface UsePaginationProps {
  baseQueryString: string;
  totalPages: number;
  currentPageNumber: number;
}

export function usePagination({
  baseQueryString,
  totalPages,
  currentPageNumber,
}: UsePaginationProps) {
  const paginationArmSize = 1;

  const pageButtonUrl = (pageNumber: number) =>
    `?page=${pageNumber}&${baseQueryString}`;

  const prevEllipsisButtonPageNumber =
    currentPageNumber - paginationArmSize - 1 > 1
      ? currentPageNumber - paginationArmSize - 1
      : undefined;

  const nextEllipsisButtonPageNumber =
    currentPageNumber + paginationArmSize + 1 < totalPages
      ? currentPageNumber + paginationArmSize + 1
      : undefined;

  const middlePages = Array.from(
    { length: totalPages },
    (_, i) => i + 1,
  ).filter(
    (pageNum) =>
      pageNum > 1 &&
      pageNum < totalPages &&
      pageNum >= currentPageNumber - paginationArmSize &&
      pageNum <= currentPageNumber + paginationArmSize,
  );

  return {
    pageButtonUrl,
    prevEllipsisButtonPageNumber,
    nextEllipsisButtonPageNumber,
    middlePages,
  };
}
