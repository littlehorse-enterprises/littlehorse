import { lhClient } from '@/app/lhClient'
import { StructDefId } from 'littlehorse-client/proto'
import { StructDef } from '../../../../../../../sdk-js/dist/proto/struct_def'

export const getStructDef = async (tenantId: string, request: StructDefId): Promise<StructDef> => {
  const client = await lhClient({ tenantId })
  return client.getStructDef(request)
}
