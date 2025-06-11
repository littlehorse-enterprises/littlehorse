"use client";

import { executeRpc } from "@/actions/executeRPC";
import {
  LHMethodParamType,
  LHMethodReturnType,
  LittleHorseMethodRPCName,
} from "@/types/executeRPCTypes";
import { useParams } from "next/navigation";
import useSWR from "swr";

export function useExecuteRPCWithSWR<M extends LittleHorseMethodRPCName>(
  methodName: M,
  request: LHMethodParamType<M>
) {
  const tenantId  = useParams().tenantId as string;

  return useSWR<LHMethodReturnType<M>>([methodName, request], async () => {
    return await executeRpc(methodName, request, tenantId);
  });
}
