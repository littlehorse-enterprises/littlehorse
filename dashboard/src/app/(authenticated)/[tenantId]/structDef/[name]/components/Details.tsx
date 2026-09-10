'use client'
import { FC } from 'react'
import { StructDef } from 'littlehorse-client/proto'
import VersionTag from '@/app/(authenticated)/[tenantId]/components/VersionTag'
import { OverflowText } from '../../../components/OverflowText'

type DetailsProps = Pick<StructDef, 'id' | 'description'>

export const Details: FC<DetailsProps> = ({ id, description }) => {
  return (
    <div className="mb-4">
      <span className="italic">StructDef</span>
      <div className="flex items-center gap-2">
        <h1 className="block text-2xl font-bold">{id?.name}</h1>
        {id?.version !== undefined && <VersionTag label={`v${id.version}`} />}
      </div>
      {description && (
        <OverflowText prose clampLines={1} className="max-w-3xl text-sm text-muted-foreground" text={description} />
      )}
    </div>
  )
}
