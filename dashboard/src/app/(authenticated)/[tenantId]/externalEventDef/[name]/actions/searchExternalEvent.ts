'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import {
  CorrelatedEvent,
  CorrelatedEventId,
  CorrelatedEventIdList,
  DeleteCorrelatedEventRequest,
  ExternalEvent,
  ExternalEventId,
  ExternalEventIdList,
  NodeRun,
  PutCorrelatedEventRequest,
  SearchCorrelatedEventRequest,
  SearchExternalEventRequest,
  VariableValue,
} from 'littlehorse-client/proto'

export interface runDetails {
  externalEvent: ExternalEvent
  nodeRun: NodeRun | null
}

export interface correlatedEventDetails {
  correlatedEvent: CorrelatedEvent
}

export interface PaginatedExternalEventList extends ExternalEventIdList {
  resultsWithDetails: runDetails[]
  bookmarkAsString: string | undefined
}

export interface PaginatedCorrelatedEventList extends CorrelatedEventIdList {
  resultsWithDetails: correlatedEventDetails[]
  bookmarkAsString: string | undefined
}

type WithBookmarkAsString = {
  bookmarkAsString: string | undefined
}

export type ExternalEventSearchProps = SearchExternalEventRequest & WithTenant & WithBookmarkAsString
export type CorrelatedEventSearchProps = SearchCorrelatedEventRequest & WithTenant & WithBookmarkAsString
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
      } catch {
        // nodeRun is null if the node run is not found
      }

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

export const searchCorrelatedEvent = async ({
  tenantId,
  bookmarkAsString,
  ...req
}: CorrelatedEventSearchProps): Promise<PaginatedCorrelatedEventList> => {
  const client = await lhClient({ tenantId })
  const requestWithBookmark = bookmarkAsString ? { ...req, bookmark: Buffer.from(bookmarkAsString, 'base64') } : req
  const correlatedEventIdList: CorrelatedEventIdList = await client.searchCorrelatedEvent(requestWithBookmark)

  const hydrateWithCorrelatedEventDetails = (): Promise<correlatedEventDetails>[] => {
    return correlatedEventIdList.results.map(async (correlatedEventId: CorrelatedEventId) => {
      const correlatedEvent = await client.getCorrelatedEvent(correlatedEventId)

      return {
        correlatedEvent,
      }
    })
  }

  const correlatedEventWithDetails: correlatedEventDetails[] = await Promise.all(hydrateWithCorrelatedEventDetails())

  return {
    ...correlatedEventIdList,
    bookmarkAsString: correlatedEventIdList.bookmark?.toString('base64'),
    resultsWithDetails: correlatedEventWithDetails,
  }
}

export const deleteCorrelatedEvent = async ({
  tenantId,
  correlatedEventId,
}: {
  tenantId: string
  correlatedEventId: CorrelatedEventId
}): Promise<void> => {
  const client = await lhClient({ tenantId })
  const request: DeleteCorrelatedEventRequest = {
    id: correlatedEventId,
  }
  await client.deleteCorrelatedEvent(request)
}

export const putCorrelatedEvent = async ({
  tenantId,
  key,
  externalEventDefName,
  content,
}: {
  tenantId: string
  key: string
  externalEventDefName: string
  content?: VariableValue
}): Promise<CorrelatedEvent> => {
  const client = await lhClient({ tenantId })
  const request: PutCorrelatedEventRequest = {
    key,
    externalEventDefId: { name: externalEventDefName },
    content,
  }
  return await client.putCorrelatedEvent(request)
}
