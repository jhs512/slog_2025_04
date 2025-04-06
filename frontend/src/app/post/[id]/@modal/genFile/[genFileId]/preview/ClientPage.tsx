"use client";

import { useRouter } from "next/navigation";

import { components } from "@/lib/backend/apiV1/schema";
import { getFileSizeHr } from "@/lib/business/utils";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

import { Download } from "lucide-react";

export default function ClientPage({
  id,
  genFile,
}: {
  id: string;
  genFile: components["schemas"]["PostGenFileDto"];
}) {
  const router = useRouter();

  return (
    <Dialog
      open
      onOpenChange={() => {
        router.back();
      }}
    >
      <DialogContent className="max-w-[100dvh] max-h-[100dvh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>파일 미리보기</DialogTitle>
          <DialogDescription>
            {id}번 글의 파일({genFile.originalFileName})
          </DialogDescription>
        </DialogHeader>
        <div className="flex justify-center">
          {genFile.fileExtTypeCode == "img" && (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={genFile.publicUrl} alt="" />
          )}
          {genFile.fileExtTypeCode == "audio" && (
            <audio src={genFile.publicUrl} controls />
          )}
          {genFile.fileExtTypeCode == "video" && (
            <video src={genFile.publicUrl} controls />
          )}
        </div>
        <Button variant="link" asChild className="justify-start">
          <a href={genFile.downloadUrl} className="flex items-center gap-2">
            <Download />

            <span>
              {genFile.originalFileName}
              <br />({getFileSizeHr(genFile.fileSize)}) 다운로드
            </span>
          </a>
        </Button>
        <DialogFooter className="gap-2">
          <Button
            variant="outline"
            onClick={() => {
              router.back();
            }}
          >
            닫기
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
