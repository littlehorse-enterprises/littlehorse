import { useCallback } from 'react';
import { useReactFlow, XYPosition, useStoreApi } from 'reactflow';
import { useUI } from '../contexts/ui/provider';
import { useWorkflow } from '../contexts/workflow/provider';
import type { NodeType } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/extractNodes';
import { useCreateReactFlowNode } from './useCreateReactFlowNode';

interface UseNodeDropResult {
  handleNodeDrop: (nodeType: NodeType, screenPosition: XYPosition) => void
}

export function useNodeDrop(): UseNodeDropResult {
  const { setNodes } = useReactFlow();
  const storeApi = useStoreApi();
  const { actions: uiActions } = useUI();
  const { actions: wfActions } = useWorkflow();

  const handleNodeDrop = useCallback(
    (nodeType: NodeType, screenPosition: XYPosition) => {
      const flow = document.querySelector('.react-flow');
      const flowRect = flow?.getBoundingClientRect();
      
      // TODO: I had to do this because screenToFlowPosition was not working and couldn't find where the stale value was coming from
      const { transform } = storeApi.getState();
      const [ translateX, translateY, zoom ] = transform;

      const isInFlow =
        flowRect &&
        screenPosition.x >= flowRect.left &&
        screenPosition.x <= flowRect.right &&
        screenPosition.y >= flowRect.top &&
        screenPosition.y <= flowRect.bottom;

      if (isInFlow) {
        const position = {
          x: (screenPosition.x - flowRect.left - translateX) / zoom,
          y: (screenPosition.y - flowRect.top - translateY) / zoom,
        };
        
        const newNode = useCreateReactFlowNode(nodeType, position);

        // TODO: the node should be added to the workflowstate with onNodesChange
        setNodes(nodes => nodes.concat(newNode));
        uiActions.selectNode(newNode);
        wfActions.addNode(newNode.id, nodeType, newNode.data.taskName, newNode.data.varName);
      }
    },
    [setNodes, uiActions, wfActions]
  )

  return { handleNodeDrop }
}
