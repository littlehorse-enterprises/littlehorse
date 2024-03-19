'use server'
import { authOptions } from '@/app/api/auth/[...nextauth]/authOptions'
import { getClient } from '@/lhConfig'
import { WithBookmark } from '@/types'
import { TaskDefIdList, WfSpecIdList } from 'littlehorse-client/dist/proto/service'
import { getServerSession } from 'next-auth'
import { SearchType } from './Search'

type Props = {} & WithBookmark

const LIMIT = 10

const searchWfSpec = async ({ tenantId, bookmark }: Props): Promise<WfSpecIdList> => {
  const session = await getServerSession(authOptions)
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  return client.searchWfSpec({
    bookmark: bookmark ? Buffer.from(bookmark) : undefined,
    limit: LIMIT,
  })
}

const searchTaskDef = async ({ tenantId, bookmark }: Props): Promise<TaskDefIdList> => {
  const session = await getServerSession(authOptions)
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  return client.searchTaskDef({
    bookmark: bookmark ? Buffer.from(bookmark) : undefined,
    limit: LIMIT,
  })
}

type SearchProps = { type: SearchType } & WithBookmark
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

export type SearchResponse = WfSpecList | TaskDefList | SearchResult

export const search = async ({ type, bookmark, tenantId }: SearchProps): Promise<SearchResponse> => {
  let results: Results
  switch (type) {
    case 'taskDef':
      results = await searchTaskDef({ tenantId, bookmark })
      break
    default:
      results = await searchWfSpec({ tenantId, bookmark })
      break
  }
  return {
    ...results,
    type,
    bookmark: results.bookmark?.toString('base64'),
  }
}
