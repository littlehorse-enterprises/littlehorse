import { useMemo } from 'react';
import { DraggableNode } from './DraggableNode';
import type { NodeType } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/extractNodes';
import nodeTypes from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes';

const nodeLabels: Record<NodeType, string> = {
  entrypoint: 'Entry Point',
  exit: 'Exit',
  task: 'Task',
  externalEvent: 'External Event',
  nop: 'No Operation',
  sleep: 'Sleep',
  startMultipleThreads: 'Start Multiple Threads',
  startThread: 'Start Thread',
  throwEvent: 'Throw Event',
  userTask: 'User Task',
  waitForCondition: 'Wait For Condition',
  waitForThreads: 'Wait For Threads'
}

export function DraggableNodes() {
  const availableNodeTypes = useMemo(() => Object.keys(nodeTypes) as NodeType[], []);
  
  return (
    <div className="space-y-2">
      {availableNodeTypes.map((nodeType) => (
        <DraggableNode
          key={nodeType}
          nodeType={nodeType}
        >
          {nodeLabels[nodeType]}
        </DraggableNode>
      ))}
    </div>
  )
}
