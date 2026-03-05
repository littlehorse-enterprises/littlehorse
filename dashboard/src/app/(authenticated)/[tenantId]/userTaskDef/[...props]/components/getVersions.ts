'use server'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { lhClient } from '@/app/lhClient'
import { VersionList, WithTenant } from '@/types'

type Props = {
  name: string
} & WithTenant

export const getVersions = async (props: Props): Promise<VersionList> => {
  const { tenantId, name } = props
  const client = await lhClient({ tenantId })

  const allVersions: string[] = []
  let bookmark: Buffer | undefined

  do {
    const specs = await client.searchUserTaskDef({
      userTaskDefCriteria: { $case: 'name', value: name },
      bookmark,
      limit: SEARCH_DEFAULT_LIMIT,
    })

    const versions = specs.results.map(({ version }) => version.toString())
    allVersions.push(...versions)
    bookmark = specs.bookmark ?? undefined
  } while (bookmark && bookmark.length > 0)

  return { versions: allVersions }
}
