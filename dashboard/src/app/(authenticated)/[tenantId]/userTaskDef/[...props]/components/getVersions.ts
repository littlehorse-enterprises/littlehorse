'use server'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { lhClient } from '@/app/lhClient'
import { VersionList, WithBookmark, WithTenant } from '@/types'

type Props = {
  name: string
} & WithBookmark &
  WithTenant

export const getVersions = async (props: Props): Promise<VersionList> => {
  const { tenantId, name } = props
  const bookmark = props.bookmark ? Buffer.from(props.bookmark) : undefined
  const client = await lhClient({ tenantId })

  const specs = await client.searchUserTaskDef({
    userTaskDefCriteria: { $case: 'name', value: name },
    bookmark,
    limit: SEARCH_DEFAULT_LIMIT,
  })

  const versions = specs.results.map(({ version }) => {
    return version.toString()
  })

  return {
    bookmark: specs.bookmark?.toString('base64'),
    versions,
  }
}
