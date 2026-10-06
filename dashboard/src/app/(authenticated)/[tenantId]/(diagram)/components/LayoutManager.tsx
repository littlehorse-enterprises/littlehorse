import { NodeRun } from 'littlehorse-client/proto'
import { FC, useCallback, useEffect, useRef } from 'react'
import { Edge, Node, useOnViewportChange, useReactFlow, useStore, type Viewport } from 'reactflow'
import { nodeDimensions } from './nodeDimensions'
import { layoutDiagram } from './graphLayout'

export { ELK_LAYOUT_OPTIONS } from './graphLayout'

export const getNodeRunsList = (nodeId: string, nodeRuns?: NodeRun[]): NodeRun[] | undefined =>
  nodeRuns
    ?.filter(nodeRun => nodeRun.nodeName === nodeId)
    .sort((a, b) => {
      const aPos = a.id?.position ?? 0
      const bPos = b.id?.position ?? 0
      return bPos - aPos
    })

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
      try {
        const { graph: laidOutGraph, routes } = await layoutDiagram(nodes, edges)

        const laidOutNodes = nodes.map(node => {
          const elkNode = laidOutGraph.children?.find(n => n.id === node.id)
          const nodeRunsList = getNodeRunsList(node.id, nodeRuns)
          const fade = nodeRunsList !== undefined && nodeRunsList.length === 0
          const { w, h } = nodeDimensions(node.type, node.id)
          return {
            ...node,
            data: { ...node.data, fade, nodeRunsList },
            position: {
              // Center the real glyph in its reserved footprint so fixed-side
              // ports and rendered handles share the same orthogonal lane.
              x: (elkNode?.x ?? 0) + (w - (node.width ?? w)) / 2,
              y: (elkNode?.y ?? 0) + (h - (node.height ?? h)) / 2,
            },
            isLaidOut: true,
          }
        })
        setNodes(laidOutNodes)
        setEdges(
          edges.map(edge => ({
            ...edge,
            data: { ...edge.data, route: routes.get(edge.id) },
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
