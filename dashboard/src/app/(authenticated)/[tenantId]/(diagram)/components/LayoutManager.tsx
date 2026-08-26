import ELK, { type ElkNode } from 'elkjs/lib/elk.bundled.js'
import { NodeRun } from 'littlehorse-client/proto'
import { FC, useCallback, useEffect } from 'react'
import { Edge, Node, useOnViewportChange, useReactFlow, useStore, type Viewport } from 'reactflow'
import type { RoutePoint } from './EdgeTypes/elkRoute'

const elk = new ELK()

export const getNodeRunsList = (nodeId: string, nodeRuns?: NodeRun[]): NodeRun[] | undefined =>
  nodeRuns
    ?.filter(nodeRun => nodeRun.nodeName === nodeId)
    .sort((a, b) => {
      const aPos = a.id?.position ?? 0
      const bPos = b.id?.position ?? 0
      return bPos - aPos
    })

/** The layout options are shared with the headless layout tests. */
export const ELK_LAYOUT_OPTIONS = {
  'elk.algorithm': 'layered',
  'elk.direction': 'RIGHT',
  'elk.spacing.nodeNode': '75',
  'elk.layered.spacing.nodeNodeBetweenLayers': '120',
  'elk.spacing.edgeEdge': '25',
  'elk.spacing.edgeNode': '40',
  'elk.layered.spacing.edgeNodeBetweenLayers': '40',
  'elk.layered.spacing.edgeEdgeBetweenLayers': '25',
  'elk.edgeRouting': 'ORTHOGONAL',
  'elk.layered.nodePlacement.strategy': 'BRANDES_KOEPF',
  'elk.layered.cycleBreaking.strategy': 'DEPTH_FIRST',
  'elk.layered.considerModelOrder.strategy': 'NODES_AND_EDGES',
  'elk.layered.crossingMinimization.strategy': 'LAYER_SWEEP',
  'elk.layered.unnecessaryBendpoints': 'true',
  'elk.padding': '[top=50,left=50,bottom=50,right=50]',
  'elk.separateConnectedComponents': 'false',
  'org.eclipse.elk.layered.mergeEdges': 'false',
}

type LayoutManagerProps = {
  nodeRuns?: NodeRun[]
  viewportKey: string
  setNodes: (nodes: Node[] | ((nodes: Node[]) => Node[])) => void
  onLayoutComplete?: (nodes: Node[]) => void
}

export const LayoutManager: FC<LayoutManagerProps> = ({ nodeRuns, viewportKey, setNodes, onLayoutComplete }) => {
  const nodes = useStore(store => store.getNodes())
  const edges = useStore(store => store.edges)
  const setEdges = useStore(store => store.setEdges)
  const { fitView, setViewport } = useReactFlow()

  useOnViewportChange({
    onChange: useCallback(
      (viewport: Viewport) => {
        sessionStorage.setItem(viewportKey, JSON.stringify(viewport))
      },
      [viewportKey]
    ),
  })

  const onLoad = useCallback(
    async (nodes: Node[], edges: Edge[]) => {
      const elkGraph: ElkNode = {
        id: 'root',
        layoutOptions: ELK_LAYOUT_OPTIONS,
        children: nodes.map(node => ({
          id: node.id,
          width: node.width ?? 150,
          height: node.height ?? 50,
        })),
        edges: edges.map(edge => ({
          id: edge.id,
          sources: [edge.source],
          targets: [edge.target],
        })),
      }

      try {
        const laidOutGraph = await elk.layout(elkGraph)

        // ELK routed every edge orthogonally, respecting the edge-edge and
        // edge-node clearances configured above. Keep those routes: node
        // positions may NOT be adjusted after this point, or the routes (and
        // their clearances) stop being true.
        const routeById = new Map<string, RoutePoint[]>(
          (laidOutGraph.edges ?? []).flatMap(edge => {
            const section = edge.sections?.[0]
            if (!section) return []
            const points = [section.startPoint, ...(section.bendPoints ?? []), section.endPoint]
            return [[edge.id, points.map(p => ({ x: p.x, y: p.y }))]]
          })
        )

        const laidOutNodes = nodes.map(node => {
          const elkNode = laidOutGraph.children?.find(n => n.id === node.id)
          const nodeRunsList = getNodeRunsList(node.id, nodeRuns)
          const fade = nodeRunsList !== undefined && nodeRunsList.length === 0
          return {
            ...node,
            data: { ...node.data, fade, nodeRunsList },
            position: {
              x: elkNode?.x ?? 0,
              y: elkNode?.y ?? 0,
            },
            isLaidOut: true,
          }
        })
        setNodes(laidOutNodes)
        setEdges(
          edges.map(edge => ({
            ...edge,
            data: { ...edge.data, elkRoute: routeById.get(edge.id) },
          }))
        )
        onLayoutComplete?.(laidOutNodes)
        setTimeout(() => {
          const saved = sessionStorage.getItem(viewportKey)
          if (saved) {
            try {
              setViewport(JSON.parse(saved))
            } catch {
              fitView()
            }
          } else {
            fitView()
          }
        }, 10)
      } catch (error) {
        console.error('ELK layout error:', error)
      }
    },
    [fitView, setViewport, viewportKey, nodeRuns, setNodes, setEdges, onLayoutComplete]
  )

  useEffect(() => {
    if (
      nodes.some(
        (node: Node & { isLaidOut?: boolean }) =>
          node.width !== undefined && node.height !== undefined && !node.isLaidOut
      )
    ) {
      onLoad(nodes, edges)
    }
  }, [nodes, edges, onLoad])
  return <></>
}
