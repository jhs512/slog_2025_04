"use client";

import Link from "next/link";

import { Button } from "@/components/ui/button";

import { LogIn } from "lucide-react";

export default function LoginPageButton({
  variant,
  className,
  text,
  icon,
  ...props
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
  if (!variant) variant = "link";
  if (typeof text === "boolean") text = "로그인";

  return (
    <Button {...props} variant={variant} className={className} asChild>
      <Link href="/">
        {icon || <LogIn />}
        {text && <span>{text}</span>}
      </Link>
    </Button>
  );
}
