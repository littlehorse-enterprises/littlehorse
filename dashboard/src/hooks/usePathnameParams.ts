"use client";

import { PathnameKeys } from "@/types/PageParams";
import { useParams } from "next/navigation";

export function useTypedParams(): Record<PathnameKeys, string> {
  const params = useParams();

  return {
    tenantId: params.tenantId as string,
    wfSpecName: params.wfSpecName as string,
    wfSpecVersion: params.wfSpecVersion as string,
  };
}
