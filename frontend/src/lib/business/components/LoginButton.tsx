"use client";

import { Button } from "@/components/ui/button";

import { LogIn } from "lucide-react";

export default function LoginButton({
  variant,
  className,
  text,
  icon,
  providerTypeCode = "kakao",
}: {
  variant?:
    | "link"
    | "default"
    | "destructive"
    | "outline"
    | "secondary"
    | "ghost"
    | null
    | undefined;
  className?: string;
  text?: string | boolean;
  icon?: React.ReactNode;
  providerTypeCode?: string;
}) {
  const socialLoginForKakaoUrl = `${process.env.NEXT_PUBLIC_API_BASE_URL}/oauth2/authorization/${providerTypeCode}`;
  const redirectUrlAfterSocialLogin = process.env.NEXT_PUBLIC_FRONTEND_BASE_URL;
  if (!variant) variant = "link";
  if (typeof text === "boolean") text = "로그인";

  return (
    <Button variant={variant} className={className} asChild>
      <a
        href={`${socialLoginForKakaoUrl}?redirectUrl=${redirectUrlAfterSocialLogin}`}
      >
        {icon || <LogIn />}
        {text && <span>{text}</span>}
      </a>
    </Button>
  );
}
