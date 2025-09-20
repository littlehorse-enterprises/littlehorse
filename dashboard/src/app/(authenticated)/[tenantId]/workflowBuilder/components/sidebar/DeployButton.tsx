'use client';

import { Button } from '@/components/ui/button';
import { useCallback, useState } from 'react';
import { useDeployWorkflow } from '../../hooks/useDeployWorkflow';

export function DeployButton() {
  const [isDeploying, setIsDeploying] = useState(false);
  const { deploy } = useDeployWorkflow();

  const handleDeployWorkflow = useCallback(async () => {
    setIsDeploying(true);
    await deploy();
    setIsDeploying(false);
  }, [deploy])

  return (
    <>
      <div className="mt-6 flex justify-center">
        <Button onClick={handleDeployWorkflow} disabled={isDeploying}>
          Deploy Workflow
        </Button>
      </div>
    </>
  )
}
