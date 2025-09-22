'use client';

import 'reactflow/dist/style.css';

import { Background, ReactFlow, ReactFlowProvider, MiniMap } from 'reactflow';

import { Sidebar } from './sidebar/Sidebar';
import { LayoutPanel } from './LayoutPanel';
import { useWorkflowBuilder } from '../hooks/useWorkflowBuilder';
import nodeTypes from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes';

function WfBuilderFlow() {
  const {
    nodes,
    edges,
    onNodesChange,
    onEdgesChange,
    onConnect,
    onNodeClick,
    onEdgeClick,
    onPaneClick,
    handleLayout,
  } = useWorkflowBuilder();

  return (
    <div className="flex h-full grow flex-col md:flex-row">
      <Sidebar />
      <div className="h-full w-full grow">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          onNodeClick={onNodeClick}
          onEdgeClick={onEdgeClick}
          onPaneClick={onPaneClick}
          fitView
          nodeTypes={nodeTypes}
          fitViewOptions={{ includeHiddenNodes: false }}
        >
          <LayoutPanel handleLayout={handleLayout} />
          <MiniMap color="#111827" />
          <Background />
        </ReactFlow>
      </div>
    </div>
  )
}

export function WfBuilderContainer() {
  return (
    <ReactFlowProvider>
      <WfBuilderFlow />
    </ReactFlowProvider>
  )
}
