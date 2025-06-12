import { Node as NodeProto, ThreadSpec, WfSpec } from 'littlehorse-client/proto'
import { Node } from '@xyflow/react'
import { ThreadSpecWithName } from '@/types/withs'
import { getNodeType } from './node'

function extractNode(id: string, node: NodeProto, threadSpec: ThreadSpecWithName): Node {
  return {
    id: `${id}:${threadSpec.name}`,
    type: 'node',
    data: { ...node, type: getNodeType(node), label: id },
    position: { x: 0, y: 0 },
  }
}

export function extractNodes(wfSpec: WfSpec, threadSpec: ThreadSpecWithName) {
  const reactFlowNodes: Node[] = []
  Object.entries(threadSpec.threadSpec.nodes as ThreadSpec['nodes']).forEach(([id, node]) => {
    const reactFlowNode = extractNode(id, node, threadSpec)
    reactFlowNodes.push(reactFlowNode)

    if (reactFlowNode.data.type === 'START_THREAD') {
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
