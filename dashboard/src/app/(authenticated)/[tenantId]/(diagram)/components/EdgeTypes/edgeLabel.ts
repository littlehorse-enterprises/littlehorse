import { getComparatorLabel } from '@/app/utils/comparatorUtils'
import { VariableDisplayContext } from '@/app/utils/variables'
import { Edge as EdgeProto, VariableAssignment } from 'littlehorse-client/proto'
import {
  formatNodeOutputSourceLabel,
  getEdgeOperandDisplayText,
  getNodeOutputNodeName,
  getTruthyConditionDisplayText,
  hasResolvedNodeOutput,
  parseEdgeCondition,
} from './edgeConditionDisplay'

export type LabelPartKind = 'keyword' | 'source' | 'output' | 'literal' | 'operator'
export type LabelPart = { text: string; kind: LabelPartKind; title?: string }
export type LabelEdge = EdgeProto & {
  isElseEdge?: boolean
  nodeOutputValues?: VariableDisplayContext['nodeOutputValues']
}

const OPERAND_CHIP_MAX_PX = 88

const operandPart = (assignment: VariableAssignment, context?: VariableDisplayContext): LabelPart => {
  const nodeName = getNodeOutputNodeName(assignment)
  if (nodeName) {
    const taskLabel = formatNodeOutputSourceLabel(nodeName)
    if (hasResolvedNodeOutput(assignment, context)) {
      const text = getEdgeOperandDisplayText(assignment, context)
      return { text, kind: 'output', title: `Output of ${taskLabel} (${nodeName}): ${text}` }
    }
    return { text: taskLabel, kind: 'output', title: `Output of ${taskLabel} (${nodeName})` }
  }
  const text = getEdgeOperandDisplayText(assignment, context)
  return { text, kind: assignment.source.oneofKind === 'literalValue' ? 'literal' : 'source' }
}

export const conditionLabelParts = (edge: LabelEdge): LabelPart[] => {
  const parsed = parseEdgeCondition(edge.edgeCondition)
  if (!parsed) return []
  const context = edge.nodeOutputValues ? { nodeOutputValues: edge.nodeOutputValues } : undefined
  const keyword: LabelPart = { text: 'If', kind: 'keyword' }
  if (parsed.isTruthyCheck && parsed.leftOperand) {
    return [
      keyword,
      { ...operandPart(parsed.leftOperand, context), text: getTruthyConditionDisplayText(parsed.leftOperand, context) },
    ]
  }
  return [
    keyword,
    ...(parsed.leftOperand ? [operandPart(parsed.leftOperand, context)] : []),
    {
      text: parsed.operatorSymbol,
      kind: 'operator',
      title: parsed.comparator !== undefined ? getComparatorLabel(parsed.comparator) : undefined,
    },
    ...(parsed.rightOperand ? [operandPart(parsed.rightOperand, context)] : []),
  ]
}

export const edgeLabelSize = (edge?: LabelEdge) => {
  if (!edge) return undefined
  const parts = conditionLabelParts(edge)
  const condition = parts.length > 0
  const chip = condition || edge.isElseEdge
  const mutation = edge.variableMutations.length > 0
  if (!chip && !mutation) return undefined
  // 9px monospace glyphs are ~5.5px wide; 7px reserves font variation. Operand
  // chips truncate at OPERAND_CHIP_MAX_PX. The base covers the branch icon and
  // card padding; each chip adds its padding + ring, all before Default's 0.75 scale.
  const chipWidth = condition
    ? parts.reduce((width, part) => {
        const text = part.text.length * 7
        if (part.kind === 'keyword') return width + text
        if (part.kind === 'operator') return width + text + 8
        return width + Math.min(text, OPERAND_CHIP_MAX_PX) + 10
      }, 30) +
      (parts.length - 1) * 4
    : 30 + 'Else'.length * 7
  return {
    w: Math.max(chip ? chipWidth * 0.75 : 0, mutation ? 16 : 0),
    h: (chip ? 27 * 0.75 : 0) + (mutation ? 16 : 0),
  }
}
