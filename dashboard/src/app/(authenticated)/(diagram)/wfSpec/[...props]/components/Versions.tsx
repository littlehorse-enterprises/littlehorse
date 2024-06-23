import { VersionSelector } from '@/app/(authenticated)/components/VersionSelector'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { WfSpecId } from 'littlehorse-client/proto'
import { useParams } from 'next/navigation'
import { FC, useCallback, useState } from 'react'
import { getWfSpecVersions } from '../actions/getVersions'

export const Versions: FC<{ wfSpecId?: WfSpecId }> = ({ wfSpecId }) => {
  const [versions, setVersions] = useState<string[]>([])
  const { tenantId } = useWhoAmI()
  const { name, majorVersion, revision } = wfSpecId!
  const { props } = useParams()

  const loadVersions = useCallback(async () => {
    const { versions } = await getWfSpecVersions({ name, tenantId })
    setVersions(versions)
  }, [name, tenantId])

  return (
    <VersionSelector
      path={`/wfSpec/${props[0]}`}
      currentVersion={`${majorVersion}.${revision}`}
      versions={versions}
      loadVersions={loadVersions}
    />
  )
}
