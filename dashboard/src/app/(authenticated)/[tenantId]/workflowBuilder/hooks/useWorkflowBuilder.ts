import { useCallback } from 'react';
import { useReactFlow } from 'reactflow';
import { useWorkflow } from '../contexts/workflow/provider';
import { useELKLayout } from '../hooks/useELKLayout';
import { useReactFlowState } from '../hooks/useReactFlowState';
import { useWorkflowInteractions } from '../hooks/useWorkflowInteractions';
import { toast } from 'sonner';
import type { Node, Edge, Connection, NodeChange, EdgeChange } from 'reactflow';
import type { LayoutDirection } from '../types';
import { useUI } from '../contexts/ui/provider';

interface UseWorkflowBuilderResult {
  nodes: Node[]
  edges: Edge[]
  onNodesChange: (nodes: NodeChange[]) => void
  onEdgesChange: (edges: EdgeChange[]) => void
  onConnect: (params: Connection) => void
  onNodeClick: (event: React.MouseEvent, node: Node) => void
  onEdgeClick: (event: React.MouseEvent, edge: Edge) => void
  onPaneClick: () => void
  handleLayout: (direction: LayoutDirection) => void
}

export function useWorkflowBuilder(): UseWorkflowBuilderResult {
  const { actions: wfActions } = useWorkflow();
  const { actions: uiActions } = useUI();
  const { fitView } = useReactFlow();

  const { 
    nodes, 
    edges, 
    setNodes, 
    setEdges, 
    onNodesChange, 
    onEdgesChange, 
    onConnect, 
  } = useReactFlowState(wfActions.setOutgoingEdges);

  const { getLayoutedElements } = useELKLayout();
  const { onNodeClick, onEdgeClick, onPaneClick } = useWorkflowInteractions();

  const handleLayout = useCallback(
    async (direction: LayoutDirection) => {
      try {
        const result = await getLayoutedElements(nodes, edges, direction);
        setNodes(result.nodes);
        setEdges(result.edges);

        requestAnimationFrame(() => {
          fitView();
        })
      } catch (error) {
        console.error('Layout error:', error);
        toast.error('Failed to apply layout');
      }
    },
    [nodes, edges, getLayoutedElements, setNodes, setEdges, fitView]
  );

  return {
    nodes,
    edges,
    onNodesChange,
    onEdgesChange,
    onConnect,
    onNodeClick,
    onEdgeClick,
    onPaneClick,
    handleLayout,
  };
}
