'use client'

import { TypeDisplay } from '@/app/(authenticated)/[tenantId]/components/TypeDisplay'
import { formatDateReadable } from '@/app/utils'
import { IdentifierBadge } from '@/components/ui/badge'
import { TaskDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type Props = {
  spec: TaskDef
}

export const TaskDefHeader: FC<Props> = ({ spec }) => {
  const outputType = spec.returnType

  return (
    <div className="mb-4 space-y-1">
      <span className="italic">TaskDef</span>
      <h1 className="block text-2xl font-bold">{spec.id?.name}</h1>
      {spec.description && <p className="text-sm text-gray-600">{spec.description}</p>}
      {spec.createdAt && (
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <span className="font-semibold text-gray-700">Created:</span>
          <span>{formatDateReadable(spec.createdAt)}</span>
        </div>
      )}
      <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-gray-600">
        <span className="font-semibold text-gray-700">Output Type:</span>
        {!outputType ? (
          <span className="font-mono text-gray-400">Unknown Output Type</span>
        ) : (
          <TypeDisplay definedType={outputType.returnType?.definedType} />
        )}
      </div>
      <div className="mt-2">
        <h2 className="text-md mb-2 font-bold">Input</h2>
        {spec.inputVars.length === 0 ? (
          <div className="text-sm italic text-gray-600">No input variables</div>
        ) : (
          <div className="space-y-1">
            {spec.inputVars.map(varDef => (
              <div key={varDef.name} className="flex items-center gap-1">
                <IdentifierBadge name={varDef.name} />
                <TypeDisplay definedType={varDef.typeDef?.definedType} />
                {varDef.defaultValue && (
                  <div className="text-sm text-gray-600">{Object.values(varDef.defaultValue)[0]}</div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
