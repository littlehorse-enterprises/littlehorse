"use server"

import { WfRun, WfRunId } from "littlehorse-client/proto"
import { lhClient } from "@/lib/lhClient"

export async function getWfRunsFromIds(wfRunIds: WfRunId[], tenantId: string): Promise<WfRun[]> {
  if (!wfRunIds || wfRunIds.length === 0) {
    return []
  }

  try {
    const client = await lhClient(tenantId)
    
    // Fetch each workflow run in parallel
    const wfRunPromises = wfRunIds.map(async (wfRunId) => {
      return await client.getWfRun(wfRunId)
    })

    // Wait for all requests to complete
    const wfRuns = await Promise.all(wfRunPromises)
    
    // Filter out any undefined results
    return wfRuns.filter((run): run is WfRun => run !== undefined)
  } catch (error) {
    console.error("Error fetching workflow runs:", error)
    return []
  }
} 