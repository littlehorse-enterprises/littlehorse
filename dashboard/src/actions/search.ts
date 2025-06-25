'use server'
import { SearchType } from '@/types/search'
import { WithBookmark, WithTenant } from '@/types/withs'
import { lhClient } from '@/utils/client/lhClient'
import { SEARCH_LIMIT_DEFAULT } from '@/utils/ui/constants'
import {
  ExternalEventDefId,
  TaskDefId,
  TaskRunId,
  UserTaskDefId,
  WfSpecId,
  WorkflowEventDefId,
} from 'littlehorse-client/proto'

type Props = { prefix?: string; limit?: number } & WithBookmark

const genericSearch = async <P, R>(props: P, fn: (props: P) => Promise<R>): Promise<R> => {
  return fn(props)
}

type SearchProps = { type: SearchType } & Props & WithTenant

export const search = async ({ type, tenantId, bookmark, limit, prefix }: SearchProps): Promise<SearchResponse> => {
  const client = await lhClient(tenantId)
  const request = {
    prefix,
    bookmark: bookmarkFrom(bookmark),
    limit: limit || SEARCH_LIMIT_DEFAULT,
  }

  let results
  switch (type) {
    case 'TaskDef':
      results = await genericSearch(request, client.searchTaskDef)
      break
    case 'UserTaskDef':
      results = await genericSearch(request, client.searchUserTaskDef)
      break
    case 'ExternalEventDef':
      results = await genericSearch(request, client.searchExternalEventDef)
      break
    case 'WorkflowEventDef':
      results = await genericSearch(request, client.searchWorkflowEventDef)
      break
    default:
      results = await genericSearch(request, client.searchWfSpec)
      break
  }

  return {
    ...results,
    type,
    bookmark: results.bookmark?.toString('base64'),
  }
}

const bookmarkFrom = (bookmark?: string): Buffer | undefined => {
  if (bookmark === undefined) return bookmark
  return Buffer.from(bookmark, 'base64')
}

export interface SearchResult {
  type: SearchType
  bookmark?: string

  // disable rule here because ts pmo
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  results: any
}

export type WfSpecList = SearchResult & {
  type: 'WfSpec'
  results: WfSpecId[]
}

type TaskDefList = SearchResult & {
  type: 'TaskDef'
  results: TaskDefId[]
}

type UserTaskDefList = SearchResult & {
  type: 'UserTaskDef'
  results: UserTaskDefId[]
}

type ExternalEventDefList = SearchResult & {
  type: 'ExternalEventDef'
  results: ExternalEventDefId[]
}

type WorkflowEventDefList = SearchResult & {
  type: 'WorkflowEventDef'
  results: WorkflowEventDefId[]
}

type TaskRunList = SearchResult & {
  type: 'TaskRun'
  results: TaskRunId[]
}

export type Ids = WfSpecId | TaskDefId | UserTaskDefId | ExternalEventDefId | WorkflowEventDefId | TaskRunId

export type SearchResponse =
  | WfSpecList
  | TaskDefList
  | UserTaskDefList
  | ExternalEventDefList
  | WorkflowEventDefList
  | TaskRunList
