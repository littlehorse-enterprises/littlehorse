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
  nodeRun: NodeRun
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
  const externalEvevntIdList: ExternalEventIdList = await client.searchExternalEvent(requestWithBookmark)
  const hydrateWithExternalEventDetails = (): Promise<runDetails>[] => {
    return externalEvevntIdList.results.map(async (externalEvevntId: ExternalEventId) => {
      if (!externalEvevntId.externalEventDefId) {
        throw new Error('externalEventDefId is required')
      }
      const externalEvent = await client.getExternalEvent({
        wfRunId: externalEvevntId.wfRunId,
        guid: externalEvevntId.guid,
        externalEventDefId: externalEvevntId.externalEventDefId,
      })
      const nodeRun = await client.getNodeRun(externalEvent.id!)

      return {
        externalEvent,
        nodeRun,
      }
    })
  }

  const externalEventWithDetails: runDetails[] = await Promise.all(hydrateWithExternalEventDetails())

  return {
    ...externalEvevntIdList,
    bookmarkAsString: externalEvevntIdList.bookmark?.toString('base64'),
    resultsWithDetails: externalEventWithDetails,
  }
}
