import { getVariable } from '@/app/utils/variables'
import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { Edge as EdgeProto } from 'littlehorse-client/proto'

export type LabelPart = { text: string; badge: boolean }

export const conditionLabelParts = (edge: EdgeProto): LabelPart[] => {
  const condition = edge.edgeCondition
  if (condition.oneofKind === undefined) return []
  if (condition.oneofKind === 'legacyCondition') {
    const { left, right, comparator } = condition.legacyCondition
    return [
      ...(left ? [{ text: getVariable(left), badge: true }] : []),
      { text: getComparatorSymbol(comparator), badge: false },
      ...(right ? [{ text: getVariable(right), badge: true }] : []),
    ]
  }
  const assignment = condition.condition
  if (assignment.source.oneofKind === undefined) return []
  if (assignment.source.oneofKind === 'expression') {
    const { lhs, rhs, operation } = assignment.source.expression
    if (operation.oneofKind === 'comparator') {
      return [
        ...(lhs ? [{ text: getVariable(lhs), badge: true }] : []),
        { text: getComparatorSymbol(operation.comparator), badge: false },
        ...(rhs ? [{ text: getVariable(rhs), badge: true }] : []),
      ]
    }
  }
  return [{ text: getVariable(assignment), badge: true }]
}

export const edgeLabelSize = (edge?: EdgeProto & { isElseEdge?: boolean }) => {
  if (!edge) return undefined
  const parts = conditionLabelParts(edge)
  const condition = parts.length > 0
  const chip = condition || edge.isElseEdge
  const mutation = edge.variableMutations.length > 0
  if (!chip && !mutation) return undefined
  // 10px monospace glyphs are ~6px wide; 7px reserves font variation. Include
  // badge padding, flex gaps and chip padding before Default's 0.75 scale.
  const chipWidth = condition
    ? parts.reduce((width, part) => width + part.text.length * 7 + (part.badge ? 8 : 0), 16) +
      Math.max(0, parts.length - 1) * 4
    : 44
  return {
    w: Math.max(chip ? chipWidth * 0.75 : 0, mutation ? 16 : 0),
    h: (chip ? (condition ? 27 : 23) * 0.75 : 0) + (mutation ? 16 : 0),
  }
}
