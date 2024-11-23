'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { ScheduledWfRunIdList } from 'littlehorse-client/proto'

type GetWfSpecProps = {
  name: string
  version: string
} & WithTenant

export const getScheduleWfSpec = async ({ name, version, tenantId }: GetWfSpecProps): Promise<ScheduledWfRunIdList> => {
  const client = await lhClient({ tenantId })

  const [majorVersion, revision] = version.split('.')
  return client.searchScheduledWfRun({
    wfSpecName: name,
    majorVersion: parseInt(majorVersion) || 0,
    revision: parseInt(revision) | 0,
  })
}
