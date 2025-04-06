"use client";

import KaKaoLoginButton from "@/lib/business/components/KaKaoLoginButton";
import NaverLoginButton from "@/lib/business/components/NaverLoginButton";

import { useGlobalLoginMember } from "@/stores/auth/loginMember";

export default function ClientPage() {
  const { isLogin, loginMember } = useGlobalLoginMember();

  return (
    <div className="flex-1 flex justify-center items-center">
      {!isLogin && (
        <div className="flex flex-col gap-2">
          <KaKaoLoginButton text />
        </div>
      )}
      {isLogin && <div>{loginMember.nickname}님 환영합니다.</div>}
    </div>
  );
}
