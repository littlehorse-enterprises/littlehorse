import ELK, { type ElkNode } from 'elkjs/lib/elk.bundled.js'
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

export const buildElkGraph = (nodes: Node[], edges: Edge<DiagramEdgeData>[]): ElkNode => ({
  id: 'root',
  layoutOptions: ELK_LAYOUT_OPTIONS,
  children: nodes.map(node => {
    const { w, h } = nodeDimensions(node.type, node.id)
    const loop = edges.some(edge => edge.source === node.id && edge.sourceHandle === 'source-loop')
    return {
      id: node.id,
      width: w,
      height: h,
      layoutOptions: { 'elk.portConstraints': 'FIXED_SIDE', 'elk.portAlignment.default': 'CENTER' },
      ports: [
        { id: portId(node.id, 'target-0'), layoutOptions: { 'elk.port.side': 'WEST' } },
        { id: portId(node.id, 'source-0'), layoutOptions: { 'elk.port.side': 'EAST' } },
        ...(loop ? [{ id: portId(node.id, 'source-loop'), layoutOptions: { 'elk.port.side': 'SOUTH' } }] : []),
      ],
    }
  }),
  edges: edges.map(edge => {
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
  }),
})

export const layoutDiagram = async (nodes: Node[], edges: Edge<DiagramEdgeData>[]) => {
  const graph = await elk.layout(buildElkGraph(nodes, edges))
  const routes = (graph.edges ?? []).map(edge => {
    const section = edge.sections?.[0]
    if (!section) throw new Error(`ELK did not route edge ${edge.id}`)
    return { id: edge.id, points: [section.startPoint, ...(section.bendPoints ?? []), section.endPoint] }
  })
  return { graph, routes: planRoutes(edges, routes) }
}
