import { useCallback } from 'react'
import { useReactFlow, XYPosition } from 'reactflow'
import { generateNodeId } from '../lib/utils'
import { useUI } from '../contexts/ui/provider'
import { useWorkflow } from '../contexts/workflow/provider'
import type { NodeType } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/extractNodes'
import { DEFAULT_TIMEOUT_SECONDS } from '../lib/constants'
import { Comparator } from 'littlehorse-client/proto'

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
            nodeRunsList: nodeType === 'waitForCondition' ? [{ status: null }] : [], // TODO: check how status works on wait for condition, I'm passing null just to get it to render
            fade: false,
            nodeNeedsToBeHighlighted: false,
          }

          switch (nodeType) {
            // TODO: take a look at entrypoint, exit, and nop properties
            case 'entrypoint':
              return {
                ...baseData,
              }

            case 'exit':
              return {
                ...baseData,
              }

            case 'task':
              taskName = `task-${nodeId}`;
              varName = '';
              return {
                ...baseData,
                taskToExecute: {
                  $case: 'taskDefId' as const, // TODO: why as const?
                  value: { name: taskName },
                },
                variables: [],
                retries: 0,
                timeoutSeconds: DEFAULT_TIMEOUT_SECONDS,
              }

            case 'externalEvent':
              return {
                ...baseData,
                externalEventDefId: { name: '' },
                timeoutSeconds: {
                  $case: 'literalValue',
                  value: { int: DEFAULT_TIMEOUT_SECONDS },
                },
                maskCorrelationKey: false,
              }

            case 'startThread':
              return {
                ...baseData,
                threadSpecName: '',
                variables: {},
              }

            case 'waitForThreads':
              return {
                ...baseData,
                threadsToWaitFor: {
                  $case: 'threads',
                  value: { threads: [] },
                },
                perThreadFailureHandlers: [],
              }

            case 'nop':
              return {
                ...baseData,
              }

            case 'sleep':
              return {
                ...baseData,
                rawSeconds: {
                  $case: 'literalValue',
                  value: { int: 1 },
                },
              }

            case 'userTask':
              return {
                ...baseData,
                userTaskDefName: '',
                userTaskDefVersion: 0,
                userGroup: {
                  $case: 'literalValue',
                  value: { str: '' },
                },
                userIds: [],
                actions: [],
              }

            case 'startMultipleThreads':
              return {
                ...baseData,
                threadSpecName: '',
                variables: {},
                iterable: {
                  $case: 'variable',
                  value: { variableName: '' },
                },
              }

            case 'throwEvent':
              return {
                ...baseData,
                eventDefId: { name: `throw-event-${nodeId}` },
                content: {
                  $case: 'literalValue',
                  value: { str: '' },
                },
              }

            case 'waitForCondition':
              return {
                ...baseData,
                condition: {
                  comparator: Comparator.EQUALS,
                  left: {
                    $case: 'literalValue',
                    value: { bool: true },
                  },
                  right: {
                    $case: 'literalValue',
                    value: { bool: true },
                  },
                },
              }

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
