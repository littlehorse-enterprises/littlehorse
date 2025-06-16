import { Node } from '@xyflow/react'
import { Node as NodeProto, ThreadSpec, WfSpec } from 'littlehorse-client/proto'
import { getNodeType } from './node'

function extractNode(id: string, node: NodeProto, threadSpecName: string): Node {
  return {
    id: `${id}:${threadSpecName}`,
    type: 'node',
    data: { ...node, type: getNodeType(node), label: id },
    position: { x: 0, y: 0 },
  }
}

export function extractNodes(wfSpec: WfSpec, threadSpec: ThreadSpec) {
  const reactFlowNodes: Node[] = []
  Object.entries(threadSpec.nodes as ThreadSpec['nodes']).forEach(([id, node]) => {
    const threadSpecName = Object.keys(wfSpec.threadSpecs).find(key => wfSpec.threadSpecs[key] === threadSpec) as string
    const reactFlowNode = extractNode(id, node, threadSpecName)
    reactFlowNodes.push(reactFlowNode)

    if (reactFlowNode.data.type === 'START_THREAD') {
      const startedThreadSpecName = node.startThread?.threadSpecName
      if (startedThreadSpecName === undefined) return

      const moreNodes = extractNodes(wfSpec, wfSpec.threadSpecs[startedThreadSpecName])
      reactFlowNodes.push(...moreNodes)
    }
  })
  return reactFlowNodes
}
