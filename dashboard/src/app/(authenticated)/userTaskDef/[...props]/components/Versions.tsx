import { VersionSelector } from '@/app/(authenticated)/components/VersionSelector'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { UserTaskDefId } from 'littlehorse-client'
import { useParams } from 'next/navigation'
import { FC, useCallback, useState } from 'react'
import { getVersions } from './getVersions'

export const Versions: FC<{ id?: UserTaskDefId }> = ({ id }) => {
  const [versions, setVersions] = useState<string[]>([])
  const { tenantId } = useWhoAmI()
  const { name, version } = id!
  const { props } = useParams()

  const loadVersions = useCallback(async () => {
    const { versions } = await getVersions({ name, tenantId })
    setVersions(versions)
  }, [name, tenantId])

  return (
    <VersionSelector
      path={`/wfSpec/${props[0]}`}
      currentVersion={`${version}`}
      versions={versions}
      loadVersions={loadVersions}
    />
  )
}
