import { Toaster } from "@/components/ui/sonner";
import { ClientLayout } from "./ClientLayout";

export default function MainLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div
      className="flex flex-col min-h-[100dvh] bg-background"
      vaul-drawer-wrapper="true"
    >
      <ClientLayout>
        {children}
        <Toaster />
      </ClientLayout>
    </div>
  );
}
