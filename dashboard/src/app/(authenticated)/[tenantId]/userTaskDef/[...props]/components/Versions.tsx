'use client'
import { VersionSelector } from '@/app/(authenticated)/[tenantId]/components/VersionSelector'
import { routes } from '@/app/routes'
import { UserTaskDefId } from 'littlehorse-client/proto'
import { useParams } from 'next/navigation'
import { FC, useCallback, useState } from 'react'
import { getVersions } from './getVersions'

export const Versions: FC<{ id?: UserTaskDefId }> = ({ id }) => {
  const [versions, setVersions] = useState<string[]>([])
  const tenantId = useParams().tenantId as string
  const { name, version } = id!
  const { props } = useParams()

  const loadVersions = useCallback(async () => {
    const { versions } = await getVersions({ name, tenantId })
    setVersions(versions)
  }, [name, tenantId])

  return (
    <VersionSelector
      path={routes.wfSpec.base(props![0] as string)}
      currentVersion={`${version}`}
      versions={versions}
      loadVersions={loadVersions}
    />
  )
}
