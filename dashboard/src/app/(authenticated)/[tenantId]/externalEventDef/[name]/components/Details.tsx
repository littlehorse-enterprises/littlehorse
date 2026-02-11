'use client'
import { OutputTypeDisplay } from '@/app/(authenticated)/[tenantId]/components/OutputTypeDisplay'
import { ExternalEventDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type DetailsProps = {
  spec: ExternalEventDef
}

export const Details: FC<DetailsProps> = ({ spec: { id, retentionPolicy, typeInformation } }) => {
  return (
    <div className="mb-4 space-y-1">
      <span className="italic">ExternalEventDef</span>
      <h1 className="block text-2xl font-bold">{id?.name}</h1>
      <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-gray-600">
        <span className="font-semibold text-gray-700">Output Type:</span>
        <OutputTypeDisplay outputType={typeInformation} />
      </div>
      {retentionPolicy && retentionPolicy.extEvtGcPolicy && (
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <span className="font-semibold text-gray-700">Retention Policy:</span>
          <span className="font-mono text-gray-400">{retentionPolicy.extEvtGcPolicy.value} seconds</span>
        </div>
      )}
    </div>
  )
}
