"use client";

import Link from "next/link";

import { useGlobalLoginMember } from "@/stores/auth/loginMember";

import { Button } from "@/components/ui/button";
import {
  Drawer,
  DrawerClose,
  DrawerContent,
  DrawerDescription,
  DrawerHeader,
  DrawerTitle,
  DrawerTrigger,
} from "@/components/ui/drawer";
import { Separator } from "@/components/ui/separator";

import {
  LogOut,
  Menu,
  MonitorCog,
  NotebookTabs,
  TableOfContents,
  User,
  UserRoundSearch,
} from "lucide-react";

import LoginPageButton from "./LoginPageButton";
import Logo from "./Logo";
import MeMenuButton from "./MeMenuButton";
import PostWriteButton from "./PostWriteButton";
import ThemeToggleButton from "./ThemeToggleButton";

export default function NarrowHeaderContent({
  className,
}: {
  className?: string;
}) {
  const {
    isLogin,
    isAdmin,
    loginMember,
    logoutAndHome,
    isAdminPage,
    isUserPage,
  } = useGlobalLoginMember();

  return (
    <div className={`${className} py-1`}>
      <Drawer>
        <DrawerTrigger asChild>
          <Button variant="link">
            <Menu />
          </Button>
        </DrawerTrigger>
        <DrawerContent>
          <DrawerHeader className="sr-only">
            <DrawerTitle>전체 메뉴</DrawerTitle>
            <DrawerDescription>전체 메뉴</DrawerDescription>
          </DrawerHeader>
          <div className="max-h-[calc(100dvh-150px)] px-2 pb-2 overflow-y-auto">
            <ul>
              {isUserPage && (
                <>
                  <li>
                    <DrawerClose asChild>
                      <Button
                        variant="link"
                        className="w-full justify-start"
                        asChild
                      >
                        <Link href="/post/list">
                          <TableOfContents /> 글
                        </Link>
                      </Button>
                    </DrawerClose>
                  </li>
                  {isLogin && (
                    <li>
                      <DrawerClose asChild>
                        <PostWriteButton
                          className="w-full justify-start"
                          text
                        />
                      </DrawerClose>
                    </li>
                  )}
                  {isLogin && (
                    <li>
                      <DrawerClose asChild>
                        <Button
                          variant="link"
                          className="w-full justify-start"
                          asChild
                        >
                          <Link href="/post/mine">
                            <NotebookTabs /> 내글
                          </Link>
                        </Button>
                      </DrawerClose>
                    </li>
                  )}
                </>
              )}

              {isAdminPage && (
                <>
                  <li>
                    <DrawerClose asChild>
                      <Button
                        variant="link"
                        className="w-full justify-start"
                        asChild
                      >
                        <Link href="/adm">
                          <MonitorCog /> 관리자 홈
                        </Link>
                      </Button>
                    </DrawerClose>
                  </li>

                  <li>
                    <DrawerClose asChild>
                      <Button
                        variant="link"
                        className="w-full justify-start"
                        asChild
                      >
                        <Link href="/adm/member/list">
                          <UserRoundSearch /> 회원관리
                        </Link>
                      </Button>
                    </DrawerClose>
                  </li>
                </>
              )}
              <li className="py-2">
                <Separator />
              </li>
              <li>
                <DrawerClose asChild>
                  <Button
                    variant="link"
                    className="w-full justify-start"
                    asChild
                  >
                    <Logo text />
                  </Button>
                </DrawerClose>
              </li>
              {!isLogin && (
                <li>
                  <DrawerClose asChild>
                    <Button
                      variant="link"
                      className="w-full justify-start"
                      asChild
                    >
                      <LoginPageButton text />
                    </Button>
                  </DrawerClose>
                </li>
              )}
              {isLogin && (
                <li>
                  <DrawerClose asChild>
                    <Button
                      variant="link"
                      className="w-full justify-start"
                      asChild
                    >
                      <Link href="/member/me">
                        <User /> {loginMember.nickname}
                      </Link>
                    </Button>
                  </DrawerClose>
                </li>
              )}
              {isAdmin && (
                <li>
                  <DrawerClose asChild>
                    <Button
                      variant="link"
                      className="w-full justify-start"
                      asChild
                    >
                      <Link href="/adm">
                        <MonitorCog /> 관리자 홈
                      </Link>
                    </Button>
                  </DrawerClose>
                </li>
              )}
              {isLogin && (
                <li>
                  <DrawerClose asChild>
                    <Button variant="link" onClick={logoutAndHome}>
                      <LogOut /> 로그아웃
                    </Button>
                  </DrawerClose>
                </li>
              )}
            </ul>
          </div>
        </DrawerContent>
      </Drawer>

      {isUserPage && (
        <Button variant="link" asChild>
          <Logo />
        </Button>
      )}
      {isAdminPage && (
        <Button variant="link" asChild>
          <Link href="/adm">
            <MonitorCog />
          </Link>
        </Button>
      )}
      <div className="flex-grow"></div>
      {isLogin && <MeMenuButton />}
      <ThemeToggleButton />
    </div>
  );
}
