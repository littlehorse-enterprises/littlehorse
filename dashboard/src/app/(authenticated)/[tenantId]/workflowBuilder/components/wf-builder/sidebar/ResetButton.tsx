'use client';

import { Button } from '../../../components/ui/button';
import { useCallback } from 'react';

interface ResetButtonProps {
  onReset: () => void;
}

export function ResetButton({ onReset }: ResetButtonProps) {

  const handleResetWorkflow = useCallback(async () => {
    onReset();
  }, [onReset]);

  return (
    <>
    <div className="flex justify-center mt-2">
      <Button onClick={handleResetWorkflow}>Reset Workflow</Button>
    </div>
    </>
  );
}
