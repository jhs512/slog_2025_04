"use client";

import LoginButton from "./LoginButton";

export default function NaverLoginButton({
  variant,
  className,
  text,
  icon,
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
}) {
  if (typeof text === "boolean") text = "네이버 로그인";
  if (!icon) icon = <span>N</span>;
  if (!variant) variant = "outline";

  return (
    <LoginButton
      variant={variant}
      className={className}
      text={text}
      icon={icon}
      providerTypeCode="naver"
    />
  );
}
