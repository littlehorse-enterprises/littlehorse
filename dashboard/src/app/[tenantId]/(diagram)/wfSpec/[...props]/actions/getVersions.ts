'use server'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { getClient } from '@/lhConfig'
import { VersionList, WithBookmark, WithTenant } from '@/types'
import { auth } from '@/auth'

type GetWfSpecProps = {
  name: string
} & WithBookmark &
  WithTenant

export const getWfSpecVersions = async (props: GetWfSpecProps): Promise<VersionList> => {
  const session = await auth()
  const { tenantId, name } = props
  const bookmark = props.bookmark ? Buffer.from(props.bookmark) : undefined
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  const specs = await client.searchWfSpec({ name, bookmark, limit: SEARCH_DEFAULT_LIMIT })

  const versions = specs.results.map(({ majorVersion, revision }) => {
    return `${majorVersion}.${revision}`
  })

  return {
    bookmark: specs.bookmark?.toString('base64'),
    versions,
  }
}
