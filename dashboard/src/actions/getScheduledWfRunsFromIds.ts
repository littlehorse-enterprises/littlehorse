"use server"

import { ScheduledWfRun, ScheduledWfRunId } from "littlehorse-client/proto"
import { lhClient } from "@/lib/lhClient"

export async function getScheduledWfRunsFromIds(scheduledWfRunIds: ScheduledWfRunId[], tenantId: string): Promise<ScheduledWfRun[]> {
  if (!scheduledWfRunIds || scheduledWfRunIds.length === 0) {
    return []
  }

  try {
    const client = await lhClient(tenantId)
    
    // Fetch each scheduled workflow run in parallel
    const scheduledWfRunPromises = scheduledWfRunIds.map(async (scheduledWfRunId) => {
      return await client.getScheduledWfRun(scheduledWfRunId)
    })

    // Wait for all requests to complete
    const scheduledWfRuns = await Promise.all(scheduledWfRunPromises)
    
    // Filter out any undefined results
    return scheduledWfRuns.filter((run): run is ScheduledWfRun => run !== undefined)
  } catch (error) {
    console.error("Error fetching scheduled workflow runs:", error)
    return []
  }
} 