'use server'


import { lhClient } from '../lhClient'
import { WfSpecData } from '@/types'

export async function getLatestWfSpecs(tenantId: string, wfSpecNames: string[]): Promise<WfSpecData[]> {
  const client = await lhClient({ tenantId })
  const specMap = new Map<string, WfSpecData>()

  await Promise.all(
    wfSpecNames.map(async name => {
      if (!specMap.has(name)) {
        const wf = await client.getLatestWfSpec({ name })
        specMap.set(name, {
          name,
          latestVersion: `${wf?.id?.majorVersion}.${wf?.id?.revision}`,
        })
      }
    })
  )

  return Array.from(specMap.values())
}
