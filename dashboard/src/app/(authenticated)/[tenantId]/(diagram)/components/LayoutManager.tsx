import ELK from 'elkjs/lib/elk.bundled.js'
import { NodeRun } from 'littlehorse-client/proto'
import { FC, useCallback, useEffect } from 'react'
import { Edge, Node, useOnViewportChange, useReactFlow, useStore, type Viewport } from 'reactflow'

const elk = new ELK()

export const LayoutManager: FC<{ nodeRuns?: NodeRun[]; viewportKey: string }> = ({ nodeRuns, viewportKey }) => {
  const nodes = useStore(store => store.getNodes())
  const edges = useStore(store => store.edges)
  const setNodes = useStore(store => store.setNodes)
  const setEdges = useStore(store => store.setEdges)
  const { fitView, setViewport } = useReactFlow()

  useOnViewportChange({
    onChange: useCallback(
      (viewport: Viewport) => {
        console.log('viewport', viewport)
        sessionStorage.setItem(viewportKey, JSON.stringify(viewport))
      },
      [viewportKey]
    ),
  })

  const onLoad = useCallback(
    async (nodes: Node[], edges: Edge[]) => {
      const elkGraph = {
        id: 'root',
        layoutOptions: {
          'elk.algorithm': 'layered',
          'elk.direction': 'RIGHT',
          'elk.spacing.nodeNode': '150',
          'elk.layered.spacing.nodeNodeBetweenLayers': '200',
          'elk.spacing.edgeEdge': '100',
          'elk.spacing.edgeNode': '50',
          'elk.edgeRouting': 'ORTHOGONAL',
          'elk.layered.nodePlacement.strategy': 'LINEAR_SEGMENTS',
          'elk.layered.cycleBreaking.strategy': 'DEPTH_FIRST',
          'elk.layered.considerModelOrder.strategy': 'NODES_AND_EDGES',
          'elk.layered.crossingMinimization.strategy': 'LAYER_SWEEP',
          'elk.layered.unnecessaryBendpoints': 'true',
          'elk.layered.compaction.postCompaction.strategy': 'EDGE_LENGTH',
          'elk.padding': '[top=100,left=100,bottom=100,right=100]',
          'elk.separateConnectedComponents': 'false',
          'org.eclipse.elk.layered.mergeEdges': 'false',
        },
        children: nodes.map(node => ({
          id: node.id,
          width: node.width ?? 150,
          height: node.height ?? 50,
        })),
        edges: edges.map(edge => ({
          id: `${edge.source}-${edge.target}`,
          sources: [edge.source],
          targets: [edge.target],
        })),
      }

      try {
        const laidOutGraph = await elk.layout(elkGraph)
        const hasCycles = nodes.some(node => node.type === 'cycle')
        // Layout the original workflow nodes
        const laidOutNodes = nodes.map(node => {
          const elkNode = laidOutGraph.children?.find(n => n.id === node.id)
          const nodeRunsList = nodeRuns
            ?.filter(nodeRun => nodeRun.nodeName === node.id)
            .sort((a, b) => {
              const aPos = a.id?.position ?? 0
              const bPos = b.id?.position ?? 0
              return bPos - aPos
            })
          const fade = nodeRunsList !== undefined && nodeRunsList.length === 0
          if (node.type === 'cycle' && elkNode?.x !== undefined) {
            const initialNode = laidOutGraph.children?.find(n => n.id === node.data.outgoingEdges[0].sinkNodeName)
            const cycleNodeX = elkNode.x - initialNode?.x!
            elkNode.x = (initialNode?.x! + cycleNodeX) / 2
          }

          if (node.type === 'exit' && hasCycles) {
            const initialNode = laidOutGraph.children?.find(n => n.id.includes('ENTRYPOINT'))
            if (elkNode && initialNode?.y !== undefined && elkNode.y !== initialNode.y) {
              elkNode.y = initialNode.y
            }
          }
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
        setEdges(edges)
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
    [fitView, setViewport, viewportKey, nodeRuns, setNodes, setEdges]
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
