'use server'

import { WfSpecData } from '@/types'
import { lhClient } from '../lhClient'

export async function getLatestWfSpecs(tenantId: string, wfSpecNames: string[]): Promise<WfSpecData[]> {
  const client = await lhClient({ tenantId })
  const specMap = new Map<string, WfSpecData>()

  await Promise.all(
    wfSpecNames.map(async name => {
      if (!specMap.has(name)) {
        const wf = await client.getLatestWfSpec({ name })
        if (!wf) return

        specMap.set(name, {
          name,
          latestVersion: `${wf.id?.majorVersion}.${wf.id?.revision}`,
          createdAt: wf.createdAt ? new Date(wf.createdAt) : undefined,
          parentWfSpec: wf.parentWfSpec ?? undefined,
        })
      }
    })
  )

  return Array.from(specMap.values())
}
