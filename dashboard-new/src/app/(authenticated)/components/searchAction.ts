'use server'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { lhClient } from '@/app/lhClient'
import { WithBookmark } from '@/types'
import { TaskDefIdList, UserTaskDefIdList, WfSpecIdList } from 'littlehorse-client/dist/proto/service'
import { SearchType } from './Search'

type Props = { prefix?: string; limit?: number } & WithBookmark

const searchWfSpec = async ({ prefix, bookmark, limit, tenantId }: Props): Promise<WfSpecIdList> => {
  const client = await lhClient({ tenantId })
  return client.searchWfSpec({
    prefix,
    bookmark: bookmark ? Buffer.from(bookmark) : undefined,
    limit: limit || SEARCH_DEFAULT_LIMIT,
  })
}

const searchTaskDef = async ({ prefix, bookmark, limit, tenantId }: Props): Promise<TaskDefIdList> => {
  const client = await lhClient({ tenantId })

  return client.searchTaskDef({
    prefix,
    bookmark: bookmark ? Buffer.from(bookmark) : undefined,
    limit: limit || SEARCH_DEFAULT_LIMIT,
  })
}

type SearchProps = { type: SearchType } & Props
const searchUserTaskDef = async ({ prefix, bookmark, limit, tenantId }: Props): Promise<UserTaskDefIdList> => {
  const client = await lhClient({ tenantId })
  return client.searchUserTaskDef({
    prefix,
    bookmark: bookmark ? Buffer.from(bookmark) : undefined,
    limit: limit || SEARCH_DEFAULT_LIMIT,
  })
}

export const search = async ({ type, limit, prefix, bookmark, tenantId }: SearchProps): Promise<SearchResponse> => {
  let results
  switch (type) {
    case 'taskDef':
      results = await searchTaskDef({ prefix, bookmark, limit, tenantId })
      break
    case 'userTaskDef':
      results = await searchUserTaskDef({ prefix, bookmark, limit, tenantId })
      break
    default:
      results = await searchWfSpec({ prefix, bookmark, limit, tenantId })
      break
  }
  return {
    ...results,
    type,
    bookmark: results.bookmark?.toString('base64'),
  }
}

interface SearchResult {
  type: SearchType
  bookmark?: string
  results: any
}

type WfSpecList = SearchResult & {
  type: 'wfSpec'
  results: Pick<WfSpecIdList, 'results'>
}

type TaskDefList = SearchResult & {
  type: 'taskDef'
  results: Pick<WfSpecIdList, 'results'>
}

type UserTaskDefList = SearchResult & {
  type: 'userTaskDef'
  results: Pick<WfSpecIdList, 'results'>
}

export type SearchResponse = WfSpecList | TaskDefList | UserTaskDefList | SearchResult
