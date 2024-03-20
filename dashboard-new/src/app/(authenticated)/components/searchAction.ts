'use server'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { lhClient } from '@/app/lhClient'
import { WithBookmark } from '@/types'
import {
  ExternalEventDefIdList,
  TaskDefIdList,
  UserTaskDefIdList,
  WfSpecIdList,
} from 'littlehorse-client/dist/proto/service'
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

const searchExternalEventDef = async ({
  prefix,
  bookmark,
  limit,
  tenantId,
}: Props): Promise<ExternalEventDefIdList> => {
  const client = await lhClient({ tenantId })

  return client.searchExternalEventDef({
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

export const search = async ({ type, ...props }: SearchProps): Promise<SearchResponse> => {
  let results
  switch (type) {
    case 'TaskDef':
      results = await searchTaskDef(props)
      break
    case 'UserTaskDef':
      results = await searchUserTaskDef(props)
      break
    case 'ExternalEventDef':
      results = await searchExternalEventDef(props)
      break
    default:
      results = await searchWfSpec(props)
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
  type: 'WfSpec'
  results: Pick<WfSpecIdList, 'results'>
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

export type SearchResponse = WfSpecList | TaskDefList | UserTaskDefList | ExternalEventDefList | SearchResult
