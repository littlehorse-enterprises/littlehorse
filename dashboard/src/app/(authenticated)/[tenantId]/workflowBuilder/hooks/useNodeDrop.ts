import { useCallback } from 'react';
import { useReactFlow, XYPosition } from 'reactflow';
import { generateNodeId } from '../lib/utils';
import { useUI } from '../contexts/ui/provider';
import { useWorkflow } from '../contexts/workflow/provider';
import { NodeType } from '../types';

interface UseNodeDropResult {
  handleNodeDrop: (nodeType: NodeType, screenPosition: XYPosition) => void;
}

export function useNodeDrop(): UseNodeDropResult {
  const { setNodes, screenToFlowPosition } = useReactFlow();
  const { actions: uiActions } = useUI();
  const { actions: wfActions } = useWorkflow();

  const handleNodeDrop = useCallback(
    (nodeType: NodeType, screenPosition: XYPosition) => {
      const flow = document.querySelector('.react-flow');
      const flowRect = flow?.getBoundingClientRect();
      const isInFlow =
        flowRect &&
        screenPosition.x >= flowRect.left &&
        screenPosition.x <= flowRect.right &&
        screenPosition.y >= flowRect.top &&
        screenPosition.y <= flowRect.bottom;

      if (isInFlow) {
        const position = screenToFlowPosition(screenPosition);
        const nodeId = generateNodeId();

        const newNode = {
          id: nodeId,
          type: nodeType,
          position,
          data: {
            ...(nodeType === NodeType.TASK_NODE && {
              taskName: `${nodeType.toLowerCase()}-node`,
              varName: ''
            })
          },
        };

        setNodes((nodes) => nodes.concat(newNode));
        uiActions.selectNode(newNode);
        wfActions.addNode(nodeId, nodeType, newNode.data.taskName, newNode.data.varName);
      }
    },
    [setNodes, screenToFlowPosition, uiActions, wfActions],
  );

  return { handleNodeDrop };
}
