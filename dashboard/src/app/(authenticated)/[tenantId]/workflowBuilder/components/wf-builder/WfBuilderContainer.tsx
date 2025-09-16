'use client';

import 'reactflow/dist/style.css';

import {
  Background,
  ReactFlow,
  ReactFlowProvider,
  MiniMap
} from 'reactflow';

import { Sidebar } from '../../components/wf-builder/sidebar/Sidebar';
import { LayoutPanel } from '../../components/wf-builder/LayoutPanel';
import { EntryPoint } from '../../components/wf-builder/nodes/EntryPoint';
import { ExitPoint } from '../../components/wf-builder/nodes/ExitPoint';
import { TaskNode } from '../../components/wf-builder/nodes/TaskNode';
import { NodeType } from '../../types';
import { useWorkflowBuilder } from '../../hooks/useWorkflowBuilder';

const nodeTypes = {
  [NodeType.ENTRY_POINT]: EntryPoint,
  [NodeType.EXIT_POINT]: ExitPoint,
  [NodeType.TASK_NODE]: TaskNode,
};

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
    onReset,
    handleLayout
  } = useWorkflowBuilder();
 
  return (
    <div className="flex flex-col grow h-full md:flex-row">
      <Sidebar onReset={onReset} />
      <div className="grow w-full h-full">
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
          <MiniMap
            color="#111827"
          />
          <Background />
        </ReactFlow>
      </div>
    </div>
  );
}

export function WfBuilderContainer() {
  return (
    <ReactFlowProvider>
      <WfBuilderFlow />
    </ReactFlowProvider>
  );
}
