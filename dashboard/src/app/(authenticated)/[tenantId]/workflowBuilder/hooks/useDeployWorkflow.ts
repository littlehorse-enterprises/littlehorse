import { useCallback, useEffect } from 'react';
import { useWorkflow } from '../contexts/workflow/provider';
import { deployWorkflow } from '../actions/deployWorkflow';
import { useUI } from '../contexts/ui/provider';
import { toast } from 'sonner';
import { useWhoAmI } from '@/contexts/WhoAmIContext';

interface DeployWorkflowResult {
  deploy: () => Promise<void>;
}

export function useDeployWorkflow(): DeployWorkflowResult {
  const { state: wfState, actions: wfActions } = useWorkflow();
  const { actions: uIActions } = useUI();
  const { tenantId } = useWhoAmI();

  useEffect(() => {
    if (!wfState.spec.name) {
      wfActions.setWorkflowName(`workflow-${Date.now()}${Math.floor(Math.random() * 1000)}`);
    }
  }, [wfState.spec.name, wfActions])

  const deploy = useCallback(async () => {
    uIActions.setLoading(true);

    try {
      await deployWorkflow(wfState.spec, tenantId);
      toast.success('Workflow deployed successfully');
    } catch (error) {
      toast.error('Failed to deploy workflow');
    } finally {
      uIActions.setLoading(false);
    }
  }, [wfState.spec, uIActions]);

  return {
    deploy,
  }
}
