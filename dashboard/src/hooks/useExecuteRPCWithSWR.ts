import useSWR from "swr";
import {
  LHMethodParamType,
  LHMethodReturnType,
  LittleHorseMethodRPCName,
} from "@/lib/executeRPC";
import { executeRpc } from "@/actions/executeRPC";

export function useExecuteRPCWithSWR<M extends LittleHorseMethodRPCName>(
  methodName: M,
  request: LHMethodParamType<M>,
  tenantId: string
) {
  return useSWR<LHMethodReturnType<M>>([methodName, request], async () => {
    return await executeRpc(methodName, request, tenantId);
  });
}
