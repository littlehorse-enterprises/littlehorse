import { VersionSelector } from '@/app/(authenticated)/[tenantId]/components/VersionSelector'
import { routes } from '@/app/routes'
import { WfSpecId } from 'littlehorse-client/proto'
import { useParams } from 'next/navigation'
import { FC, useCallback, useState } from 'react'
import { getWfSpecVersions } from '../actions/getVersions'

export const Versions: FC<{ wfSpecId?: WfSpecId }> = ({ wfSpecId }) => {
  const [versions, setVersions] = useState<string[]>([])
  const { name, majorVersion, revision } = wfSpecId!
  const { props } = useParams()
  const tenantId = useParams().tenantId as string

  const loadVersions = useCallback(async () => {
    const { versions } = await getWfSpecVersions({ name, tenantId })
    setVersions(versions)
  }, [name, tenantId])

  return (
    <VersionSelector
      path={routes.wfSpec.base(props![0] as string)}
      currentVersion={`${majorVersion}.${revision}`}
      versions={versions}
      loadVersions={loadVersions}
    />
  )
}
