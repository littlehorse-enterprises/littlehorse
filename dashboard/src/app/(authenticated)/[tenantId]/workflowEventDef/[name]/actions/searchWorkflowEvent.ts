'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import {
  NodeRun,
  SearchWorkflowEventRequest,
  WorkflowEvent,
  WorkflowEventId,
  WorkflowEventIdList,
} from 'littlehorse-client/proto'

export interface runDetails {
  workflowEvent: WorkflowEvent
  nodeRun: NodeRun | null
}
export interface PaginatedWorkflowEventList extends WorkflowEventIdList {
  resultsWithDetails: runDetails[]
  bookmarkAsString: string | undefined
}

type WithBookmarkAsString = {
  bookmarkAsString: string | undefined
}

export type WorkflowEventSearchProps = SearchWorkflowEventRequest & WithTenant & WithBookmarkAsString
export const searchWorkflowEvent = async ({
  tenantId,
  bookmarkAsString,
  ...req
}: WorkflowEventSearchProps): Promise<PaginatedWorkflowEventList> => {
  const client = await lhClient({ tenantId })
  const requestWithBookmark = bookmarkAsString ? { ...req, bookmark: Buffer.from(bookmarkAsString, 'base64') } : req
  const workflowEventIdList: WorkflowEventIdList = await client.searchWorkflowEvent(requestWithBookmark)

  const hydrateWithWorkflowEventDetails = (): Promise<runDetails>[] => {
    return workflowEventIdList.results.map(async (workflowEventId: WorkflowEventId) => {
      if (!workflowEventId.workflowEventDefId) {
        throw new Error('workflowEventDefId is required')
      }
      const workflowEvent = await client.getWorkflowEvent({
        workflowEventDefId: workflowEventId.workflowEventDefId,
        wfRunId: workflowEventId.wfRunId,
        number: workflowEventId.number,
      })

      let nodeRun = null
      try {
        nodeRun = await client.getNodeRun(workflowEvent.id!)
      } catch {
        // nodeRun is null if the node run is not found
      }

      return {
        workflowEvent,
        nodeRun,
      }
    })
  }

  const workflowEventWithDetails: runDetails[] = await Promise.all(hydrateWithWorkflowEventDetails())

  return {
    ...workflowEventIdList,
    bookmarkAsString: workflowEventIdList.bookmark?.toString('base64'),
    resultsWithDetails: workflowEventWithDetails,
  }
}
