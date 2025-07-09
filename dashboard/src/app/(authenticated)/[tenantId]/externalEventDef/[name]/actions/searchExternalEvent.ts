'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import {
  ExternalEvent,
  ExternalEventId,
  ExternalEventIdList,
  NodeRun,
  SearchExternalEventRequest,
} from 'littlehorse-client/proto'

export interface runDetails {
  externalEvent: ExternalEvent
  nodeRun: NodeRun | null
}
export interface PaginatedExternalEventList extends ExternalEventIdList {
  resultsWithDetails: runDetails[]
  bookmarkAsString: string | undefined
}

type WithBookmarkAsString = {
  bookmarkAsString: string | undefined
}

export type ExternalEventSearchProps = SearchExternalEventRequest & WithTenant & WithBookmarkAsString
export const searchExternalEvent = async ({
  tenantId,
  bookmarkAsString,
  ...req
}: ExternalEventSearchProps): Promise<PaginatedExternalEventList> => {
  const client = await lhClient({ tenantId })
  const requestWithBookmark = bookmarkAsString ? { ...req, bookmark: Buffer.from(bookmarkAsString, 'base64') } : req
  const externalEventIdList: ExternalEventIdList = await client.searchExternalEvent(requestWithBookmark)

  const hydrateWithExternalEventDetails = (): Promise<runDetails>[] => {
    return externalEventIdList.results.map(async (externalEventId: ExternalEventId) => {
      if (!externalEventId.externalEventDefId) {
        throw new Error('externalEventDefId is required')
      }
      const externalEvent = await client.getExternalEvent({
        externalEventDefId: externalEventId.externalEventDefId,
        wfRunId: externalEventId.wfRunId,
        guid: externalEventId.guid,
      })

      let nodeRun = null
      try {
        nodeRun = await client.getNodeRun(externalEvent.id!)
      } catch {}

      return {
        externalEvent,
        nodeRun,
      }
    })
  }

  const externalEventWithDetails: runDetails[] = await Promise.all(hydrateWithExternalEventDetails())

  return {
    ...externalEventIdList,
    bookmarkAsString: externalEventIdList.bookmark?.toString('base64'),
    resultsWithDetails: externalEventWithDetails,
  }
}
