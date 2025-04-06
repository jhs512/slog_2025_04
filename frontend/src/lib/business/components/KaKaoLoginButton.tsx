"use client";

import { MessageCircle } from "lucide-react";

import LoginButton from "./LoginButton";

export default function KaKaoLoginButton({
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
  if (typeof text === "boolean") text = "카카오 로그인";
  if (!icon) icon = <MessageCircle />;
  if (!variant) variant = "outline";

  return (
    <LoginButton
      variant={variant}
      className={className}
      text={text}
      icon={icon}
    />
  );
}
