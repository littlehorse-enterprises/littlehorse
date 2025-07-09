'use server'

import { lhClient } from '@/lhClient'
import { LHMethodParamType, LHMethodReturnType, LittleHorseMethodRPCName } from '@/types'

export async function executeRpc<M extends LittleHorseMethodRPCName>(
  methodName: M,
  params: LHMethodParamType<M>,
  tenantId: string
): Promise<LHMethodReturnType<M>> {
  const client = await lhClient(tenantId)

  // Use a type cast to inform TypeScript that this is a callable function

  return (client[methodName] as (request: LHMethodParamType<M>) => Promise<LHMethodReturnType<M>>)(params)
}
