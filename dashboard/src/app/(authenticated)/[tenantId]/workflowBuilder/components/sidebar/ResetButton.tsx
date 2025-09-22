'use client';

import { Button } from '@/components/ui/button';
import { useCallback } from 'react';
import { useReset } from '../../hooks/useReset';

export function ResetButton() {
  const { resetAll } = useReset();
  
  const handleResetWorkflow = useCallback(async () => {
    resetAll();
  }, [resetAll]);

  return (
    <div className="mt-2 flex justify-center">
      <Button onClick={handleResetWorkflow}>Reset Workflow</Button>
    </div>
  );
}
