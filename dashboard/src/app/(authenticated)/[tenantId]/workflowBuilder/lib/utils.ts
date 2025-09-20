import { VariableType, Comparator } from 'littlehorse-client/proto';
import { NodeType } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/extractNodes';

import type {
  PutWfSpecRequest,
  PutTaskDefRequest,
  TaskDef,
  EntrypointNode,
  ExitNode,
  TaskNode,
  ExternalEventNode,
  StartThreadNode,
  WaitForThreadsNode,
  NopNode,
  SleepNode,
  UserTaskNode,
  StartMultipleThreadsNode,
  ThrowEventNode,
  WaitForConditionNode,
} from 'littlehorse-client/proto';

import type { WorkflowState } from '../types';
import type { Node as ReactFlowNode } from 'reactflow';
import type { Node } from 'littlehorse-client/proto';
import { DEFAULT_TIMEOUT_SECONDS } from '../lib/constants';

export function createNodeValue(nodeType: NodeType, taskName?: string, varName?: string):
  | EntrypointNode
  | ExitNode
  | TaskNode
  | ExternalEventNode
  | StartThreadNode
  | WaitForThreadsNode
  | NopNode
  | SleepNode
  | UserTaskNode
  | StartMultipleThreadsNode
  | ThrowEventNode
  | WaitForConditionNode {
  switch (nodeType) {
    case 'entrypoint':
      return {} as EntrypointNode;

    case 'exit':
      return {} as ExitNode;

    case 'task':
      return {
        taskToExecute: {
          $case: 'taskDefId',
          value: { name: taskName ? taskName : '' },
        },
        timeoutSeconds: DEFAULT_TIMEOUT_SECONDS,
        retries: 0,
        variables: varName ? [{
          source: {
            $case: 'variableName',
            value: varName
          }
        }] : []
      } as TaskNode;

    case 'externalEvent':
      return {
        externalEventDefId: { name: '' },
        timeoutSeconds: {
          $case: 'literalValue',
          value: { int: DEFAULT_TIMEOUT_SECONDS },
        },
        maskCorrelationKey: false,
      } as ExternalEventNode;

    case 'startThread':
      return {
        threadSpecName: '',
        variables: {},
      } as StartThreadNode;

    case 'waitForThreads':
      return {
        threadsToWaitFor: {
          $case: 'threads',
          value: { threads: [] },
        },
        perThreadFailureHandlers: [],
      } as WaitForThreadsNode;

    case 'nop':
      return {} as NopNode;

    case 'sleep':
      return {
        rawSeconds: {
          $case: 'literalValue',
          value: { int: 1 },
        },
      } as SleepNode;

    case 'userTask':
      return {
        userTaskDefName: '',
        userTaskDefVersion: 0,
        userGroup: {
          $case: 'literalValue',
          value: { str: '' },
        },
        userIds: [],
        actions: [],
      } as UserTaskNode;

    case 'startMultipleThreads':
      return {
        threadSpecName: '',
        variables: {},
        iterable: {
          $case: 'variable',
          value: { variableName: '' },
        },
      } as StartMultipleThreadsNode;

    case 'throwEvent':
      return {
        eventDefId: { name: '' },
        content: {
          $case: 'literalValue',
          value: { str: '' },
        },
      } as ThrowEventNode;

    case 'waitForCondition':
      return {
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
      } as WaitForConditionNode;  

    default:
      throw new Error(`Unknown node type: ${nodeType}`);
  }
}

export function extractTasksInfo(spec: PutWfSpecRequest): Array<TaskDef> {
  const tasksMap = new Map<string, TaskDef>();

  Object.values(spec.threadSpecs).forEach(threadSpec => {
    Object.values(threadSpec.nodes).forEach(node => {
      // TODO: consider dynamic tasks
      if (node.node?.$case === 'task' && node.node.value.taskToExecute?.$case === 'taskDefId') {
        const taskName = node.node.value.taskToExecute.value.name;
        const variables = node.node.value.variables || [];

        const inputVars = variables
          .filter(variable => variable.source?.$case === 'variableName')
          .map(variable => ({
            name: variable.source!.value as string,
            typeDef: {
              type: VariableType.STR,
              masked: false,
            },
          }));

        if (!tasksMap.has(taskName)) {
          tasksMap.set(taskName, {
            id: { name: taskName },
            inputVars,
            createdAt: undefined,
          });
        }
      }
    })
  })

  return Array.from(tasksMap.values());
}

export function createTaskDefRequest(taskInfo: TaskDef): PutTaskDefRequest {
  return {
    name: taskInfo.id?.name || '',
    inputVars: taskInfo.inputVars,
    returnType: {
      returnType: {
        type: VariableType.STR,
        masked: false,
      },
    },
  };
}

export function generateNodeId(): string {
  return `node${Date.now()}${Math.floor(Math.random() * 1000)}`; // TODO: set IDs in a store with shorter strings
}

// TODO: check if there is a less manual way to do this
const createReactFlowNodeData = (nodeType: NodeType, lhNode: Node, nodeId: string) => {
  const baseData = {
    nodeRunsList: [],
    fade: false,
    nodeNeedsToBeHighlighted: false,
  };

  switch (nodeType) {
    case 'task':
      if (lhNode.node?.$case === 'task') {
        const taskNode = lhNode.node.value;
        const reactFlowData = {
          ...baseData,
          taskToExecute: taskNode.taskToExecute,
        };
        return reactFlowData;
      }
    // TODO: add other node types
    case 'entrypoint':
    case 'exit':
    default:
      return baseData;
  }
}

export function convertNodes(workflowState: WorkflowState): ReactFlowNode[] {
  const nodes: ReactFlowNode[] = [];
  const workflowNodes = workflowState.spec.threadSpecs.entrypoint.nodes;

  Object.entries(workflowNodes).forEach(([nodeId, lhNode], index) => {
    if (!lhNode.node?.$case) return;

    const nodeType = lhNode.node.$case as NodeType;
    let position = { x: 100 + index * 200, y: 200 }; // TODO: use Dagre layout for positions

    if (nodeType === 'entrypoint') {
      position = { x: 100, y: 100 };
    } else if (nodeType === 'exit') {
      position = { x: 300 + index * 200, y: 100 };
    }

    nodes.push({
      id: nodeId,
      type: nodeType,
      position,
      data: createReactFlowNodeData(nodeType, lhNode, nodeId),
    });
  })

  return nodes;
}
