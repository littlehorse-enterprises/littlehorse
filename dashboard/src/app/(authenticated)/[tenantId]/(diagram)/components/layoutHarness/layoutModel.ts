import { ThreadSpec } from 'littlehorse-client/proto'
import { Position } from 'reactflow'
import { nodeDimensions } from '../nodeDimensions'
import { extractEdges } from '../EdgeTypes/extractEdges'
import { edgeLabelSize } from '../EdgeTypes/edgeLabel'
import { extractNodes, getCycleNodes } from '../NodeTypes/extractNodes'
import { nopSourceHandlePlacements } from '../NodeTypes/nopHandleLayout'
import { layoutDiagram } from '../graphLayout'
import { snapEdgeRoute } from '../EdgeTypes/elkRoute'
import { Polyline, Rect } from './geometry'

export interface SceneNode {
  id: string
  type: string
  rect: Rect
}

export interface SceneEdge {
  id: string
  source: string
  target: string
  targetHandle: string
  isCycleEdge: boolean
  hasLabel: boolean
  labelRect?: Rect
  polyline: Polyline
  paths: Polyline[]
  missingSourceHandle: boolean
  missingTargetHandle: boolean
}

export interface Scene {
  nodes: SceneNode[]
  edges: SceneEdge[]
}

export const rendersHandle = (
  nodeType: string,
  type: 'source' | 'target',
  id: string,
  outgoing: ThreadSpec['nodes'][string]['outgoingEdges']
): boolean => {
  if (type === 'target') return nodeType !== 'entrypoint' && id === 'target-0'
  if (nodeType === 'exit' && id === 'source-0') return false
  return nopSourceHandlePlacements(outgoing).some(placement => placement.id === id)
}

export const buildScene = async (threadSpec: ThreadSpec): Promise<Scene> => {
  const spec = ThreadSpec.clone(threadSpec)
  getCycleNodes(spec)
  const rfNodes = extractNodes(spec)
  const rfEdges = extractEdges(spec)
  const { graph, routes } = await layoutDiagram(rfNodes, rfEdges)
  const positioned = new Map((graph.children ?? []).map(child => [child.id, child]))
  const nodes: SceneNode[] = rfNodes.map(node => {
    const elkNode = positioned.get(node.id)
    if (!elkNode) throw new Error(`ELK did not position node ${node.id}`)
    const { w, h } = nodeDimensions(node.type, node.id)
    return { id: node.id, type: node.type ?? 'task', rect: { x: elkNode.x!, y: elkNode.y!, w, h } }
  })
  const byId = new Map(nodes.map(node => [node.id, node]))
  const edges: SceneEdge[] = rfEdges.map(edge => {
    const planned = routes.get(edge.id)
    if (!planned) throw new Error(`ELK did not route edge ${edge.id}`)
    const source = byId.get(edge.source)!.rect
    const target = byId.get(edge.target)!.rect
    const loop = edge.sourceHandle === 'source-loop'
    const route = snapEdgeRoute(
      planned,
      { x: source.x + source.w * (loop ? 0.5 : 1), y: source.y + source.h * (loop ? 1 : 0.5) },
      { x: target.x, y: target.y + target.h / 2 },
      loop ? Position.Bottom : Position.Right,
      Position.Left
    )
    const size = edgeLabelSize(edge.data)
    const labelRect =
      size && route.labelPoint
        ? { x: route.labelPoint.x - size.w / 2, y: route.labelPoint.y - size.h / 2, ...size }
        : undefined
    return {
      id: edge.id,
      source: edge.source,
      target: edge.target,
      targetHandle: edge.targetHandle ?? 'target-0',
      isCycleEdge: edge.source.startsWith('cycle-') || edge.target.startsWith('cycle-'),
      hasLabel: size !== undefined,
      labelRect,
      polyline: route.points,
      paths: route.paths,
      missingSourceHandle: !rendersHandle(
        byId.get(edge.source)!.type,
        'source',
        edge.sourceHandle!,
        spec.nodes[edge.source].outgoingEdges
      ),
      missingTargetHandle: !rendersHandle(
        byId.get(edge.target)!.type,
        'target',
        edge.targetHandle!,
        spec.nodes[edge.target].outgoingEdges
      ),
    }
  })
  return { nodes, edges }
}
