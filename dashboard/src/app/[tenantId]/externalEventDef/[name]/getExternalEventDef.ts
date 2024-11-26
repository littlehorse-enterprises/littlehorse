'use server'

import { lhClient } from '@/app/lhClient'
import { ExternalEventDef, ExternalEventDefId } from 'littlehorse-client/proto'

export const getExternalEventDef = async (tenantId: string, request: ExternalEventDefId): Promise<ExternalEventDef> => {
  const client = await lhClient({ tenantId })

  return client.getExternalEventDef(request)
}
