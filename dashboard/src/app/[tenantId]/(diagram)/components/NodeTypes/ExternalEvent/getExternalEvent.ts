'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { ExternalEventId } from 'littlehorse-client/proto'

export type ExternalEventRequestProps = ExternalEventId & WithTenant
export const getExternalEvent = async ({ tenantId, ...req }: ExternalEventRequestProps) => {
  const client = await lhClient({ tenantId })
  return client.getExternalEvent(req)
}
