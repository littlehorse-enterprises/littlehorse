'use server'
import { lhClient } from '@/app/lhClient'
import { WithBookmark } from '@/types'
import { TaskDefIdList, UserTaskDefIdList, WfSpecIdList } from 'littlehorse-client/dist/proto/service'
import { SearchType } from './Search'

type Props = { prefix?: string } & WithBookmark
const LIMIT = 10

const searchWfSpec = async ({ prefix, tenantId, bookmark }: Props): Promise<WfSpecIdList> => {
  const client = await lhClient({ tenantId })
  return client.searchWfSpec({
    prefix,
    bookmark: bookmark ? Buffer.from(bookmark) : undefined,
    limit: LIMIT,
  })
}

const searchTaskDef = async ({ prefix, tenantId, bookmark }: Props): Promise<TaskDefIdList> => {
  const client = await lhClient({ tenantId })

  return client.searchTaskDef({
    prefix,
    bookmark: bookmark ? Buffer.from(bookmark) : undefined,
    limit: LIMIT,
  })
}

const searchUserTaskDef = async ({ prefix, tenantId, bookmark }: Props): Promise<UserTaskDefIdList> => {
  const client = await lhClient({ tenantId })
  return client.searchUserTaskDef({
    prefix,
    bookmark: bookmark ? Buffer.from(bookmark) : undefined,
    limit: LIMIT,
  })
}

export const search = async ({ type, prefix, bookmark, tenantId }: SearchProps): Promise<SearchResponse> => {
  let results: Results
  switch (type) {
    case 'taskDef':
      results = await searchTaskDef({ prefix, tenantId, bookmark })
      break
    case 'userTaskDef':
      results = await searchUserTaskDef({ prefix, tenantId, bookmark })
      break
    default:
      results = await searchWfSpec({ prefix, tenantId, bookmark })
      break
  }
  return {
    ...results,
    type,
    bookmark: results.bookmark?.toString('base64'),
  }
}

type SearchProps = { type: SearchType; prefix?: string } & WithBookmark
type Results = WfSpecIdList | TaskDefIdList

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
