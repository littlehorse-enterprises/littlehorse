import { Node as FlowNode } from 'reactflow'
import { getNopSourceHandleConfigs, HandleSide } from './nopHandles'

const PORT_PREFIX = 'port'

export const portId = (nodeId: string, handleId: string) => `${nodeId}:${PORT_PREFIX}:${handleId}`

export type { HandleSide }

const parseHandleOffset = (handleId: string): number => {
  const match = handleId.match(/-(\d+)$/)
  if (!match) return 0.5
  const index = Number(match[1])
  return (index + 1) / (index + 2)
}

export const getHandleSide = (node: FlowNode, handleId: string, handleType: 'source' | 'target'): HandleSide => {
  if (node.type === 'cycle') {
    return handleType === 'target' ? 'EAST' : 'WEST'
  }

  if (node.type === 'nop' && handleType === 'source') {
    const configs = getNopSourceHandleConfigs(node.data?.outgoingEdges ?? [])
    const match = configs.find(config => config.id === handleId)
    if (match) return match.side
  }

  if (handleType === 'target') return 'WEST'
  return 'EAST'
}

export const getHandleSideAndOffset = (
  node: FlowNode,
  handleId: string,
  handleType: 'source' | 'target'
): { side: HandleSide; offset: number } => {
  if (node.type === 'nop' && handleType === 'source') {
    const configs = getNopSourceHandleConfigs(node.data?.outgoingEdges ?? [])
    const match = configs.find(config => config.id === handleId)
    if (match) return { side: match.side, offset: match.offset }
  }

  return { side: getHandleSide(node, handleId, handleType), offset: parseHandleOffset(handleId) }
}

export const buildNodePorts = (node: FlowNode) => {
  const ports: Array<{ id: string; width: number; height: number; layoutOptions: Record<string, string> }> = [
    {
      id: portId(node.id, 'target-0'),
      width: 1,
      height: 1,
      layoutOptions: { 'org.eclipse.elk.port.side': getHandleSide(node, 'target-0', 'target') },
    },
  ]

  if (node.type === 'entrypoint') {
    ports.push({
      id: portId(node.id, 'source-0'),
      width: 1,
      height: 1,
      layoutOptions: { 'org.eclipse.elk.port.side': 'EAST' },
    })
    return ports
  }

  if (node.type === 'exit') {
    return ports
  }

  if (node.type === 'nop') {
    getNopSourceHandleConfigs(node.data?.outgoingEdges ?? []).forEach(config => {
      ports.push({
        id: portId(node.id, config.id),
        width: 1,
        height: 1,
        layoutOptions: { 'org.eclipse.elk.port.side': config.side },
      })
    })
    return ports
  }

  if (node.type === 'cycle') {
    ports[0].layoutOptions['org.eclipse.elk.port.side'] = 'EAST'
    ports.push({
      id: portId(node.id, 'source-0'),
      width: 1,
      height: 1,
      layoutOptions: { 'org.eclipse.elk.port.side': 'WEST' },
    })
    return ports
  }

  ports.push({
    id: portId(node.id, 'source-0'),
    width: 1,
    height: 1,
    layoutOptions: { 'org.eclipse.elk.port.side': 'EAST' },
  })

  return ports
}
