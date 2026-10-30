import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { getVariable, VariableDisplayContext } from '@/app/utils/variables'
import { Comparator, Edge as EdgeProto, VariableAssignment } from 'littlehorse-client/proto'

export const formatNodeOutputSourceLabel = (nodeName: string): string => {
  const parts = nodeName.split('-')
  if (parts.length >= 3) {
    const nodeType = parts[parts.length - 1]
    if (nodeType === 'TASK' || nodeType === 'USER_TASK') return parts.slice(1, -1).join('-')
  }
  return nodeName
}

export const getNodeOutputNodeName = (assignment: VariableAssignment): string | undefined =>
  assignment.source.oneofKind === 'nodeOutput' ? assignment.source.nodeOutput.nodeName : undefined

export const hasResolvedNodeOutput = (assignment: VariableAssignment, context?: VariableDisplayContext): boolean => {
  const nodeName = getNodeOutputNodeName(assignment)
  return nodeName !== undefined && context?.nodeOutputValues?.[nodeName] !== undefined
}

export const getOperandDisplayText = (
  assignment: VariableAssignment,
  context?: VariableDisplayContext,
  options?: { preferSourceLabel?: boolean }
): string => {
  const nodeName = getNodeOutputNodeName(assignment)
  if (options?.preferSourceLabel && nodeName) return formatNodeOutputSourceLabel(nodeName)
  return getVariable(assignment, 0, context)
}

export type ParsedEdgeCondition = {
  leftOperand?: VariableAssignment
  rightOperand?: VariableAssignment
  comparator?: Comparator
  operatorSymbol: string
  isTruthyCheck?: boolean
}

export const getTruthyConditionDisplayText = (
  assignment: VariableAssignment,
  context?: VariableDisplayContext
): string => {
  if (assignment.source.oneofKind === 'variableName' && assignment.source.variableName) {
    const name = assignment.source.variableName
    if (assignment.path?.oneofKind === 'jsonPath') return `${name}${assignment.path.jsonPath.replace('$.', '.')}`
    if (assignment.path?.oneofKind === 'lhPath') return getVariable(assignment, 0, context).replace(/^\{|\}$/g, '')
    return name
  }
  const nodeName = getNodeOutputNodeName(assignment)
  if (nodeName) {
    if (hasResolvedNodeOutput(assignment, context)) return getOperandDisplayText(assignment, context)
    return formatNodeOutputSourceLabel(nodeName)
  }
  return getOperandDisplayText(assignment, context)
}

export const getEdgeOperandDisplayText = (
  assignment: VariableAssignment,
  context?: VariableDisplayContext,
  options?: { preferSourceLabel?: boolean }
): string => {
  if (assignment.source.oneofKind === 'variableName') return getTruthyConditionDisplayText(assignment, context)
  return getOperandDisplayText(assignment, context, options)
}

export const parseEdgeCondition = (
  edgeCondition: EdgeProto['edgeCondition'] | undefined
): ParsedEdgeCondition | null => {
  if (edgeCondition?.oneofKind === 'legacyCondition') {
    const { left, right, comparator } = edgeCondition.legacyCondition
    return { leftOperand: left, rightOperand: right, comparator, operatorSymbol: getComparatorSymbol(comparator) }
  }
  if (edgeCondition?.oneofKind === 'condition') {
    const assignment = edgeCondition.condition
    if (assignment.source.oneofKind === undefined) return null
    if (assignment.source.oneofKind === 'expression') {
      const { lhs, rhs, operation } = assignment.source.expression
      if (operation.oneofKind !== 'comparator') return null
      const comparator = operation.comparator
      return { leftOperand: lhs, rightOperand: rhs, comparator, operatorSymbol: getComparatorSymbol(comparator) }
    }
    return { leftOperand: assignment, operatorSymbol: '', isTruthyCheck: true }
  }
  return null
}

export const isNopConditionalBranch = (outgoingEdges: Pick<EdgeProto, 'edgeCondition'>[]): boolean =>
  outgoingEdges.length > 1 && outgoingEdges.some(edge => edge.edgeCondition?.oneofKind !== undefined)

export const isBranchEdgeReached = (
  targetNodeName: string,
  threadNodeRuns: { nodeName?: string }[] | undefined
): boolean => threadNodeRuns === undefined || threadNodeRuns.some(nodeRun => nodeRun.nodeName === targetNodeName)
