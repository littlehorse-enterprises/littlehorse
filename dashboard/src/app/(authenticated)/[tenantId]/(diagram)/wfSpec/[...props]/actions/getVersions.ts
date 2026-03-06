'use server'
import { authOptions } from '@/app/api/auth/[...nextauth]/authOptions'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { getClient } from '@/lhConfig'
import { VersionList, WithTenant } from '@/types'
import { getServerSession } from 'next-auth'

type GetWfSpecProps = {
  name: string
} & WithTenant

export const getWfSpecVersions = async (props: GetWfSpecProps): Promise<VersionList> => {
  const session = await getServerSession(authOptions)
  const { tenantId, name } = props
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  const allVersions: string[] = []
  let bookmark: Buffer | undefined

  do {
    const specs = await client.searchWfSpec({
      wfSpecCriteria: { $case: 'name', value: name },
      bookmark,
      limit: SEARCH_DEFAULT_LIMIT,
    })

    const versions = specs.results.map(({ majorVersion, revision }) => `${majorVersion}.${revision}`)
    allVersions.push(...versions)
    bookmark = specs.bookmark ?? undefined
  } while (bookmark && bookmark.length > 0)

  return { versions: allVersions }
}
