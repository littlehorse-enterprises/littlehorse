'use server'

import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { ScheduledWfRun } from 'littlehorse-client/proto'

type GetWfSpecProps = {
  name: string
  version: string
} & WithTenant

export const getScheduleWfSpec = async ({ name, version, tenantId }: GetWfSpecProps): Promise<ScheduledWfRun[]> => {
  const client = await lhClient({ tenantId })

  const [majorVersion, revision] = version.split('.')

  return Promise.all(
    (
      await client.searchScheduledWfRun({
        wfSpecName: name,
        majorVersion: parseInt(majorVersion) || 0,
        revision: parseInt(revision) || 0,
      })
    ).results.map(async scheduledWfRun => {
      return await client.getScheduledWfRun({
        id: scheduledWfRun.id,
      })
    })
  )
}
