import { ThreadSpec, WfSpec } from 'littlehorse-client/proto'
import { CustomNode } from '@/types/node'
import { getNodeType } from './node'

export function extractNodes(wfSpec: WfSpec, threadSpec: ThreadSpec): CustomNode[] {
  const customNodes: CustomNode[] = []

  Object.entries(threadSpec.nodes as ThreadSpec['nodes']).forEach(([id, node]) => {
    const threadSpecName = Object.keys(wfSpec.threadSpecs).find(key => wfSpec.threadSpecs[key] === threadSpec)
    const customNode = {
      id: `${id}:${threadSpecName}`,
      type: 'node',
      data: {
        node,
        type: getNodeType(node).type,
        label: id,
      },
      position: { x: 0, y: 0 },
    }
    customNodes.push(customNode)

    // #region Recursion
    if (customNode.data.type === 'START_THREAD') {
      const startThreadEntrypointThread = node.startThread?.threadSpecName
      if (startThreadEntrypointThread === undefined) return

      const moreNodes = extractNodes(wfSpec, wfSpec.threadSpecs[startThreadEntrypointThread])
      customNodes.push(...moreNodes)
    }
    // #endregion
  })

  return customNodes
}
