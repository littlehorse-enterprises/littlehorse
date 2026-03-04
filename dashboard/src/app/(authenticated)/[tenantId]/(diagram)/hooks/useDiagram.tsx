import { useContext } from 'react'
import { useStore } from 'reactflow'
import { DiagramContext, NodeInContext } from '../context'

export const useDiagram = () => {
  const { thread, setThread, setSelectedNode, selectedNode } = useContext(DiagramContext)
  const nodes = useStore<NonNullable<NodeInContext>[]>(state => state.getNodes() as NonNullable<NodeInContext>[])
  const edges = useStore(state => state.edges)
  const setEdges = useStore(state => state.setEdges)
  const setNodes = useStore(state => state.setNodes)

  return { thread, setThread, selectedNode, setSelectedNode, nodes, setNodes, edges, setEdges }
}
