'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { ExternalEventId, ExternalEventRun } from 'littlehorse-client/proto';


export type ExternalEventRunRequestProps = ExternalEventId & WithTenant
export const getExternalEventRun = async ({ tenantId, ...req }: ExternalEventRunRequestProps) => {
  const client = await lhClient({ tenantId })
  return client.getExternalEvent(req)
}
