'use client';

import { DraggableNode } from './DraggableNode';
import { useUI } from '../../../contexts/ui/provider';
import { NodeDataPanel } from './NodeDataPanel';
import { EdgeDataPanel } from './EdgeDataPanel';
import { DeployButton } from './DeployButton';
import { useNodeDrop } from '../../../hooks/useNodeDrop';
import { NodeType } from '../../../types';
import { ResetButton } from './ResetButton';

interface SidebarProps {
  onReset: () => void;
}

export function Sidebar({ onReset }: SidebarProps) {
  const { state: uiState } = useUI();
  const { handleNodeDrop } = useNodeDrop();
 
  return (
    <aside className="px-2 py-3 text-xs bg-gray-900 md:w-1/5 md:max-w-[250px]">
      <DraggableNode nodeType={NodeType.ENTRY_POINT} onDrop={handleNodeDrop}>
        Entry Point
      </DraggableNode>
      <DraggableNode nodeType={NodeType.TASK_NODE} onDrop={handleNodeDrop}>
        Task Node
      </DraggableNode>
      <DraggableNode nodeType={NodeType.EXIT_POINT} onDrop={handleNodeDrop}>
        Exit Point
      </DraggableNode>
      {uiState.selectedNode && (
        <NodeDataPanel node={uiState.selectedNode} />
      )}
      {uiState.selectedEdge && (
        <EdgeDataPanel edge={uiState.selectedEdge} />
      )}
      <DeployButton />
      <ResetButton onReset={onReset} />
    </aside>
  );
}
