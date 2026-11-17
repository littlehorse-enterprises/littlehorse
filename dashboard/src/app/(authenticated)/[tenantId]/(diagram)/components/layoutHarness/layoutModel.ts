import { ThreadSpec } from 'littlehorse-client/proto'
import { Position } from 'reactflow'
import { nodeDimensions } from '../nodeDimensions'
import { extractEdges } from '../EdgeTypes/extractEdges'
import { edgeLabelSize } from '../EdgeTypes/edgeLabel'
import { extractNodes } from '../NodeTypes/extractNodes'
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
  /** Source and target can reach each other, so the edge is part of a loop in the spec. */
  inLoop: boolean
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

export const rendersHandle = (nodeType: string, type: 'source' | 'target', id: string): boolean => {
  if (type === 'target') return nodeType !== 'entrypoint' && id === 'target-0'
  return id === 'source-0'
}

/** Groups nodes that can all reach one another (Tarjan's strongly connected components). */
const loopGroups = (spec: ThreadSpec): Map<string, number> => {
  const group = new Map<string, number>()
  const index = new Map<string, number>()
  const low = new Map<string, number>()
  const stack: string[] = []
  const onStack = new Set<string>()
  let next = 0
  let groups = 0
  const visit = (id: string) => {
    index.set(id, next)
    low.set(id, next++)
    stack.push(id)
    onStack.add(id)
    for (const { sinkNodeName } of spec.nodes[id]?.outgoingEdges ?? []) {
      if (!index.has(sinkNodeName)) {
        visit(sinkNodeName)
        low.set(id, Math.min(low.get(id)!, low.get(sinkNodeName)!))
      } else if (onStack.has(sinkNodeName)) {
        low.set(id, Math.min(low.get(id)!, index.get(sinkNodeName)!))
      }
    }
    if (low.get(id) === index.get(id)) {
      let member: string
      do {
        member = stack.pop()!
        onStack.delete(member)
        group.set(member, groups)
      } while (member !== id)
      groups++
    }
  }
  for (const id of Object.keys(spec.nodes)) if (!index.has(id)) visit(id)
  return group
}

export const buildScene = async (threadSpec: ThreadSpec): Promise<Scene> => {
  const spec = ThreadSpec.clone(threadSpec)
  const groups = loopGroups(spec)
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
    const route = snapEdgeRoute(
      planned,
      { x: source.x + source.w, y: source.y + source.h / 2 },
      { x: target.x, y: target.y + target.h / 2 },
      Position.Right,
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
      inLoop: groups.get(edge.source) === groups.get(edge.target),
      hasLabel: size !== undefined,
      labelRect,
      polyline: route.points,
      paths: route.paths,
      missingSourceHandle: !rendersHandle(byId.get(edge.source)!.type, 'source', edge.sourceHandle!),
      missingTargetHandle: !rendersHandle(byId.get(edge.target)!.type, 'target', edge.targetHandle!),
    }
  })
  return { nodes, edges }
}
