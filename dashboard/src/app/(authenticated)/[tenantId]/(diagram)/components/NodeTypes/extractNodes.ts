import { Node as NodeProto, WfSpec } from 'littlehorse-client/proto'
import { Node, NodeProps } from 'reactflow'
import { ThreadSpecWithName } from '../Diagram'

export function extractNodes(wfSpec: WfSpec, threadSpec: ThreadSpecWithName) {
  const reactFlowNodes: Node[] = []
  Object.entries(threadSpec.threadSpec.nodes).forEach(([id, node]) => {
    const reactFlowNode = extractNode(id, node, threadSpec)
    reactFlowNodes.push(reactFlowNode)

    if (reactFlowNode.type === 'START_THREAD') {
      const startedThreadSpecName = node.startThread?.threadSpecName
      if (startedThreadSpecName === undefined) return

      const moreNodes = extractNodes(wfSpec, {
        name: startedThreadSpecName,
        threadSpec: wfSpec.threadSpecs[startedThreadSpecName],
      })
      reactFlowNodes.push(...moreNodes)
    }
  })
  return reactFlowNodes
}

function extractNode(id: string, node: NodeProto, threadSpec: ThreadSpecWithName): Node {
  const type = getNodeType(node)
  return {
    id: `${id}:${threadSpec.name}`,
    type,
    data: { ...node, ...extractData(type, node) },
    position: { x: 0, y: 0 },
  }
}

export type NodeRunTypeList = Exclude<
  NodeType,
  'ENTRYPOINT' | 'NOP' | 'EXIT' | 'UNKNOWN_NODE_TYPE' | 'START_MULTIPLE_THREADS'
>

type NodeObj = {
  [key in keyof Omit<NodeProto, 'outgoingEdges' | 'failureHandlers'>]: unknown
}

const extractData = (type: NodeType, node: NodeProto) => {
  switch (type) {
    case 'ENTRYPOINT':
      return node.entrypoint
    case 'EXIT':
      return node.exit
    case 'TASK':
      return node.task
    case 'EXTERNAL_EVENT':
      return node.externalEvent
    case 'START_THREAD':
      return node.startThread
    case 'WAIT_FOR_THREADS':
      return node.waitForThreads
    case 'NOP':
      return node.nop
    case 'SLEEP':
      return node.sleep
    case 'USER_TASK':
      return node.userTask
    case 'START_MULTIPLE_THREADS':
      return node.startMultipleThreads
    case 'THROW_EVENT':
      return node.throwEvent
    case 'WAIT_FOR_CONDITION':
      return node.waitForCondition
  }
}

export type NodeType =
  | 'ENTRYPOINT'
  | 'EXIT'
  | 'TASK'
  | 'EXTERNAL_EVENT'
  | 'START_THREAD'
  | 'WAIT_FOR_THREADS'
  | 'NOP'
  | 'SLEEP'
  | 'USER_TASK'
  | 'START_MULTIPLE_THREADS'
  | 'THROW_EVENT'
  | 'UNKNOWN_NODE_TYPE'
  | 'WAIT_FOR_CONDITION'

export const getNodeType = (node: NodeObj): NodeType => {
  if (node['exit'] !== undefined) return 'EXIT'
  if (node['task'] !== undefined) return 'TASK'
  if (node['externalEvent'] !== undefined) return 'EXTERNAL_EVENT'
  if (node['startThread'] !== undefined) return 'START_THREAD'
  if (node['waitForThreads'] !== undefined) return 'WAIT_FOR_THREADS'
  if (node['waitForCondition'] !== undefined) return 'WAIT_FOR_CONDITION'
  if (node['nop'] !== undefined) return 'NOP'
  if (node['sleep'] !== undefined) return 'SLEEP'
  if (node['userTask'] !== undefined) return 'USER_TASK'
  if (node['entrypoint'] !== undefined) return 'ENTRYPOINT'
  if (node['startMultipleThreads'] !== undefined) return 'START_MULTIPLE_THREADS'
  if (node['throwEvent'] !== undefined) return 'THROW_EVENT'
  return 'UNKNOWN_NODE_TYPE'
}

export type NodeData<T = any> = NodeProps<NodeProto & T>
