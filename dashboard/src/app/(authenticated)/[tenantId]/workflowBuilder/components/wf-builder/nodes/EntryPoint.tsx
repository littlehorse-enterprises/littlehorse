'use client';

import { Handle, Position } from 'reactflow';

export function EntryPoint() {
  return (
    <div className="flex h-6 w-6 cursor-pointer rounded-xl border-[1px] border-gray-500 bg-green-200">
      <Handle type="source" position={Position.Right} className="bg-transparent" />
    </div>
  );
}
