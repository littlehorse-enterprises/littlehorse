import { useCallback } from 'react'
import type { Node, Edge } from 'reactflow'
import { useUI } from '../contexts/ui/provider'

interface UseWorkflowInteractionsResult {
  onNodeClick: (event: React.MouseEvent, node: Node) => void
  onEdgeClick: (event: React.MouseEvent, edge: Edge) => void
  onPaneClick: () => void
}
export function useWorkflowInteractions(): UseWorkflowInteractionsResult {
  const { actions: uiActions } = useUI()

  const onNodeClick = useCallback(
    (_event: React.MouseEvent, node: Node) => {
      uiActions.selectNode(node)
    },
    [uiActions]
  )

  const onEdgeClick = useCallback(
    (_event: React.MouseEvent, edge: Edge) => {
      uiActions.selectEdge(edge)
    },
    [uiActions]
  )

  const onPaneClick = useCallback(() => {
    uiActions.selectNode(null)
  }, [uiActions])

  return {
    onNodeClick,
    onEdgeClick,
    onPaneClick,
  }
}
