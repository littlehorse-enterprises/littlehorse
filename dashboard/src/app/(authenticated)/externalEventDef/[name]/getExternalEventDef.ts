'use server'

import { lhClient } from '@/app/lhClient'
import { ExternalEventDef, ExternalEventDefId } from 'littlehorse-client/proto'
import { cookies } from 'next/headers'

export const getExternalEventDef = async (request: ExternalEventDefId): Promise<ExternalEventDef> => {
  const tenantId = cookies().get('tenantId')?.value
  const client = await lhClient({ tenantId })

  return client.getExternalEventDef(request)
}
