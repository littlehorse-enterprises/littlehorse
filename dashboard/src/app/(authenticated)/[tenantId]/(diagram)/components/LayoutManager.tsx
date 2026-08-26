import ELK, { type ElkNode } from 'elkjs/lib/elk.bundled.js'
import { NodeRun } from 'littlehorse-client/proto'
import { FC, useCallback, useEffect, useRef } from 'react'
import { Edge, Node, useOnViewportChange, useReactFlow, useStore, type Viewport } from 'reactflow'
import type { RoutePoint } from './EdgeTypes/elkRoute'
import { nodeDimensions } from './nodeDimensions'

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
  /**
   * Identity of the graph currently displayed (spec/run/thread). Layout runs
   * exactly once per key: reruns triggered by anything else — selection
   * changes, modals opening, node re-measurement on container resize — must
   * not re-layout or touch the user's viewport.
   */
  layoutKey: string
  viewportKey: string
  setNodes: (nodes: Node[] | ((nodes: Node[]) => Node[])) => void
  /**
   * The parent's useEdgesState setter. Routes MUST be written through the
   * controlled `edges` prop, exactly like node positions: writing them only
   * into reactflow's internal store looks right until the next parent
   * re-render, when the route-less prop re-syncs the store and every edge
   * falls back to the naive path — the diagram visibly "reshuffles" on the
   * first click.
   */
  setEdges: (edges: Edge[] | ((edges: Edge[]) => Edge[])) => void
  onLayoutComplete?: (nodes: Node[]) => void
}

export const LayoutManager: FC<LayoutManagerProps> = ({
  nodeRuns,
  layoutKey,
  viewportKey,
  setNodes,
  setEdges,
  onLayoutComplete,
}) => {
  const nodes = useStore(store => store.getNodes())
  const edges = useStore(store => store.edges)
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
        children: nodes.map(node => {
          // Deterministic footprints (see nodeDimensions.ts): layout must not
          // depend on live-measured DOM sizes, or any re-layout after a
          // remount/HMR/selection reshuffles an already-displayed diagram.
          const { w, h } = nodeDimensions(node.type, node.id)
          return { id: node.id, width: w, height: h }
        }),
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

  const laidOutKey = useRef<string | null>(null)
  useEffect(() => {
    if (laidOutKey.current === layoutKey) return
    // ELK's inputs are deterministic (nodeDimensions), but waiting for
    // reactflow's first measurement pass guarantees the instance is live, so
    // the post-layout fitView/viewport restore isn't a no-op on a cold load.
    const ready = nodes.length > 0 && nodes.every(node => node.width !== undefined && node.height !== undefined)
    if (!ready) return
    laidOutKey.current = layoutKey
    onLoad(nodes, edges)
  }, [layoutKey, nodes, edges, onLoad])
  return <></>
}
