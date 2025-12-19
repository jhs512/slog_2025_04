"use client";

import LoginForm from "@/lib/business/components/LoginForm";

export default function ClientPage() {
  return (
    <div className="flex-1 flex flex-col justify-center items-center gap-8">
      <h1 className="text-2xl font-bold">관리자 로그인</h1>
      <LoginForm />
    </div>
  );
}
