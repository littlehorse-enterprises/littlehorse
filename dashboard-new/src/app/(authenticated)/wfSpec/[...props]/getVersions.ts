'use server'
import { authOptions } from '@/app/api/auth/[...nextauth]/authOptions'
import { getClient } from '@/lhConfig'
import { WithBookmark } from '@/types'
import { getServerSession } from 'next-auth'

type GetWfSpecProps = {
  name: string
} & WithBookmark

type VersionList = {
  versions: string[]
  bookmark?: string
}

const VERSION_LIMIT = 10

export const getWfSpecVersions = async (props: GetWfSpecProps): Promise<VersionList> => {
  const session = await getServerSession(authOptions)
  const { tenantId, name } = props
  const bookmark = props.bookmark ? Buffer.from(props.bookmark) : undefined
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  const specs = await client.searchWfSpec({ name, bookmark, limit: VERSION_LIMIT })

  const versions = specs.results.map(({ majorVersion, revision }) => {
    return `${majorVersion}.${revision}`
  })

  return {
    bookmark: specs.bookmark?.toString(),
    versions,
  }
}
