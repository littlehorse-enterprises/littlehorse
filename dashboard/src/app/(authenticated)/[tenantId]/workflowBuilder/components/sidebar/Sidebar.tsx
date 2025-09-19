'use client'

import { DraggableNodes } from './DraggableNodes'
import { useUI } from '../../contexts/ui/provider'
import { NodeDataPanel } from './NodeDataPanel'
import { EdgeDataPanel } from './EdgeDataPanel'
import { DeployButton } from './DeployButton'
import { ResetButton } from './ResetButton'
import { FC } from 'react'

interface SidebarProps {
  onReset: () => void
}

export const Sidebar: FC<SidebarProps> = ({ onReset }) => {
  const { state: uiState } = useUI();

  return (
    <aside className="bg-gray-900 px-2 py-3 text-xs md:w-1/5 md:max-w-[250px]">
      <DraggableNodes />
      {uiState.selectedNode && <NodeDataPanel node={uiState.selectedNode} />}
      {uiState.selectedEdge && <EdgeDataPanel edge={uiState.selectedEdge} />}
      <DeployButton />
      <ResetButton onReset={onReset} />
    </aside>
  )
}
