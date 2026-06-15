import { VariableDisplayContext } from '@/app/utils'
import { getComparatorLabel, getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { tryFormatAsJson } from '@/app/utils/tryFormatAsJson'
import { VariableAssignment } from 'littlehorse-client/proto'
import { FC, useMemo } from 'react'
import { CopyButton } from '../../../components/CopyButton'
import type { EdgeData } from './Default'
import {
  formatNodeOutputSourceLabel,
  getNodeOutputNodeName,
  getOperandDisplayText,
  hasResolvedNodeOutput,
  parseEdgeCondition,
} from './edgeConditionDisplay'

const OperandDetail: FC<{
  label: string
  assignment: VariableAssignment
  displayContext?: VariableDisplayContext
}> = ({ label, assignment, displayContext }) => {
  const nodeName = getNodeOutputNodeName(assignment)
  const displayText = getOperandDisplayText(assignment, displayContext)
  const resolved = hasResolvedNodeOutput(assignment, displayContext)
  const sourceLabel = nodeName ? formatNodeOutputSourceLabel(nodeName) : undefined

  return (
    <div className="flex flex-col gap-2">
      <small className="text-[0.75em] text-slate-400">{label}</small>
      <div className="flex flex-col border border-slate-200 bg-slate-50 p-2">
        <div className="flex w-full justify-end">
          <CopyButton className="h-4 w-4 text-slate-400" value={displayText} />
        </div>
        <p className="break-all font-mono text-sm">{tryFormatAsJson(displayText)}</p>
        {nodeName ? (
          <p className="mt-1 text-xs text-slate-500">
            {resolved ? `Output of ${sourceLabel} (${nodeName})` : `Task output from ${sourceLabel} (${nodeName})`}
          </p>
        ) : null}
      </div>
    </div>
  )
}

export const EdgeConditionDetail: FC<{ edge: EdgeData }> = ({ edge }) => {
  const displayContext = useMemo(
    () =>
      edge.nodeOutputValues && Object.keys(edge.nodeOutputValues).length > 0
        ? { nodeOutputValues: edge.nodeOutputValues }
        : undefined,
    [edge.nodeOutputValues]
  )

  const parsed = parseEdgeCondition(edge.edgeCondition)
  if (!parsed) {
    const { edgeCondition } = edge
    if (edgeCondition?.$case === 'condition' && edgeCondition.value) {
      return <OperandDetail label="Condition" assignment={edgeCondition.value} displayContext={displayContext} />
    }
    return null
  }

  const { leftOperand, rightOperand, comparator, operatorSymbol } = parsed
  if (!leftOperand || comparator == null) return null

  const opLabel = getComparatorLabel(comparator)

  return (
    <div className="flex flex-col gap-4">
      <OperandDetail label="Left operand" assignment={leftOperand} displayContext={displayContext} />
      <div className="flex flex-col gap-1">
        <small className="text-[0.75em] text-slate-400">Comparator</small>
        <p className="font-medium">
          {opLabel}{' '}
          <span className="font-mono text-sm text-slate-500">
            ({operatorSymbol || getComparatorSymbol(comparator)})
          </span>
        </p>
      </div>
      {rightOperand ? (
        <OperandDetail label="Right operand" assignment={rightOperand} displayContext={displayContext} />
      ) : null}
    </div>
  )
}
