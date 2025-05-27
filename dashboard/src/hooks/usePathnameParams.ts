"use client";

import { usePathname } from "next/navigation";

export function usePathnameParams() {
  const pathname = usePathname();
  const params = pathname.split("/");

  return {
    tenantId: params[1],
  };
}
