'use client'
import { ExternalEventDef } from 'littlehorse-client/dist/proto/external_event'
import { TaskDef } from 'littlehorse-client/dist/proto/task_def'
import { FC } from 'react'

type DetailsProps = {
  spec: ExternalEventDef
}

export const Details: FC<DetailsProps> = ({ spec: { name, retentionPolicy } }) => {
  return (
    <div className="mb-4">
      <span className="italic">ExternalEventDef</span>
      <h1 className="block text-2xl font-bold">{name}</h1>
      {retentionPolicy?.secondsAfterPut && (
        <div className="flex items-center gap-2">
          Retention Policy:
          <span className="font-mono text-gray-400">{retentionPolicy.secondsAfterPut} seconds</span>
        </div>
      )}
    </div>
  )
}
