'use client';

import { Handle, Position } from 'reactflow';
import { SettingsIcon } from 'lucide-react'

export function TaskNode() {
  return (
    <div className="flex cursor-pointer flex-col items-center rounded-md border-[1px] border-orange-500 px-2 pt-1 text-xs text-black bg-orange-300">
      <SettingsIcon className="h-4 w-4 fill-orange-500 stroke-black" />
      Task Node
      <Handle type="source" position={Position.Right} className="bg-transparent" />
      <Handle type="target" position={Position.Left} className="bg-transparent" />
    </div>
  );
}

