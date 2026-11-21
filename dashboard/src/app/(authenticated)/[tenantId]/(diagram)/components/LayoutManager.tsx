import { NodeRun } from 'littlehorse-client/proto'
import { FC, useCallback, useEffect, useRef } from 'react'
import { Edge, Node, useOnViewportChange, useReactFlow, useStore, type Viewport } from 'reactflow'
import { footprintOf, layoutDiagram } from './graphLayout'

export { ELK_LAYOUT_OPTIONS } from './graphLayout'

export const getNodeRunsList = (nodeId: string, nodeRuns?: NodeRun[]): NodeRun[] | undefined =>
  nodeRuns
    ?.filter(nodeRun => nodeRun.nodeName === nodeId)
    .sort((a, b) => {
      const aPos = a.id?.position ?? 0
      const bPos = b.id?.position ?? 0
      return bPos - aPos
    })

export type NodeRunsResolver = (node: Node) => NodeRun[] | undefined

type LayoutManagerProps = {
  getNodeRuns: NodeRunsResolver
  /**
   * Identity of the graph currently displayed (spec/run/thread). Layout runs
   * exactly once per key: reruns triggered by anything else — selection
   * changes, modals opening, node re-measurement on container resize — must
   * not re-layout or touch the user's viewport.
   */
  layoutKey: string
  /**
   * The spec/run/thread being shown. A new layout of the same graph (an
   * exception handler shown, hidden, collapsed or expanded) fits the view to
   * the new shape instead of restoring the saved viewport.
   */
  graphKey?: string
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
  getNodeRuns,
  layoutKey,
  graphKey,
  viewportKey,
  setNodes,
  setEdges,
  onLayoutComplete,
}) => {
  const nodes = useStore(store => store.getNodes())
  const edges = useStore(store => store.edges)
  const { fitView, setViewport } = useReactFlow()
  const currentLayoutKey = useRef(layoutKey)
  currentLayoutKey.current = layoutKey
  const laidOutGraphKey = useRef<string | undefined>(undefined)

  useOnViewportChange({
    onChange: useCallback(
      (viewport: Viewport) => {
        sessionStorage.setItem(viewportKey, JSON.stringify(viewport))
      },
      [viewportKey]
    ),
  })

  const onLoad = useCallback(
    async (nodes: Node[], edges: Edge[], key: string) => {
      try {
        const { boxes, routes } = await layoutDiagram(nodes, edges)
        if (currentLayoutKey.current !== key) return

        const laidOutNodes = nodes.map(node => {
          const elkNode = boxes.get(node.id)
          if (node.type === 'exceptionHandler') {
            return {
              ...node,
              data: { ...node.data, size: { w: elkNode?.width ?? 0, h: elkNode?.height ?? 0 } },
              position: { x: elkNode?.x ?? 0, y: elkNode?.y ?? 0 },
              isLaidOut: true,
            }
          }
          const { w, h } = footprintOf(node)
          // A collapsed handler chip has no NodeRuns of its own; Diagram sets its run state.
          const runData =
            node.type === 'exceptionHandlerChip'
              ? {}
              : (() => {
                  const nodeRunsList = getNodeRuns(node)
                  return { fade: nodeRunsList !== undefined && nodeRunsList.length === 0, nodeRunsList }
                })()
          return {
            ...node,
            data: { ...node.data, ...runData },
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
        const reshaped = graphKey !== undefined && laidOutGraphKey.current === graphKey
        laidOutGraphKey.current = graphKey
        setTimeout(() => {
          if (currentLayoutKey.current !== key) return
          if (reshaped) {
            fitView({ duration: 400 })
            return
          }
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
    [fitView, setViewport, viewportKey, graphKey, getNodeRuns, setNodes, setEdges, onLayoutComplete]
  )

  const laidOutKey = useRef<string | null>(null)
  useEffect(() => {
    if (laidOutKey.current === layoutKey) return
    // ELK's inputs are deterministic (nodeDimensions), but waiting for
    // reactflow's first measurement pass guarantees the instance is live, so
    // the post-layout fitView/viewport restore isn't a no-op on a cold load.
    // The store may still contain the previous thread's measured nodes while
    // React Flow synchronizes the new controlled nodes.
    const ready =
      nodes.length > 0 &&
      nodes.every(node => node.data.layoutKey === layoutKey && node.width !== undefined && node.height !== undefined)
    if (!ready) return
    laidOutKey.current = layoutKey
    onLoad(nodes, edges, layoutKey)
  }, [layoutKey, nodes, edges, onLoad])
  return <></>
}
