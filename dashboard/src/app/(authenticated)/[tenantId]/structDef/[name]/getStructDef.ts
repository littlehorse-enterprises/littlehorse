import { lhClient } from '@/app/lhClient'
import { StructDefId } from 'littlehorse-client/proto'
import { StructDef } from 'littlehorse-client/proto'

export const getStructDef = async (tenantId: string, request: StructDefId): Promise<StructDef> => {
  const client = await lhClient({ tenantId })
  return client.getStructDef(request)
}
