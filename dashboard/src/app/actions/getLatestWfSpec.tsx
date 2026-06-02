'use server'

import { uniqueInOrder } from '@/app/utils'
import { WfSpecData } from '@/types'
import { lhClient } from '../lhClient'

export async function getLatestWfSpecs(tenantId: string, wfSpecNames: string[]): Promise<WfSpecData[]> {
  const client = await lhClient({ tenantId })
  const uniqueOrdered = uniqueInOrder(wfSpecNames)

  const specMap = new Map<string, WfSpecData>()
  await Promise.all(
    uniqueOrdered.map(async name => {
      const wf = await client.getLatestWfSpec({ name })
      if (!wf) return

      specMap.set(name, {
        name,
        latestVersion: `${wf.id?.majorVersion}.${wf.id?.revision}`,
        createdAt: wf.createdAt ? new Date(wf.createdAt) : undefined,
        parentWfSpec: wf.parentWfSpec ?? undefined,
      })
    })
  )

  return uniqueOrdered.map(name => specMap.get(name)).filter((row): row is WfSpecData => row != null)
}
