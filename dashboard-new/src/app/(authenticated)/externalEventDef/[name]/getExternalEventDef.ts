'use server'

import { lhClient } from '@/app/lhClient'
import { ExternalEventDef } from 'littlehorse-client/dist/proto/external_event'
import { ExternalEventDefId } from 'littlehorse-client/dist/proto/object_id'
import { cookies } from 'next/headers'

export const getExternalEventDef = async (request: ExternalEventDefId): Promise<ExternalEventDef> => {
  const tenantId = cookies().get('tenantId')?.value
  const client = await lhClient({ tenantId })

  return client.getExternalEventDef(request)
}
