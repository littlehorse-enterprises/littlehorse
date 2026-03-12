import { getVariable } from '@/app/utils'
import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { Edge as EdgeProto } from 'littlehorse-client/proto'
import { FC } from 'react'

const variableBadgeClass = 'rounded px-1 py-0.5 text-[10px] font-mono bg-gray-100 text-fuchsia-500'

export const EdgeConditionLabel: FC<{ edge: EdgeProto }> = ({ edge }) => {
  const { edgeCondition } = edge
  if (!edgeCondition) return null

  if (edgeCondition.$case === 'legacyCondition') {
    const { left: leftOperand, right: rightOperand, comparator } = edgeCondition.value ?? {}
    const operatorSymbol = comparator != null ? getComparatorSymbol(comparator) : ''
    return (
      <span className="inline-flex items-center gap-1">
        {leftOperand != null ? <span className={variableBadgeClass}>{getVariable(leftOperand)}</span> : null}
        <span className="text-[10px] text-gray-500">{operatorSymbol}</span>
        {rightOperand != null ? <span className={variableBadgeClass}>{getVariable(rightOperand)}</span> : null}
      </span>
    )
  }

  if (edgeCondition.$case === 'condition') {
    const variableAssignment = edgeCondition.value
    if (!variableAssignment?.source) return null

    if (variableAssignment.source.$case === 'expression') {
      const expression = variableAssignment.source.value
      const { lhs: leftOperand, rhs: rightOperand, operation } = expression ?? {}
      if (!operation || operation.$case !== 'comparator') {
        return <span className={variableBadgeClass}>{getVariable(variableAssignment)}</span>
      }
      const operatorSymbol = getComparatorSymbol(operation.value)
      return (
        <span className="inline-flex items-center gap-1">
          {leftOperand != null ? <span className={variableBadgeClass}>{getVariable(leftOperand)}</span> : null}
          <span className="text-[10px] text-gray-500">{operatorSymbol}</span>
          {rightOperand != null ? <span className={variableBadgeClass}>{getVariable(rightOperand)}</span> : null}
        </span>
      )
    }

    return <span className={variableBadgeClass}>{getVariable(variableAssignment)}</span>
  }

  return null
}
