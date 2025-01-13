'use server'
import { lhClient } from '../lhClient'

export const getWfSpecsByTaskDef = async (tenantId: string, taskDefName: string) => {
  const client = await lhClient({ tenantId })
  const wfSpecs = await client.searchWfSpec({ taskDefName })
  return wfSpecs
}
