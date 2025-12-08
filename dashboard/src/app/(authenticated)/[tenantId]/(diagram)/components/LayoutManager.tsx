import ELK from 'elkjs/lib/elk.bundled.js'
import { NodeRun } from 'littlehorse-client/proto'
import { FC, useCallback, useEffect } from 'react'
import { Edge, Node, useReactFlow, useStore } from 'reactflow'

const elk = new ELK()

export const LayoutManager: FC<{ nodeRuns?: NodeRun[] }> = ({ nodeRuns }) => {
  const nodes = useStore(store => store.getNodes())
  const edges = useStore(store => store.edges)
  const setNodes = useStore(store => store.setNodes)
  const setEdges = useStore(store => store.setEdges)
  const { fitView } = useReactFlow()

  const onLoad = useCallback(
    async (nodes: Node[], edges: Edge[]) => {
      const elkGraph = {
        id: 'root',
        layoutOptions: {
          'elk.algorithm': 'layered',
          'elk.direction': 'RIGHT',
          'elk.spacing.nodeNode': '100',
          'elk.layered.spacing.nodeNodeBetweenLayers': '150',
          'elk.spacing.edgeEdge': '30',
          'elk.spacing.edgeNode': '30',
          'elk.edgeRouting': 'ORTHOGONAL',
          'elk.layered.nodePlacement.strategy': 'SIMPLE',
          'elk.layered.cycleBreaking.strategy': 'GREEDY',
          'elk.padding': '[top=50,left=50,bottom=50,right=50]',
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
        setTimeout(() => fitView(), 10)
      } catch (error) {
        console.error('ELK layout error:', error)
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
