import type { Metadata } from "next";
import { AdminConsole } from "@/components/admin/admin-console";

export const metadata: Metadata = { title: "관리", robots: { index: false } };

export default function AdminPage() {
  return <AdminConsole />;
}
