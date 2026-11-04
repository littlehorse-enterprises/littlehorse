import { getComparatorLabel, getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { tryFormatAsJson } from '@/app/utils/tryFormatAsJson'
import { VariableDisplayContext } from '@/app/utils/variables'
import { VariableAssignment } from 'littlehorse-client/proto'
import { FC, useMemo } from 'react'
import { CopyButton } from '../../../components/CopyButton'
import {
  formatNodeOutputSourceLabel,
  getEdgeOperandDisplayText,
  getNodeOutputNodeName,
  hasResolvedNodeOutput,
  parseEdgeCondition,
} from './edgeConditionDisplay'
import type { DiagramEdgeData } from './extractEdges'

const OperandDetail: FC<{
  label: string
  assignment: VariableAssignment
  context?: VariableDisplayContext
}> = ({ label, assignment, context }) => {
  const nodeName = getNodeOutputNodeName(assignment)
  const displayText = getEdgeOperandDisplayText(assignment, context)
  const resolved = hasResolvedNodeOutput(assignment, context)
  const sourceLabel = nodeName ? formatNodeOutputSourceLabel(nodeName) : undefined
  return (
    <div className="flex flex-col gap-2">
      <small className="text-[0.75em] text-slate-400">{label}</small>
      <div className="relative border border-slate-200 bg-slate-50 p-2 pr-8">
        <CopyButton className="absolute right-2 top-2 h-4 w-4 text-slate-400" value={displayText} />
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

export const EdgeConditionDetail: FC<{ edge: DiagramEdgeData }> = ({ edge }) => {
  const context = useMemo(
    () => (edge.nodeOutputValues ? { nodeOutputValues: edge.nodeOutputValues } : undefined),
    [edge.nodeOutputValues]
  )
  const parsed = parseEdgeCondition(edge.edgeCondition)
  if (!parsed) return null

  if (parsed.isTruthyCheck && parsed.leftOperand) {
    return (
      <div className="flex flex-col gap-4">
        <OperandDetail label="Left operand" assignment={parsed.leftOperand} context={context} />
        <div className="flex flex-col gap-1">
          <small className="text-[0.75em] text-slate-400">Comparator</small>
          <p className="font-medium">is true</p>
        </div>
      </div>
    )
  }

  const { leftOperand, rightOperand, comparator, operatorSymbol } = parsed
  if (!leftOperand || comparator === undefined) return null
  return (
    <div className="flex flex-col gap-4">
      <OperandDetail label="Left operand" assignment={leftOperand} context={context} />
      <div className="flex flex-col gap-1">
        <small className="text-[0.75em] text-slate-400">Comparator</small>
        <p className="font-medium">
          {getComparatorLabel(comparator)}{' '}
          <span className="font-mono text-sm text-slate-500">
            ({operatorSymbol || getComparatorSymbol(comparator)})
          </span>
        </p>
      </div>
      {rightOperand ? <OperandDetail label="Right operand" assignment={rightOperand} context={context} /> : null}
    </div>
  )
}
