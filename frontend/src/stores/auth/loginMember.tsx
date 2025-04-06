"use client";

import { createContext, use, useState } from "react";

import { usePathname, useRouter } from "next/navigation";

import client from "@/lib/backend/client";

import { components } from "@/lib/backend/apiV1/schema";

type Member = components["schemas"]["MemberDto"];

export const LoginMemberContext = createContext<{
  loginMember: Member;
  setLoginMember: (member: Member) => void;
  isLoginMemberPending: boolean;
  isLogin: boolean;
  isAdmin: boolean;
  logout: (callback: () => void) => void;
  logoutAndHome: () => void;
  isAdminPage: boolean;
  isUserPage: boolean;
}>({
  loginMember: createEmptyMember(),
  setLoginMember: () => {},
  isLoginMemberPending: true,
  isLogin: false,
  isAdmin: false,
  logout: () => {},
  logoutAndHome: () => {},
  isAdminPage: false,
  isUserPage: false,
});

function createEmptyMember(): Member {
  return {
    id: 0,
    createDate: "",
    modifyDate: "",
    nickname: "",
    profileImgUrl: "",
  };
}

export function useLoginMember() {
  const router = useRouter();
  const pathname = usePathname();

  const [isLoginMemberPending, setLoginMemberPending] = useState(true);
  const [loginMember, _setLoginMember] = useState<Member>(createEmptyMember());

  const removeLoginMember = () => {
    _setLoginMember(createEmptyMember());
    setLoginMemberPending(false);
  };

  const setLoginMember = (member: Member) => {
    _setLoginMember(member);
    setLoginMemberPending(false);
  };

  const setNoLoginMember = () => {
    setLoginMemberPending(false);
  };

  const isLogin = loginMember.id !== 0;
  const isAdmin = loginMember.id === 2;

  const logout = (callback: () => void) => {
    client.DELETE("/api/v1/members/logout").then(() => {
      removeLoginMember();
      callback();
    });
  };

  const logoutAndHome = () => {
    logout(() => router.replace("/"));
  };

  const isAdminPage = pathname.startsWith("/adm");
  const isUserPage = !isAdminPage;

  return {
    loginMember,
    setLoginMember,
    isLoginMemberPending,
    setNoLoginMember,
    isLogin,
    isAdmin,
    logout,
    logoutAndHome,
    isAdminPage,
    isUserPage,
  };
}

export function useGlobalLoginMember() {
  return use(LoginMemberContext);
}
