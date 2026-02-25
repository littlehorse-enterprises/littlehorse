'use client'
import { TypeDisplay } from '@/app/(authenticated)/[tenantId]/components/TypeDisplay'
import { WorkflowEventDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type DetailsProps = {
  spec: WorkflowEventDef
}

export const Details: FC<DetailsProps> = ({ spec: { id, contentType } }) => {
  return (
    <div className="mb-4 space-y-1">
      <span className="italic">WorkflowEventDef</span>
      <h1 className="block text-2xl font-bold">{id?.name}</h1>
      <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-gray-600">
        <span className="font-semibold text-gray-700">Output Type:</span>
        {!contentType ? (
          <span className="font-mono text-gray-400">Unknown Output Type</span>
        ) : (
          <TypeDisplay definedType={contentType.returnType?.definedType} />
        )}
      </div>
    </div>
  )
}
