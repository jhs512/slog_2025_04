import type { Metadata } from "next";

import localFont from "next/font/local";

import { Toaster } from "@/components/ui/sonner";

import { ClientLayout } from "./ClientLayout";
import "./globals.css";

const pretendard = localFont({
  src: "./../../node_modules/pretendard/dist/web/variable/woff2/PretendardVariable.woff2",
  display: "swap",
  weight: "45 920",
  variable: "--font-pretendard",
});

export const metadata: Metadata = {
  title: "글로그",
  description: "글로그는 당신을 위한 기술 블로그 입니다.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" suppressHydrationWarning>
      <body
        className={`${pretendard.variable} ${pretendard.className} antialiased`}
      >
        <div
          className="flex flex-col min-h-[100dvh] bg-background"
          vaul-drawer-wrapper="true"
        >
          <ClientLayout>
            {children}
            <Toaster />
          </ClientLayout>
        </div>
      </body>
    </html>
  );
}
