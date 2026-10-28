import type { Metadata } from "next";
import { Geist } from "next/font/google";
import Link from "next/link";
import { MeProvider } from "@/components/me-provider";
import { UserMenu } from "@/components/user-menu";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: { default: "1일 1이슈", template: "%s · 1일 1이슈" },
  description: "오늘의 이슈에 찬성·중립·반대로 답하고, 사람들이 왜 다르게 생각하는지 읽어 보세요.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="ko" className={`${geistSans.variable} h-full antialiased`}>
      <body className="flex min-h-full flex-col bg-white text-stone-900">
        <MeProvider>
          <header className="border-b border-stone-200">
            <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-3">
              <Link href="/" className="text-lg font-bold">
                1일 1이슈
              </Link>
              <UserMenu />
            </div>
          </header>
          <main className="mx-auto w-full max-w-3xl flex-1 px-4 py-8">{children}</main>
          <footer className="border-t border-stone-200 py-6 text-center text-xs text-stone-400">
            <p>이슈 설명은 원문 기사를 바탕으로 직접 작성했으며, 투표 결과는 서비스 참여자 기준입니다.</p>
            <Link href="/privacy" className="mt-2 inline-block underline">
              개인정보 처리방침
            </Link>
          </footer>
        </MeProvider>
      </body>
    </html>
  );
}
