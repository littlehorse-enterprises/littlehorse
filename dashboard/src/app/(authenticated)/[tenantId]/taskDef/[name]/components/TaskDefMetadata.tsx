'use client'

import { TypeDisplay } from '@/app/(authenticated)/[tenantId]/components/TypeDisplay'
import { formatDateReadable, getVariableValue } from '@/app/utils'
import { Badge, IdentifierBadge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { TaskDef } from 'littlehorse-client/proto'
import { FC, ReactNode } from 'react'

type Props = {
  spec: TaskDef
  actions?: ReactNode
}

const MetadataField: FC<{ label: string; children: ReactNode }> = ({ label, children }) => (
  <div className="rounded-md border border-gray-200 bg-white px-4 py-2.5">
    <p className="text-xs text-muted-foreground">{label}</p>
    <p className="mt-1 text-sm font-medium leading-snug">{children}</p>
  </div>
)

export const TaskDefMetadata: FC<Props> = ({ spec, actions }) => {
  const inputVars = spec.inputVars ?? []
  const taskDefName = spec.id?.name ?? ''

  return (
    <div className="mb-6">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <span className="text-sm italic text-muted-foreground">TaskDef</span>
          <h1 className="mt-1 text-2xl font-bold tracking-tight">{taskDefName}</h1>
          {spec.description && <p className="mt-1 text-sm text-muted-foreground">{spec.description}</p>}
          <div className="mt-2 flex flex-wrap items-center gap-1 font-mono text-sm">
            <span className="font-medium text-foreground">{taskDefName}</span>
            <span className="text-gray-500">(</span>
            {inputVars.map((varDef, index) => (
              <span key={varDef.name} className="inline-flex flex-wrap items-center gap-1">
                {index > 0 && <span className="text-gray-500">,</span>}
                <IdentifierBadge name={varDef.name} />
                <span className="text-gray-500">:</span>
                <TypeDisplay definedType={varDef.typeDef?.definedType} />
                {varDef.defaultValue && (
                  <>
                    <span className="text-gray-500">=</span>
                    <Badge className="bg-green-100 font-mono text-xs">{getVariableValue(varDef.defaultValue)}</Badge>
                  </>
                )}
              </span>
            ))}
            <span className="text-gray-500">)</span>
            <span className="text-gray-500">:</span>
            <TypeDisplay definedType={spec.returnType?.returnType?.definedType} />
          </div>
        </div>
        {actions}
      </div>

      <div className="mt-4 flex flex-wrap gap-3">
        <MetadataField label="Created">{spec.createdAt ? formatDateReadable(spec.createdAt) : '—'}</MetadataField>
        <MetadataField label="Inputs">{inputVars.length}</MetadataField>
        <MetadataField label="Return type">
          <TypeDisplay definedType={spec.returnType?.returnType?.definedType} />
        </MetadataField>
      </div>

      <Separator className="mt-4" />
    </div>
  )
}
