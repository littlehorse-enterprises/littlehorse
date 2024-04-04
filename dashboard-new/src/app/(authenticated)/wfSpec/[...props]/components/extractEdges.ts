import { Comparator, VariableAssignment } from 'littlehorse-client/dist/proto/common_wfspec'
import { VariableValue } from 'littlehorse-client/dist/proto/variable'
import { Edge as EdgeProto, ThreadSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { Edge } from 'reactflow'

export const extractEdges = (spec: ThreadSpec): Edge[] => {
  const edgeMap = new Map<string, number>()
  return Object.entries(spec.nodes).flatMap(([source, node]) => {
    return node.outgoingEdges.map(edge => {
      const edgeId = `${source}-${edge.sinkNodeName}`
      const index = edgeMap.get(edgeId) ?? 0
      const id = index === 0 ? edgeId : `${edgeId}-${index}`
      edgeMap.set(edgeId, index + 1)

      const label = extractEdgeLabel(edge)
      return {
        id,
        source,
        type: 'default',
        target: edge.sinkNodeName,
        label,
        targetHandle: `${index}`,
        sourceHandle: `${index}`,
        animated: true,
      }
    })
  })
}

const extractEdgeLabel = ({ condition }: EdgeProto) => {
  if (!condition) return

  const { left, right, comparator } = condition
  return `${getValue(left)} ${getComparator(comparator)} ${getValue(right)}`
}

const getComparator = (comparator: Comparator) => Conditions[comparator]
export const Conditions: Record<Comparator, string> = {
  [Comparator.LESS_THAN]: '<',
  [Comparator.GREATER_THAN]: '>',
  [Comparator.LESS_THAN_EQ]: '<=',
  [Comparator.GREATER_THAN_EQ]: '>=',
  [Comparator.EQUALS]: '=',
  [Comparator.NOT_EQUALS]: '!=',
  [Comparator.IN]: 'IN',
  [Comparator.NOT_IN]: 'NOT IN',
  [Comparator.UNRECOGNIZED]: '',
}

const getValue = (variable?: VariableAssignment) => {
  if (!variable) return 'undefined'
  if (variable.formatString) return getValueFromFormatString(variable)
  if (variable.variableName) {
    return getValueFromVariableName(variable)
  }
  if (variable.literalValue) return getValueFromLiteralValue(variable)
}

const getValueFromVariableName = ({
  variableName,
  jsonPath,
}: Pick<VariableAssignment, 'variableName' | 'jsonPath'>) => {
  if (jsonPath) return `${jsonPath} within ${variableName}`
  return variableName
}

const getValueFromLiteralValue = ({ literalValue }: Pick<VariableAssignment, 'literalValue'>) => {
  if (!literalValue) return
  const key = Object.keys(literalValue)[0] as keyof VariableValue
  return literalValue[key]
}

const getValueFromFormatString = ({ formatString }: Pick<VariableAssignment, 'formatString'>) => {
  if (!formatString) return
  return `${formatString.format}(${formatString.args})`
}
