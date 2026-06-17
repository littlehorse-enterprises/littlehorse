import { VariableDisplayContext } from '@/app/utils'
import { getComparatorLabel, getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { Comparator, Edge as EdgeProto, VariableAssignment, VariableValue } from 'littlehorse-client/proto'
import { GitBranch } from 'lucide-react'
import { FC, useMemo } from 'react'
import type { EdgeData } from './Default'
import {
  formatNodeOutputSourceLabel,
  getEdgeOperandDisplayText,
  getNodeOutputNodeName,
  getTruthyConditionDisplayText,
  hasResolvedNodeOutput,
  parseEdgeCondition,
} from './edgeConditionDisplay'

const compactCardClass = 'w-fit max-w-[176px] rounded-md border border-violet-200 bg-white px-1.5 py-1'

const operandChipClass = {
  source: 'rounded bg-fuchsia-50 px-1 py-px font-mono text-[9px] leading-tight text-fuchsia-800 ring-1 ring-fuchsia-200/80',
  output:
    'rounded bg-emerald-50 px-1 py-px font-mono text-[9px] leading-tight text-emerald-800 ring-1 ring-emerald-200/80',
  literal: 'rounded bg-slate-50 px-1 py-px font-mono text-[9px] leading-tight text-slate-700 ring-1 ring-slate-200/80',
} as const

const OperandChip: FC<{
  text: string
  variant: keyof typeof operandChipClass
  title?: string
}> = ({ text, variant, title }) => (
  <span className={`max-w-[5.5rem] truncate ${operandChipClass[variant]}`} title={title ?? text}>
    {text}
  </span>
)

const getLeftOperandPresentation = (
  assignment: VariableAssignment,
  displayContext?: VariableDisplayContext
): { text: string; variant: keyof typeof operandChipClass; title: string } => {
  const nodeName = getNodeOutputNodeName(assignment)
  const fullText = getEdgeOperandDisplayText(assignment, displayContext)

  if (assignment.source?.$case === 'nodeOutput' && nodeName) {
    const taskLabel = formatNodeOutputSourceLabel(nodeName)
    if (hasResolvedNodeOutput(assignment, displayContext)) {
      return {
        text: fullText,
        variant: 'output',
        title: `Output of ${taskLabel} (${nodeName}): ${fullText}`,
      }
    }
    return {
      text: taskLabel,
      variant: 'output',
      title: `Output of ${taskLabel} (${nodeName})`,
    }
  }

  if (assignment.source?.$case === 'literalValue') {
    return { text: fullText, variant: 'literal', title: fullText }
  }

  return { text: fullText, variant: 'source', title: fullText }
}

const CompactComparisonCard: FC<{
  leftOperand: VariableAssignment
  rightOperand?: VariableAssignment
  comparator: Comparator
  displayContext?: VariableDisplayContext
}> = ({ leftOperand, rightOperand, comparator, displayContext }) => {
  const left = getLeftOperandPresentation(leftOperand, displayContext)
  const opSymbol = getComparatorSymbol(comparator)
  const opLabel = getComparatorLabel(comparator)
  const rightText = rightOperand ? getEdgeOperandDisplayText(rightOperand, displayContext) : ''
  const rightVariant = rightOperand?.source?.$case === 'literalValue' ? 'literal' : 'source'

  return (
    <div
      className={compactCardClass}
      title={`If ${left.title} ${opLabel}${rightText ? ` ${rightText}` : ''}`}
    >
      <div className="flex items-center gap-1">
        <GitBranch className="h-2.5 w-2.5 shrink-0 text-violet-600" aria-hidden />
        <span className="text-[8px] font-bold uppercase tracking-wide text-violet-800">If</span>
        <OperandChip text={left.text} variant={left.variant} title={left.title} />
        <span
          className="shrink-0 rounded-full bg-violet-100 px-1 py-px text-[8px] font-semibold leading-none text-violet-800"
          aria-label={opLabel}
        >
          {opSymbol}
        </span>
        {rightOperand != null ? (
          <OperandChip text={rightText} variant={rightVariant} title={rightText} />
        ) : null}
      </div>
    </div>
  )
}

const TruthinessCard: FC<{
  operand: VariableAssignment
  displayContext?: VariableDisplayContext
}> = ({ operand, displayContext }) => {
  const text = getTruthyConditionDisplayText(operand, displayContext)
  const variant: keyof typeof operandChipClass =
    operand.source?.$case === 'nodeOutput'
      ? 'output'
      : operand.source?.$case === 'literalValue'
        ? 'literal'
        : 'source'

  return (
    <div className={compactCardClass} title={`When ${text} is true`}>
      <div className="flex items-center gap-1">
        <GitBranch className="h-2.5 w-2.5 shrink-0 text-violet-600" aria-hidden />
        <span className="text-[8px] font-bold uppercase tracking-wide text-violet-800">If</span>
        <OperandChip text={text} variant={variant} title={text} />
      </div>
    </div>
  )
}

const ComparisonLabel: FC<{
  leftOperand?: VariableAssignment
  rightOperand?: VariableAssignment
  operatorSymbol: string
  comparator?: Comparator
  displayContext?: VariableDisplayContext
}> = ({ leftOperand, rightOperand, operatorSymbol, comparator, displayContext }) => {
  if (leftOperand != null && comparator != null) {
    return (
      <CompactComparisonCard
        leftOperand={leftOperand}
        rightOperand={rightOperand}
        comparator={comparator}
        displayContext={displayContext}
      />
    )
  }

  const opLabel = comparator != null ? getComparatorLabel(comparator) : operatorSymbol

  return (
    <div className={compactCardClass}>
      <div className="flex flex-wrap items-center gap-1">
        <GitBranch className="h-2.5 w-2.5 shrink-0 text-violet-600" aria-hidden />
        <span className="text-[8px] font-bold uppercase tracking-wide text-violet-800">If</span>
        {leftOperand != null ? (
          <OperandChip
            text={getEdgeOperandDisplayText(leftOperand, displayContext)}
            variant="source"
            title={getEdgeOperandDisplayText(leftOperand, displayContext)}
          />
        ) : null}
        <span className="rounded-full bg-violet-100 px-1 py-px text-[8px] font-semibold text-violet-800">{opLabel}</span>
        {rightOperand != null ? (
          <OperandChip
            text={getEdgeOperandDisplayText(rightOperand, displayContext)}
            variant={rightOperand.source?.$case === 'literalValue' ? 'literal' : 'source'}
            title={getEdgeOperandDisplayText(rightOperand, displayContext)}
          />
        ) : null}
      </div>
    </div>
  )
}

export const EdgeConditionLabel: FC<{ edge: EdgeData | EdgeProto }> = ({ edge }) => {
  const nodeOutputValues = 'nodeOutputValues' in edge ? edge.nodeOutputValues : undefined
  const displayContext = useMemo(
    () => (nodeOutputValues && Object.keys(nodeOutputValues).length > 0 ? { nodeOutputValues } : undefined),
    [nodeOutputValues]
  )

  const parsed = parseEdgeCondition(edge.edgeCondition)
  if (!parsed) return null

  if (parsed.isTruthyCheck && parsed.leftOperand) {
    return <TruthinessCard operand={parsed.leftOperand} displayContext={displayContext} />
  }

  return (
    <ComparisonLabel
      leftOperand={parsed.leftOperand}
      rightOperand={parsed.rightOperand}
      operatorSymbol={parsed.operatorSymbol}
      comparator={parsed.comparator}
      displayContext={displayContext}
    />
  )
}
