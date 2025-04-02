'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { ExternalEventId, PutExternalEventRequest } from 'littlehorse-client/proto'

export const getExternalEvent = async ({ tenantId, ...req }: ExternalEventId & WithTenant) => {
  const client = await lhClient({ tenantId })
  return client.getExternalEvent(req)
}

export const putExternalEvent = async ({ tenantId, ...req }: PutExternalEventRequest & WithTenant) => {
  const client = await lhClient({ tenantId })
  return client.putExternalEvent(req)
}
