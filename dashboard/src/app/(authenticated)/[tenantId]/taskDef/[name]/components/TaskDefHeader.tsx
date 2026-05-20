'use client'

import { TypeDisplay } from '@/app/(authenticated)/[tenantId]/components/TypeDisplay'
import { formatDateReadable, getVariableValue } from '@/app/utils'
import { Badge, IdentifierBadge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { cn } from '@/components/utils'
import { TaskDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type Props = {
  spec: TaskDef
  className?: string
}

export const TaskDefHeader: FC<Props> = ({ spec, className }) => {
  const inputVars = spec.inputVars ?? []
  const taskDefName = spec.id?.name ?? ''

  return (
    <Card className={cn('flex h-full min-h-0 flex-col', className)}>
      <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
        <div className="min-w-0 pr-2">
          <p className="text-xs italic text-muted-foreground">TaskDef</p>
          <CardTitle className="text-base font-medium">{taskDefName}</CardTitle>
          {spec.description && <CardDescription className="text-xs">{spec.description}</CardDescription>}
          {spec.createdAt && (
            <CardDescription className="text-xs">Created {formatDateReadable(spec.createdAt)}</CardDescription>
          )}
        </div>
      </CardHeader>
      <CardContent className="mt-auto flex flex-1 flex-col justify-end">
        <div className="flex flex-wrap items-center gap-1 font-mono text-sm">
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
                  <Badge className="bg-green-100 font-mono text-xs">
                    {getVariableValue(varDef.defaultValue)}
                  </Badge>
                </>
              )}
            </span>
          ))}
          <span className="text-gray-500">)</span>
          <span className="text-gray-500">:</span>
          <TypeDisplay definedType={spec.returnType?.returnType?.definedType} />
        </div>
      </CardContent>
    </Card>
  )
}
