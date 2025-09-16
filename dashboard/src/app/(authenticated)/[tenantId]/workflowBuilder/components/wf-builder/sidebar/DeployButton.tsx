'use client';

import { Button } from '../../../components/ui/button';
import { useCallback, useState } from 'react';
import { useDeployWorkflow } from '../../../hooks/useDeployWorkflow';

export function DeployButton() {
  const [isDeploying, setIsDeploying] = useState(false);
  const { deploy } = useDeployWorkflow();

  const handleDeployWorkflow = useCallback(async () => {
    setIsDeploying(true);
    await deploy();
    setIsDeploying(false);
  }, [deploy]);

  return (
    <>
    <div className="flex justify-center mt-6">
      <Button onClick={handleDeployWorkflow} disabled={isDeploying}>Deploy Workflow</Button>
    </div>
    </>
  );
}
