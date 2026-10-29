import type { Metadata } from "next";
import Link from "next/link";
import { Logo } from "@/components/logo";
import { MeProvider } from "@/components/me-provider";
import { UserMenu } from "@/components/user-menu";
import "pretendard/dist/web/variable/pretendardvariable-dynamic-subset.css";
import "./globals.css";

export const metadata: Metadata = {
  title: { default: "1일 1이슈", template: "%s · 1일 1이슈" },
  description: "오늘의 이슈에 찬성·중립·반대로 답하고, 사람들이 왜 다르게 생각하는지 읽어 보세요.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="ko" className="h-full antialiased">
      <body className="flex min-h-full flex-col">
        <MeProvider>
          <header className="sticky top-0 z-10 border-b border-line bg-canvas/85 backdrop-blur">
            <div className="mx-auto flex h-14 max-w-3xl items-center justify-between px-4">
              <Logo />
              <UserMenu />
            </div>
          </header>
          <main className="mx-auto w-full max-w-3xl flex-1 px-4 py-8 sm:py-10">{children}</main>
          <footer className="border-t border-line px-4 py-8 text-center text-xs leading-relaxed text-subtle">
            <p>이슈 설명은 원문 기사를 바탕으로 직접 작성했으며, 투표 결과는 서비스 참여자 기준입니다.</p>
            <p className="mt-3 flex justify-center gap-4">
              <Link href="/terms" className="hover:text-ink">
                이용약관
              </Link>
              <Link href="/privacy" className="hover:text-ink">
                개인정보 처리방침
              </Link>
            </p>
          </footer>
        </MeProvider>
      </body>
    </html>
  );
}
