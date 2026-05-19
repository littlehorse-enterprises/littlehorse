'use client'

import { VARIABLE_TYPES } from '@/app/constants'
import { getVariableValue } from '@/app/utils'
import { IdentifierBadge, TypeBadge } from '@/components/ui/badge'
import { VariableValue } from 'littlehorse-client/proto'

export const VariableValuePillRow = ({
  varName,
  value,
  valueBreakWords = false,
}: {
  varName: string
  value: VariableValue
  valueBreakWords?: boolean
}) => {
  const variableType = value.value?.$case
  const text = getVariableValue(value)
  return (
    <div className="flex min-w-0 flex-wrap items-center gap-1">
      <IdentifierBadge name={varName} />
      {variableType && (
        <TypeBadge>{VARIABLE_TYPES[variableType as keyof typeof VARIABLE_TYPES] ?? variableType}</TypeBadge>
      )}
      <div className={`min-w-0 text-xs ${valueBreakWords ? 'max-w-full flex-1' : 'max-w-[16rem]'}`}>
        <p
          className={valueBreakWords ? 'break-words text-foreground' : 'truncate text-foreground'}
          title={valueBreakWords ? undefined : text}
        >
          {text}
        </p>
      </div>
    </div>
  )
}
