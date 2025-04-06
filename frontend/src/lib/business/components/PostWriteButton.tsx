import { ButtonHTMLAttributes } from "react";
import { toast } from "sonner";

import { useRouter } from "next/navigation";

import client from "@/lib/backend/client";

import { Button } from "@/components/ui/button";

import { Pencil } from "lucide-react";

interface PostWriteButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  className?: string;
  text?: boolean;
}

const PostWriteButton = ({
  className,
  text,
  onClick,
  ...props
}: PostWriteButtonProps) => {
  const router = useRouter();

  const handleClick = async (e: React.MouseEvent<HTMLButtonElement>) => {
    // 기존 onClick 이벤트가 있다면 실행
    if (onClick) {
      onClick(e);
    }

    const response = await client.POST("/api/v1/posts/temp");

    if (response.error) {
      toast.error(response.error.msg);
    } else {
      toast(response.data.msg);

      router.replace(`/post/${response.data.data.id}/edit`);
    }
  };

  return (
    <Button
      className={className}
      variant="link"
      onClick={handleClick}
      {...props}
    >
      <Pencil />
      {text && "작성"}
    </Button>
  );
};

PostWriteButton.displayName = "PostWriteButton";

export default PostWriteButton;
