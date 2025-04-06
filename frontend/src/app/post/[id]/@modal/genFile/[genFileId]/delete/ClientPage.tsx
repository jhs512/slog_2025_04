"use client";

import { toast } from "sonner";

import { useRouter } from "next/navigation";

import client from "@/lib/backend/client";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export default function ClientPage({
  id,
  genFileId,
}: {
  id: string;
  genFileId: string;
}) {
  const router = useRouter();

  const onDelete = async () => {
    const response = await client.DELETE(
      "/api/v1/posts/{postId}/genFiles/{id}",
      {
        params: {
          path: {
            postId: parseInt(id),
            id: parseInt(genFileId),
          },
        },
      },
    );

    if (response.error) {
      toast.error(response.error.msg);
      return;
    }

    toast(response.data.msg);

    sessionStorage.setItem("needToRefresh", "true");
    router.back();
  };

  return (
    <Dialog
      open
      onOpenChange={() => {
        router.back();
      }}
    >
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {id}번글의 {genFileId}번 파일 삭제
          </DialogTitle>
          <DialogDescription>파일을 정말 삭제하시겠습니까?</DialogDescription>
        </DialogHeader>
        <DialogFooter className="gap-2">
          <Button
            variant="outline"
            onClick={() => {
              router.back();
            }}
          >
            취소
          </Button>
          <Button variant="destructive" onClick={onDelete}>
            삭제
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
