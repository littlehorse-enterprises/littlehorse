import dagre from 'dagre'
import { FC, useEffect } from 'react'
import { useReactFlow, useStore } from 'reactflow'

export const Layouter: FC<{}> = () => {
  const nodes = useStore(store => store.getNodes())
  const edges = useStore(store => store.edges)
  const setNodes = useStore(store => store.setNodes)
  const setEdges = useStore(store => store.setEdges)
  const { fitView } = useReactFlow()

  useEffect(() => {
    const onLoad = () => {
      const dagreGraph = new dagre.graphlib.Graph()
      dagreGraph.setDefaultEdgeLabel(() => ({}))
      dagreGraph.setGraph({ rankdir: 'LR', align: 'UL', ranksep: 100 })
      nodes.forEach(node => {
        dagreGraph.setNode(node.id, { width: node.width, height: node.height })
      })

      edges.forEach(edge => {
        dagreGraph.setEdge(edge.source, edge.target, { width: edge.label ? 200 : undefined })
      })

      dagre.layout(dagreGraph)

      const layoutedNodes = nodes.map(node => {
        const nodeWithPosition = dagreGraph.node(node.id)
        return {
          ...node,
          position: { x: nodeWithPosition.x - node.width! / 2, y: nodeWithPosition.y - node.height! / 2 },
        }
      })

      setNodes(layoutedNodes)
      fitView()
    }

    if (
      nodes.some(
        node => node.width !== undefined && node.height !== undefined && node.position.x === 0 && node.position.y === 0
      )
    ) {
      onLoad()
    }
  }, [nodes, setNodes, setEdges, fitView, edges])
  return <></>
}
