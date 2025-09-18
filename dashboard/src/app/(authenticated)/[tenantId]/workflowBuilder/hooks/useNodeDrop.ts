import { useCallback } from 'react'
import { useReactFlow, XYPosition } from 'reactflow'
import { generateNodeId } from '../lib/utils'
import { useUI } from '../contexts/ui/provider'
import { useWorkflow } from '../contexts/workflow/provider'
import type { NodeType } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/extractNodes'

interface UseNodeDropResult {
  handleNodeDrop: (nodeType: NodeType, screenPosition: XYPosition) => void
}

export function useNodeDrop(): UseNodeDropResult {
  const { setNodes, screenToFlowPosition } = useReactFlow()
  const { actions: uiActions } = useUI()
  const { actions: wfActions } = useWorkflow()

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
        let taskName, varName;

        const getNodeData = () => {
          const baseData = {
            nodeRunsList: [],
            fade: false,
            nodeNeedsToBeHighlighted: false,
          }

          switch (nodeType) {
            case 'task':
              taskName = `task-${nodeId}`;
              varName = '';
              return {
                ...baseData,
                taskToExecute: {
                  $case: 'taskDefId' as const,
                  value: { name: taskName },
                },
                variables: [],
                retries: 0,
                timeoutSeconds: 60,
              }
            case 'entrypoint':
            case 'exit':
            default:
              return baseData
          }
        }

        const newNode = {
          id: nodeId,
          type: nodeType,
          position,
          data: getNodeData(),
        }

        setNodes(nodes => nodes.concat(newNode))
        uiActions.selectNode(newNode)

        wfActions.addNode(nodeId, nodeType, taskName, varName)
      }
    },
    [setNodes, screenToFlowPosition, uiActions, wfActions]
  )

  return { handleNodeDrop }
}
