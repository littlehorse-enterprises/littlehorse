'use server'
import { SEARCH_DEFAULT_LIMIT, SearchType } from '@/app/constants'
import { lhClient } from '@/app/lhClient'
import { WithBookmark, WithTenant } from '@/types'
import { WfSpecId, WfSpecIdList } from 'littlehorse-client/proto'

type Props = { prefix?: string; limit?: number } & WithBookmark

type SearchProps = { type: SearchType } & Props & WithTenant

export const search = async ({ type, tenantId, bookmark, limit, prefix }: SearchProps): Promise<SearchResponse> => {
  const client = await lhClient({ tenantId })
  const bookmarkBuf = bookmarkFrom(bookmark)
  const lim = limit || SEARCH_DEFAULT_LIMIT

  let results
  switch (type) {
    case 'StructDef':
      results = await client.searchStructDef({
        bookmark: bookmarkBuf,
        limit: lim,
        structDefCriteria: prefix ? { $case: 'prefix', value: prefix } : undefined,
      })
      break
    case 'TaskDef':
      results = await client.searchTaskDef({
        bookmark: bookmarkBuf,
        limit: lim,
        prefix,
      })
      break
    case 'UserTaskDef':
      results = await client.searchUserTaskDef({
        bookmark: bookmarkBuf,
        limit: lim,
        userTaskDefCriteria: prefix ? { $case: 'prefix', value: prefix } : undefined,
      })
      break
    case 'ExternalEventDef':
      results = await client.searchExternalEventDef({
        bookmark: bookmarkBuf,
        limit: lim,
        prefix,
      })
      break
    case 'WorkflowEventDef':
      results = await client.searchWorkflowEventDef({
        bookmark: bookmarkBuf,
        limit: lim,
        prefix,
      })
      break
    default:
      results = await client.searchWfSpec({
        bookmark: bookmarkBuf,
        limit: lim,
        wfSpecCriteria: prefix ? { $case: 'prefix', value: prefix } : undefined,
      })
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
