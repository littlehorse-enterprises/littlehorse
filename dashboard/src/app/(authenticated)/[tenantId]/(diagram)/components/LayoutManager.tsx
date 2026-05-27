import { NodeRun } from 'littlehorse-client/proto'
import { FC, useCallback, useEffect } from 'react'
import { Edge, Node, useOnViewportChange, useReactFlow, useStore, type Viewport } from 'reactflow'
import { layoutWorkflow } from '../layout/workflowLayout'

export const LayoutManager: FC<{ nodeRuns?: NodeRun[]; viewportKey: string }> = ({ nodeRuns, viewportKey }) => {
  const nodes = useStore(store => store.getNodes())
  const edges = useStore(store => store.edges)
  const setNodes = useStore(store => store.setNodes)
  const setEdges = useStore(store => store.setEdges)
  const { fitView } = useReactFlow()

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
        const { nodes: laidOutNodes, edges: laidOutEdges } = await layoutWorkflow(nodes, edges)

        const nodesWithRuns = laidOutNodes.map(node => {
          const nodeRunsList = nodeRuns
            ?.filter(nodeRun => nodeRun.nodeName === node.id)
            .sort((a, b) => {
              const aPos = a.id?.position ?? 0
              const bPos = b.id?.position ?? 0
              return bPos - aPos
            })
          const fade = nodeRunsList !== undefined && nodeRunsList.length === 0
          return {
            ...node,
            data: { ...node.data, fade, nodeRunsList },
          }
        })

        setNodes(nodesWithRuns)
        setEdges(laidOutEdges)

        setTimeout(() => {
          fitView({ padding: 0.05, duration: 200 })
        }, 10)
      } catch (error) {
        console.error('Workflow layout error:', error)
      }
    },
    [fitView, nodeRuns, setNodes, setEdges]
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
