import { VariableDisplayContext } from '@/app/utils'
import { getComparatorLabel, getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { Comparator, Edge as EdgeProto, VariableAssignment, VariableValue } from 'littlehorse-client/proto'
import { Equal, GitBranch } from 'lucide-react'
import { FC, useMemo } from 'react'
import type { EdgeData } from './Default'
import {
  formatNodeOutputSourceLabel,
  getNodeOutputNodeName,
  getOperandDisplayText,
  hasResolvedNodeOutput,
  parseEdgeCondition,
} from './edgeConditionDisplay'

const specBadgeClass =
  'rounded-md border border-fuchsia-200 bg-fuchsia-50 px-2 py-1 font-mono text-[10px] text-fuchsia-700'

const edgeLabelCardClass =
  'w-fit rounded-lg border border-violet-200/90 bg-white shadow-md ring-1 ring-violet-100/80'

const edgeLabelHeaderClass =
  'flex items-center gap-1 border-b border-violet-100 bg-gradient-to-r from-violet-50 to-indigo-50 px-2 py-1'

/** Shared height/typography for panel header strips (task output vs compared-to). */
const panelHeaderClass =
  'flex h-5 items-center gap-1 whitespace-nowrap border-b px-2 text-[8px] font-semibold uppercase leading-none tracking-wide'

const branchLabelClass = {
  true: 'min-w-[3.25rem] rounded-md border border-emerald-500 bg-emerald-600 px-3 py-1 text-center text-[11px] font-bold uppercase tracking-wider text-white shadow-md ring-1 ring-emerald-400/50',
  false:
    'min-w-[3.25rem] rounded-md border border-slate-500 bg-slate-600 px-3 py-1 text-center text-[11px] font-bold uppercase tracking-wider text-white shadow-md ring-1 ring-slate-400/50',
} as const

const SpecOperand: FC<{
  assignment: VariableAssignment
  displayContext?: VariableDisplayContext
}> = ({ assignment, displayContext }) => {
  const text = getOperandDisplayText(assignment, displayContext)
  const nodeName = getNodeOutputNodeName(assignment)
  const isLiteral = assignment.source?.$case === 'literalValue'

  if (assignment.source?.$case === 'nodeOutput' && nodeName) {
    return (
      <span className="inline-flex flex-col items-start gap-0.5" title={`Output of node ${nodeName}`}>
        <span className="text-[8px] font-semibold uppercase tracking-wide text-fuchsia-600">Node output</span>
        <span className="rounded bg-fuchsia-100 px-1.5 py-0.5 font-mono text-[10px] text-fuchsia-800">
          {formatNodeOutputSourceLabel(nodeName)}
        </span>
      </span>
    )
  }

  return (
    <span
      className={
        isLiteral
          ? 'rounded-md border border-slate-200 bg-slate-50 px-2 py-1 font-mono text-[10px] text-slate-700'
          : specBadgeClass
      }
      title={text}
    >
      {text}
    </span>
  )
}

/** WfRun view: task output compared to an expected value. */
const RuntimeComparisonCard: FC<{
  leftOperand: VariableAssignment
  rightOperand?: VariableAssignment
  comparator: Comparator
  displayContext?: VariableDisplayContext
}> = ({ leftOperand, rightOperand, comparator, displayContext }) => {
  const nodeName = getNodeOutputNodeName(leftOperand) ?? ''
  const actualValue = getOperandDisplayText(leftOperand, displayContext)
  const expectedValue = rightOperand ? getOperandDisplayText(rightOperand, displayContext) : ''
  const opLabel = getComparatorLabel(comparator)
  const opSymbol = getComparatorSymbol(comparator)

  return (
    <div
      className={`${edgeLabelCardClass} min-w-[140px] max-w-xs`}
      title={`If ${nodeName} output ${opLabel} ${expectedValue}`}
    >
      <div className={edgeLabelHeaderClass}>
        <GitBranch className="h-3 w-3 shrink-0 text-violet-600" aria-hidden />
        <span className="text-[9px] font-bold uppercase tracking-wider text-violet-800">If</span>
      </div>

      <div className="space-y-2 px-2 py-2">
        <section>
          <div className="rounded-md border border-emerald-200/90 bg-emerald-50/60">
            <div className={`${panelHeaderClass} border-emerald-200/70 bg-emerald-100/40 text-emerald-800`}>
              <span className="shrink-0">Task output</span>
              <span className="shrink-0 font-normal text-emerald-500/70" aria-hidden>
                ·
              </span>
              <span
                className="inline-flex shrink-0 items-center rounded-full bg-emerald-600 px-1.5 py-px font-mono text-[9px] font-medium normal-case tracking-normal text-white"
                title={nodeName}
              >
                {nodeName}
              </span>
            </div>
            <p
              className="whitespace-pre-wrap break-words px-2 py-1.5 font-mono text-[10px] leading-snug text-emerald-950"
              title={actualValue}
            >
              {actualValue}
            </p>
          </div>
        </section>

        <div className="flex items-center gap-1" aria-label={`${opLabel} (${opSymbol})`}>
          <div className="h-px flex-1 bg-gradient-to-r from-transparent via-gray-200 to-gray-300" />
          <span className="inline-flex items-center gap-0.5 rounded-full border border-violet-200 bg-violet-50 px-2 py-0.5 text-[9px] font-semibold text-violet-800">
            {comparator === Comparator.EQUALS ? <Equal className="h-2.5 w-2.5" aria-hidden /> : null}
            {opLabel}
          </span>
          <div className="h-px flex-1 bg-gradient-to-l from-transparent via-gray-200 to-gray-300" />
        </div>

        {rightOperand != null && (
          <section>
            <div className="rounded-md border border-slate-200/90 bg-slate-50/80">
              <div className={`${panelHeaderClass} border-slate-200/80 bg-slate-100/60 text-slate-600`}>
                <span>Compared to</span>
              </div>
              <p
                className="whitespace-pre-wrap break-words px-2 py-1.5 font-mono text-[10px] leading-snug text-slate-800"
                title={expectedValue}
              >
                {expectedValue}
              </p>
            </div>
          </section>
        )}
      </div>
    </div>
  )
}

const InlineComparison: FC<{
  leftOperand?: VariableAssignment
  rightOperand?: VariableAssignment
  operatorSymbol: string
  comparator?: Comparator
  displayContext?: VariableDisplayContext
}> = ({ leftOperand, rightOperand, operatorSymbol, comparator, displayContext }) => {
  const opLabel = comparator != null ? getComparatorLabel(comparator) : operatorSymbol

  return (
    <div className="flex max-w-[240px] flex-col gap-1.5 rounded-lg border border-violet-200/80 bg-white px-2 py-2 shadow-sm">
      <div className="flex items-center gap-1">
        <GitBranch className="h-3 w-3 text-violet-600" aria-hidden />
        <span className="text-[9px] font-bold uppercase tracking-wider text-violet-800">If</span>
      </div>
      <div className="flex flex-wrap items-center justify-center gap-1.5">
        {leftOperand != null ? <SpecOperand assignment={leftOperand} displayContext={displayContext} /> : null}
        <span className="rounded-full bg-violet-100 px-1.5 py-0.5 text-[9px] font-semibold text-violet-800">
          {opLabel}
        </span>
        {rightOperand != null ? <SpecOperand assignment={rightOperand} displayContext={displayContext} /> : null}
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
  if (leftOperand != null && comparator != null && hasResolvedNodeOutput(leftOperand, displayContext)) {
    return (
      <RuntimeComparisonCard
        leftOperand={leftOperand}
        rightOperand={rightOperand}
        comparator={comparator}
        displayContext={displayContext}
      />
    )
  }

  return (
    <InlineComparison
      leftOperand={leftOperand}
      rightOperand={rightOperand}
      operatorSymbol={operatorSymbol}
      comparator={comparator}
      displayContext={displayContext}
    />
  )
}

export const EdgeBranchLabel: FC<{ branch: 'true' | 'false'; fade?: boolean }> = ({ branch, fade }) => (
  <span className={`${branchLabelClass[branch]} ${fade ? 'opacity-25' : 'opacity-100'}`}>{branch}</span>
)

/** Condition card rendered on NOP branch nodes. */
export const NopConditionCard: FC<{
  edge: Pick<EdgeProto, 'edgeCondition'>
  nodeOutputValues?: Record<string, VariableValue>
}> = ({ edge, nodeOutputValues }) => {
  const displayContext = useMemo(
    () => (nodeOutputValues && Object.keys(nodeOutputValues).length > 0 ? { nodeOutputValues } : undefined),
    [nodeOutputValues]
  )
  const parsed = parseEdgeCondition(edge.edgeCondition)
  if (!parsed) return null

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

export const EdgeConditionLabel: FC<{ edge: EdgeData | EdgeProto }> = ({ edge }) => {
  const nodeOutputValues = 'nodeOutputValues' in edge ? edge.nodeOutputValues : undefined
  const displayContext = useMemo(
    () => (nodeOutputValues && Object.keys(nodeOutputValues).length > 0 ? { nodeOutputValues } : undefined),
    [nodeOutputValues]
  )

  const parsed = parseEdgeCondition(edge.edgeCondition)
  if (!parsed) {
    const { edgeCondition } = edge
    if (edgeCondition?.$case === 'condition' && edgeCondition.value) {
      return <span className={specBadgeClass}>{getOperandDisplayText(edgeCondition.value, displayContext)}</span>
    }
    return null
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
