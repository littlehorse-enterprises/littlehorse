'use client';

import { Handle, Position } from 'reactflow';

export function ExitPoint() {
  return (
    <div className="flex h-6 w-6 cursor-pointer rounded-xl border-[3px] border-gray-500 bg-green-200">
      <Handle type="target" position={Position.Left} className="bg-transparent" />
    </div>
  );
}

