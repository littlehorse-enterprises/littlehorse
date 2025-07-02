import { ThreadSpec, WfSpec } from 'littlehorse-client/proto'
import { CustomNode } from '@/types/node'

export function extractNodes(wfSpec: WfSpec, threadSpec: ThreadSpec): CustomNode[] {
  const customNodes: CustomNode[] = []

  Object.entries(threadSpec.nodes as ThreadSpec['nodes']).forEach(([id, node]) => {
    const nodeCase = node.node?.$case
    if (nodeCase === undefined) return

    const threadSpecName = Object.keys(wfSpec.threadSpecs).find(key => wfSpec.threadSpecs[key] === threadSpec)
    const customNode: CustomNode = {
      id: `${id}:${threadSpecName}`,
      type: 'node',
      data: {
        node,
        type: nodeCase,
        label: id,
      },
      position: { x: 0, y: 0 },
    }
    customNodes.push(customNode)

    // #region Recursion
    if (nodeCase === 'startThread') {
      const startThreadEntrypointThread = node.node.startThread?.threadSpecName
      if (startThreadEntrypointThread === undefined) return

      const moreNodes = extractNodes(wfSpec, wfSpec.threadSpecs[startThreadEntrypointThread])
      customNodes.push(...moreNodes)
    }
    // #endregion
  })

  return customNodes
}
