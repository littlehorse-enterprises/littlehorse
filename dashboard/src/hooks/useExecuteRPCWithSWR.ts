import useSWR from "swr";
import {
  LHMethodParamType,
  LHMethodReturnType,
  LittleHorseMethodRPCName,
} from "@/types/executeRPCTypes";
import { executeRpc } from "@/actions/executeRPC";
import { useTypedParams } from "./usePathnameParams";

export function useExecuteRPCWithSWR<M extends LittleHorseMethodRPCName>(
  methodName: M,
  request: LHMethodParamType<M>
) {
  const { tenantId } = useTypedParams();

  return useSWR<LHMethodReturnType<M>>([methodName, request], async () => {
    return await executeRpc(methodName, request, tenantId);
  });
}
