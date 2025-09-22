import { useCallback } from 'react';
import { useWorkflow } from '../contexts/workflow/provider';
import { useUI } from '../contexts/ui/provider';
import { useReactFlow } from 'reactflow';

export function useReset() {
  const { actions: wfActions } = useWorkflow();
  const { actions: uiActions } = useUI();
  const { setNodes, setEdges } = useReactFlow();

  const resetAll = useCallback(() => {
    wfActions.resetWorkflow();
    uiActions.resetUI();
    setNodes([]);
    setEdges([]);
  }, [wfActions, uiActions, setNodes, setEdges]);

  return { resetAll };
}
