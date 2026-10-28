import type { Metadata } from "next";
import { Account } from "@/components/account";

export const metadata: Metadata = { title: "내 정보", robots: { index: false } };

export default function MePage() {
  return <Account />;
}
