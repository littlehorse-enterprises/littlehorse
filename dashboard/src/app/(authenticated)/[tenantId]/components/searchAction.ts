'use server'
import { SEARCH_DEFAULT_LIMIT, SearchType } from '@/app/constants'
import { lhClient } from '@/app/lhClient'
import { WithBookmark, WithTenant } from '@/types'
import { WfSpecId, WfSpecIdList } from 'littlehorse-client/proto'

type Props = { prefix?: string; limit?: number } & WithBookmark

const genericSearch = async <P, R>(props: P, fn: (props: P) => Promise<R>): Promise<R> => {
  return fn(props)
}

type SearchProps = { type: SearchType } & Props & WithTenant

export const search = async ({ type, tenantId, bookmark, limit, prefix }: SearchProps): Promise<SearchResponse> => {
  const client = await lhClient({ tenantId })
  const request = {
    prefix,
    bookmark: bookmarkFrom(bookmark),
    limit: limit || SEARCH_DEFAULT_LIMIT,
  }

  let results
  switch (type) {
    case 'StructDef':
      results = await genericSearch(request, client.searchStructDef)
      break
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
  results: any
}

export type WfSpecList = SearchResult & {
  type: 'WfSpec'
  results: WfSpecId[]
}

type TaskDefList = SearchResult & {
  type: 'TaskDef'
  results: Pick<WfSpecIdList, 'results'>
}

type UserTaskDefList = SearchResult & {
  type: 'UserTaskDef'
  results: Pick<WfSpecIdList, 'results'>
}

type ExternalEventDefList = SearchResult & {
  type: 'ExternalEventDef'
  results: Pick<WfSpecIdList, 'results'>
}

type WorkflowEventDefList = SearchResult & {
  type: 'WorkflowEventDef'
  results: Pick<WfSpecIdList, 'results'>
}

export type StructDefList = SearchResult & {
  type: 'StructDef'
  results: Pick<WfSpecIdList, 'results'>
}
export type SearchResponse =
  | WfSpecList
  | TaskDefList
  | UserTaskDefList
  | ExternalEventDefList
  | WorkflowEventDefList
  | StructDefList
