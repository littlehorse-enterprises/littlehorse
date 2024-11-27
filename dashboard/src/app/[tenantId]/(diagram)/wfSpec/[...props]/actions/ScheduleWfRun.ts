'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { ScheduleWfRequest } from 'littlehorse-client/proto'
import { ScheduledWfRun } from '../../../../../../../../sdk-js/dist/proto/scheduled_wf_run'

export const ScheduleWfRun = async ({
  wfSpecName,
  tenantId,
  majorVersion,
  revision,
  parentWfRunId,
  id,
  variables,
  cronExpression,
}: ScheduleWfRequest & WithTenant): Promise<ScheduledWfRun> => {
  const client = await lhClient({ tenantId })
  return client.scheduleWf({ wfSpecName, majorVersion, revision, parentWfRunId, id, variables, cronExpression })
}
