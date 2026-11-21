import ELK, { type ElkExtendedEdge, type ElkNode } from 'elkjs/lib/elk.bundled.js'
import { Edge, Node } from 'reactflow'
import { conditionLabelParts, edgeLabelSize } from './EdgeTypes/edgeLabel'
import { type DiagramEdgeData } from './EdgeTypes/extractEdges'
import { planRoutes } from './EdgeTypes/elkRoute'
import { nodeDimensions } from './nodeDimensions'

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

const HANDLER_GROUP_OPTIONS = {
  'elk.padding': '[top=34,left=24,bottom=34,right=24]',
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
      const minWidth = node.data?.minWidth
      return {
        id: node.id,
        layoutOptions: minWidth
          ? {
              ...HANDLER_GROUP_OPTIONS,
              'elk.nodeSize.constraints': 'MINIMUM_SIZE',
              'elk.nodeSize.minimum': `(${minWidth}, 0)`,
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
  return { graph, boxes, routes: planRoutes(edges, routes) }
}
