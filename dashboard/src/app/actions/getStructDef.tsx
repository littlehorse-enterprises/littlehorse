'use server'

import { StructDefId } from 'littlehorse-client/proto'
import { lhClient } from '../lhClient'

export async function getStructDef(tenantId: string, structDefId: StructDefId) {
  const client = await lhClient({ tenantId })
  return await client.getStructDef({ name: structDefId.name, version: structDefId.version })
}
