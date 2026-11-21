import ELK, { type ElkExtendedEdge, type ElkNode } from 'elkjs/lib/elk.bundled.js'
import { Edge, Node } from 'reactflow'
import { conditionLabelParts, edgeLabelSize } from './EdgeTypes/edgeLabel'
import { type DiagramEdgeData } from './EdgeTypes/extractEdges'
import { planRoutes } from './EdgeTypes/elkRoute'
import { nodeDimensions } from './nodeDimensions'
import { HANDLER_LABEL_BAND, HANDLER_PADDING, NODE_CAPTION_HEIGHT } from './ExceptionHandlers/exceptionHandlers'

const elk = new ELK()

export const ELK_LAYOUT_OPTIONS = {
  'elk.algorithm': 'layered',
  'elk.direction': 'RIGHT',
  'elk.spacing.nodeNode': '50',
  'elk.layered.spacing.nodeNodeBetweenLayers': '80',
  'elk.spacing.edgeEdge': String(25 / 1.5),
  'elk.spacing.edgeNode': String(40 / 1.5),
  'elk.layered.spacing.edgeNodeBetweenLayers': String(40 / 1.5),
  'elk.layered.spacing.edgeEdgeBetweenLayers': String(25 / 1.5),
  'elk.edgeRouting': 'ORTHOGONAL',
  'elk.layered.nodePlacement.strategy': 'BRANDES_KOEPF',
  'elk.layered.cycleBreaking.strategy': 'DEPTH_FIRST',
  'elk.layered.considerModelOrder.strategy': 'NODES_AND_EDGES',
  'elk.layered.crossingMinimization.strategy': 'LAYER_SWEEP',
  'elk.layered.unnecessaryBendpoints': 'true',
  'elk.padding': '[top=50,left=50,bottom=50,right=50]',
  'elk.separateConnectedComponents': 'false',
  // Explicit ports provide the shared trunk; mergeEdges also covers portless
  // edges if a caller adds one. It does not deduplicate the SVG strokes.
  'org.eclipse.elk.layered.mergeEdges': 'true',
  'elk.layered.edgeLabels.centerLabelPlacementStrategy': 'TAIL_LAYER',
}

const portId = (node: string, handle: string) => `${node}/${handle}`

/**
 * The handler group a node is drawn inside (groups can nest). A handler's
 * own box, or its collapsed chip, sits in the group of the node that throws.
 */
const groupOf = (node?: Node): string | undefined =>
  node?.type === 'exceptionHandler' || node?.type === 'exceptionHandlerChip'
    ? node.data?.parentGroupId
    : node?.data?.groupId

/** Inlined handler nodes carry their ThreadSpec id; footprints come from it. */
export const specNodeId = (node: Node): string => node.data?.specNodeId ?? node.id

/** The box ELK reserves for a node; collapsed handler chips carry their own. */
export const footprintOf = (node: Node): { w: number; h: number } =>
  node.data?.footprint ?? nodeDimensions(node.type, specNodeId(node))

// A handler group is laid out as its box plus the label row above it: the
// extra top padding is that row, so nothing else is placed or routed there.
// Content stays top-left, so the box and its labels share the left edge.
const HANDLER_GROUP_OPTIONS = {
  'elk.padding': `[top=${HANDLER_LABEL_BAND + NODE_CAPTION_HEIGHT + HANDLER_PADDING},left=${HANDLER_PADDING},bottom=${HANDLER_PADDING},right=${HANDLER_PADDING}]`,
  'elk.contentAlignment': 'V_TOP H_LEFT',
}

// Handler groups are compound nodes: edges cross into and out of them, and
// every coordinate comes back absolute so reactflow nodes stay unparented.
const HIERARCHY_OPTIONS = {
  'elk.hierarchyHandling': 'INCLUDE_CHILDREN',
  'org.eclipse.elk.json.edgeCoords': 'ROOT',
  'org.eclipse.elk.json.shapeCoords': 'ROOT',
}

export const buildElkGraph = (nodes: Node[], edges: Edge<DiagramEdgeData>[]): ElkNode => {
  const byId = new Map(nodes.map(node => [node.id, node]))
  const groupsAround = (id: string) => {
    const chain: string[] = []
    for (let group = groupOf(byId.get(id)); group; group = groupOf(byId.get(group))) chain.push(group)
    return chain
  }
  // ELK wants each edge in the innermost group holding both ends.
  const containerOf = (edge: Edge) => {
    const aroundTarget = new Set(groupsAround(edge.target))
    return groupsAround(edge.source).find(group => aroundTarget.has(group))
  }

  const elkNode = (node: Node): ElkNode => {
    if (node.type === 'exceptionHandler') {
      // A label row wider than the nodes below it still gets its room.
      const labelWidth = node.data?.labelWidth
      return {
        id: node.id,
        layoutOptions: labelWidth
          ? {
              ...HANDLER_GROUP_OPTIONS,
              'elk.nodeSize.constraints': 'MINIMUM_SIZE',
              'elk.nodeSize.minimum': `(${labelWidth}, 0)`,
            }
          : HANDLER_GROUP_OPTIONS,
        children: nodes.filter(child => groupOf(child) === node.id).map(elkNode),
        edges: edges.filter(edge => containerOf(edge) === node.id).map(elkEdge),
      }
    }
    const { w, h } = footprintOf(node)
    const extraSouth = ['source-loop', 'source-failure'].filter(handle =>
      edges.some(edge => edge.source === node.id && edge.sourceHandle === handle)
    )
    return {
      id: node.id,
      width: w,
      height: h,
      layoutOptions: { 'elk.portConstraints': 'FIXED_SIDE', 'elk.portAlignment.default': 'CENTER' },
      ports: [
        { id: portId(node.id, 'target-0'), layoutOptions: { 'elk.port.side': 'WEST' } },
        { id: portId(node.id, 'source-0'), layoutOptions: { 'elk.port.side': 'EAST' } },
        ...extraSouth.map(handle => ({ id: portId(node.id, handle), layoutOptions: { 'elk.port.side': 'SOUTH' } })),
      ],
    }
  }

  const elkEdge = (edge: Edge<DiagramEdgeData>) => {
    const label = edgeLabelSize(edge.data)
    return {
      id: edge.id,
      sources: [portId(edge.source, edge.sourceHandle ?? 'source-0')],
      targets: [portId(edge.target, edge.targetHandle ?? 'target-0')],
      labels: label
        ? [
            {
              id: `${edge.id}/label`,
              text: edge.data
                ? conditionLabelParts(edge.data)
                    .map(part => part.text)
                    .join(' ') || (edge.data.isElseEdge ? 'else' : 'mutations')
                : '',
              width: label.w + 16,
              height: label.h + 16,
              layoutOptions: { 'elk.edgeLabels.inline': 'true', 'elk.edgeLabels.placement': 'CENTER' },
            },
          ]
        : [],
    }
  }

  const hasGroups = nodes.some(node => node.type === 'exceptionHandler')

  return {
    id: 'root',
    layoutOptions: hasGroups ? { ...ELK_LAYOUT_OPTIONS, ...HIERARCHY_OPTIONS } : ELK_LAYOUT_OPTIONS,
    children: nodes.filter(node => !groupOf(node)).map(elkNode),
    edges: edges.filter(edge => containerOf(edge) === undefined).map(elkEdge),
  }
}

const flatten = (node: ElkNode): ElkNode[] => (node.children ?? []).flatMap(child => [child, ...flatten(child)])
const flattenEdges = (node: ElkNode): ElkExtendedEdge[] => [
  ...(node.edges ?? []),
  ...(node.children ?? []).flatMap(flattenEdges),
]

export const layoutDiagram = async (nodes: Node[], edges: Edge<DiagramEdgeData>[]) => {
  const graph = await elk.layout(buildElkGraph(nodes, edges))
  const routes = flattenEdges(graph).map(edge => {
    const section = edge.sections?.[0]
    if (!section) throw new Error(`ELK did not route edge ${edge.id}`)
    // Converting nested coordinates to root ones adds float noise (161.66…66
    // vs 161.66…63), which breaks the exact-equality checks downstream.
    const points = [section.startPoint, ...(section.bendPoints ?? []), section.endPoint].map(point => ({
      x: Math.round(point.x * 1000) / 1000,
      y: Math.round(point.y * 1000) / 1000,
    }))
    return { id: edge.id, points }
  })
  const boxes = new Map(flatten(graph).map(node => [node.id, node]))
  return { graph, boxes, frames: handlerFrames(nodes, boxes), routes: planRoutes(edges, routes) }
}

export type Rect = { x: number; y: number; w: number; h: number }

/**
 * The drawn box of each expanded handler: what its own nodes actually draw
 * (nested handler boxes with their labels included) plus padding, and room
 * for the captions above the nodes. Nodes draw centered in their layout
 * footprint and some draw smaller than it (a task is ~31px tall in a 50px
 * footprint), so `measured` gives the rendered sizes when they're known; the
 * footprints stand in otherwise. The ELK group around the box is taller and
 * may be wider, to keep the label row above the box clear.
 */
export const handlerFrames = (
  nodes: Node[],
  boxes: Map<string, ElkNode>,
  measured: (node: Node) => { w: number; h: number } | undefined = () => undefined
): Map<string, Rect> => {
  const frames = new Map<string, Rect>()
  const depth = (node: Node) => node.id.split('/').length
  // Innermost first: a nested box is part of what its parent box holds.
  const groups = nodes.filter(node => node.type === 'exceptionHandler').sort((a, b) => depth(b) - depth(a))

  const drawn = (node: Node): Rect | undefined => {
    if (node.type === 'exceptionHandler') {
      const frame = frames.get(node.id)
      const labels = Math.max(frame?.w ?? 0, node.data?.labelWidth ?? 0)
      return frame && { x: frame.x, y: frame.y - HANDLER_LABEL_BAND, w: labels, h: frame.h + HANDLER_LABEL_BAND }
    }
    const box = boxes.get(node.id)
    if (!box) return undefined
    const footprint = { w: box.width ?? 0, h: box.height ?? 0 }
    const size = measured(node) ?? footprint
    return {
      x: (box.x ?? 0) + (footprint.w - size.w) / 2,
      y: (box.y ?? 0) + (footprint.h - size.h) / 2,
      w: size.w,
      h: size.h,
    }
  }

  for (const group of groups) {
    const inside = nodes.filter(node => groupOf(node) === group.id).flatMap(node => drawn(node) ?? [])
    if (inside.length === 0) continue
    const left = Math.min(...inside.map(r => r.x)) - HANDLER_PADDING
    const top = Math.min(...inside.map(r => r.y)) - HANDLER_PADDING - NODE_CAPTION_HEIGHT
    const right = Math.max(...inside.map(r => r.x + r.w)) + HANDLER_PADDING
    const bottom = Math.max(...inside.map(r => r.y + r.h)) + HANDLER_PADDING
    frames.set(group.id, { x: left, y: top, w: right - left, h: bottom - top })
  }
  return frames
}
