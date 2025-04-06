"use client";

import Link from "next/link";

import { useGlobalLoginMember } from "@/stores/auth/loginMember";

import { Button } from "@/components/ui/button";

export default function RequireAdmin({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isAdmin } = useGlobalLoginMember();

  if (!isAdmin)
    return (
      <div className="flex-1 flex justify-center items-center">
        <div>
          <div className="text-muted-foreground">
            해당 페이지는 관리자만 이용할 수 있습니다.
          </div>
          <div className="mt-2 flex justify-center">
            <Button variant="link" asChild>
              <Link href="/">메인으로 돌아가기</Link>
            </Button>
          </div>
        </div>
      </div>
    );

  return <>{children}</>;
}
