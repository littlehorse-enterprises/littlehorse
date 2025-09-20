import { useReactFlow } from 'reactflow';
import { useCallback } from 'react';
import { useUI } from '../contexts/ui/provider';

export function useEdgeDataEditor(edgeId: string) {
  const { setEdges } = useReactFlow();
  const { actions: uiActions } = useUI();

  const handleDelete = useCallback(() => {
    setEdges(edges => edges.filter(edge => edge.id !== edgeId));
    uiActions.selectEdge(null);
  }, [setEdges, edgeId, uiActions])

  return {
    handleDelete,
  };
}
