"use client";

import Image from "next/image";

import { getDateHr } from "@/lib/business/utils";

import { useGlobalLoginMember } from "@/stores/auth/loginMember";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function ClientPage() {
  const { loginMember } = useGlobalLoginMember();

  return (
    <div className="flex-1 flex justify-center items-center">
      <Card>
        <CardHeader>
          <CardTitle className="text-center">내 정보</CardTitle>
          <CardDescription className="sr-only">내 정보</CardDescription>
        </CardHeader>
        <CardContent className="grid grid-cols-1 gap-4 justify-items-center">
          <Image
            src={loginMember.profileImgUrl}
            alt={loginMember.nickname}
            width={100}
            height={100}
            quality={100}
            className="w-[100px] h-[100px] object-cover rounded-full ring-2 ring-primary/10"
          />
          <div className="text-xl font-medium">{loginMember.nickname}</div>
          <div className="text-sm text-muted-foreground">
            <span>가입날짜 : </span>
            <span>{getDateHr(loginMember.createDate)}</span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
