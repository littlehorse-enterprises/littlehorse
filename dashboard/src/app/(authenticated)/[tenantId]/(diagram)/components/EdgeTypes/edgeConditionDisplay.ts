import { getVariable, VariableDisplayContext } from '@/app/utils'
import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { Comparator, Edge as EdgeProto, VariableAssignment } from 'littlehorse-client/proto'

/** Human-readable task/node label from WfSpec node names like `1-greet-TASK`. */
export const formatNodeOutputSourceLabel = (nodeName: string): string => {
  const parts = nodeName.split('-')
  if (parts.length >= 3) {
    const nodeType = parts[parts.length - 1]
    if (nodeType === 'TASK' || nodeType === 'USER_TASK') {
      return parts.slice(1, -1).join('-')
    }
  }
  return nodeName
}

export const getNodeOutputNodeName = (assignment: VariableAssignment): string | undefined => {
  if (assignment.source?.$case !== 'nodeOutput') return undefined
  return assignment.source.value.nodeName
}

export const hasResolvedNodeOutput = (
  assignment: VariableAssignment,
  displayContext?: VariableDisplayContext
): boolean => {
  const nodeName = getNodeOutputNodeName(assignment)
  return nodeName != null && displayContext?.nodeOutputValues?.[nodeName] != null
}

export const getOperandDisplayText = (
  assignment: VariableAssignment,
  displayContext?: VariableDisplayContext,
  options?: { preferSourceLabel?: boolean }
): string => {
  const nodeName = getNodeOutputNodeName(assignment)
  if (options?.preferSourceLabel && nodeName) {
    return formatNodeOutputSourceLabel(nodeName)
  }
  return getVariable(assignment, 0, displayContext)
}

export type ParsedEdgeCondition = {
  leftOperand?: VariableAssignment
  rightOperand?: VariableAssignment
  comparator?: Comparator
  operatorSymbol: string
}

export const parseEdgeCondition = (edgeCondition: EdgeProto['edgeCondition']): ParsedEdgeCondition | null => {
  if (!edgeCondition) return null

  if (edgeCondition.$case === 'legacyCondition') {
    const { left, right, comparator } = edgeCondition.value ?? {}
    return {
      leftOperand: left,
      rightOperand: right,
      comparator,
      operatorSymbol: comparator != null ? getComparatorSymbol(comparator) : '',
    }
  }

  if (edgeCondition.$case === 'condition') {
    const variableAssignment = edgeCondition.value
    if (!variableAssignment?.source) return null

    if (variableAssignment.source.$case === 'expression') {
      const expression = variableAssignment.source.value
      const { lhs, rhs, operation } = expression ?? {}
      if (!operation || operation.$case !== 'comparator') return null
      const comparator = operation.value
      return {
        leftOperand: lhs,
        rightOperand: rhs,
        comparator,
        operatorSymbol: getComparatorSymbol(comparator),
      }
    }
  }

  return null
}

export const isNopConditionalBranch = (outgoingEdges: Pick<EdgeProto, 'edgeCondition'>[]): boolean => {
  if (outgoingEdges.length <= 1) return false
  return outgoingEdges.some(edge => edge.edgeCondition != null)
}

/** True when the branch target executed in the current WfRun thread (WfSpec view always true). */
export const isBranchEdgeReached = (
  targetNodeName: string,
  threadNodeRuns: { nodeName?: string }[] | undefined
): boolean => {
  if (threadNodeRuns === undefined) return true
  return threadNodeRuns.some(nodeRun => nodeRun.nodeName === targetNodeName)
}
