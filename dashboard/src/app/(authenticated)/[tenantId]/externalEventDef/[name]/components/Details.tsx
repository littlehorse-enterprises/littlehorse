'use client'
import { ExternalEventDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type DetailsProps = {
  spec: ExternalEventDef
}

export const Details: FC<DetailsProps> = ({ spec: { id, retentionPolicy } }) => {
  return (
    <div className="mb-4">
      <span className="italic">ExternalEventDef</span>
      <h1 className="block text-2xl font-bold">{id?.name}</h1>
      {retentionPolicy && retentionPolicy.extEvtGcPolicy && (
        <div className="flex items-center gap-2">
          Retention Policy:
          <span className="font-mono text-gray-400">{retentionPolicy.extEvtGcPolicy.value} seconds</span>
        </div>
      )}
    </div>
  )
}
