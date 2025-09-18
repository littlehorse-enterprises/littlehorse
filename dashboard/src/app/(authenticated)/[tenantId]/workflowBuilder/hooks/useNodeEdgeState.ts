import { useCallback, useEffect } from 'react'
import { addEdge, useEdgesState, useNodesState } from 'reactflow'
import type { Connection, Edge, Node, NodeChange, EdgeChange } from 'reactflow'

interface UseNodeEdgeStateResult {
  nodes: Node[]
  edges: Edge[]
  setNodes: (nodes: Node[]) => void
  setEdges: (edges: Edge[]) => void
  onNodesChange: (nodes: NodeChange[]) => void
  onEdgesChange: (edges: EdgeChange[]) => void
  onConnect: (params: Connection) => void
  resetState: () => void
}

export function useNodeEdgeState(onEdgesUpdate?: (edges: Edge[]) => void): UseNodeEdgeStateResult {
  const [nodes, setNodes, onNodesChange] = useNodesState<Node>([])
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([])

  const onConnect = useCallback((params: Connection) => setEdges(edges => addEdge(params, edges)), [])

  const resetState = useCallback(() => {
    setNodes([])
    setEdges([])
  }, [setNodes, setEdges])

  useEffect(() => {
    onEdgesUpdate?.(edges)
  }, [edges, onEdgesUpdate])

  return {
    nodes,
    edges,
    setNodes,
    setEdges,
    onNodesChange,
    onEdgesChange,
    onConnect,
    resetState,
  }
}
