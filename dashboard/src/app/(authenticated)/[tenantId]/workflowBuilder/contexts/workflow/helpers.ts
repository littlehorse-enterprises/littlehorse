import { produce } from 'immer';
import type { WorkflowState, WorkflowAction } from '../../types';
import { WorkflowActionType } from '../../types';
import type { Node, ThreadVarDef } from 'littlehorse-client/proto'
import { VariableType, WfRunVariableAccessLevel, AllowedUpdateType } from 'littlehorse-client/proto';
import type { NodeType } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/extractNodes';
import { createNodeValue } from '../../lib/utils';

// TODO: move this whole thing to utils and split into different node types for parametrization
function createNode(nodeType: NodeType, taskName?: string, varName?: string): Node {
  const nodeValue = createNodeValue(nodeType, taskName, varName);

  const node = {
    outgoingEdges: [],
    failureHandlers: [],
    node: {
      $case: nodeType,
      value: nodeValue,
    } as Node['node']
  }

  return node;
}

function createVariableDef(varName: string): ThreadVarDef {
  return {
    varDef: {
      name: varName,
      typeDef: { type: VariableType.STR, masked: false },
    },
    required: true,
    searchable: false,
    accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
    jsonIndexes: [],
  }
}

export function handleSetWorkflowName(
  state: WorkflowState,
  action: Extract<WorkflowAction, { type: WorkflowActionType.SET_WORKFLOW_NAME }>
): WorkflowState {
  return produce(state, draft => {
    draft.spec.name = action.payload;
  })
}

export function handleAddNode(
  state: WorkflowState,
  action: Extract<WorkflowAction, { type: WorkflowActionType.ADD_NODE }>
): WorkflowState {
  const { nodeId, nodeType, taskName, varName } = action.payload;
  const newNode = createNode(nodeType, taskName, varName);
  const newVarDef: ThreadVarDef | undefined = varName ? createVariableDef(varName) : undefined;

  return produce(state, draft => {
    draft.spec.threadSpecs.entrypoint.nodes[nodeId] = newNode;

    if (newVarDef) {
      draft.spec.threadSpecs.entrypoint.variableDefs = [
        newVarDef,
        ...(draft.spec.threadSpecs.entrypoint.variableDefs || []),
      ];
    }
  })
}

export function handleRemoveNode(
  state: WorkflowState,
  action: Extract<WorkflowAction, { type: WorkflowActionType.REMOVE_NODE }>
): WorkflowState {
  return produce(state, draft => {
    delete draft.spec.threadSpecs.entrypoint.nodes[action.payload];

    Object.values(draft.spec.threadSpecs.entrypoint.nodes).forEach(node => {
      if (node.outgoingEdges?.length > 0) {
        node.outgoingEdges = node.outgoingEdges.filter(edge => edge.sinkNodeName !== action.payload);
      }
    });
  })
}

export function handleUpdateNodeData(
  state: WorkflowState,
  action: Extract<WorkflowAction, { type: WorkflowActionType.UPDATE_NODE_DATA }>
): WorkflowState {
  const { nodeId, taskName, varName } = action.payload;

  return produce(state, draft => {
    const node = draft.spec.threadSpecs.entrypoint.nodes[nodeId].node;
    if (!node) return;

    if (node.$case === 'task' && taskName) {
      node.value.taskToExecute = {
        $case: 'taskDefId',
        value: { name: taskName },
      };
    }

    if (node.$case === 'task' && varName !== undefined) {
      node.value.variables = varName ? [{
        source: {
          $case: 'variableName',
          value: varName,
        },
      }]
      : [];
    }
  })
}

export function handleSetOutgoingEdges(
  state: WorkflowState,
  action: Extract<WorkflowAction, { type: WorkflowActionType.SET_OUTGOING_EDGES }>
): WorkflowState {
  const { edges } = action.payload;

  return produce(state, draft => {
    const nodes = draft.spec.threadSpecs.entrypoint.nodes;

    Object.values(nodes).forEach(node => {
      node.outgoingEdges = [];
    });

    edges.forEach(edge => {
      const node = nodes[edge.source];
      if (node) {
        node.outgoingEdges = [{
          sinkNodeName: edge.target,
          condition: undefined,
          variableMutations: [],
        }];
      }
    })
  });
}

export function createInitialState(): WorkflowState {
  return {
    spec: {
      name: `workflow${Date.now()}${Math.floor(Math.random() * 1000)}`,
      threadSpecs: {
        entrypoint: {
          nodes: {},
          variableDefs: [],
          interruptDefs: [],
        },
      },
      entrypointThreadName: 'entrypoint',
      allowedUpdates: AllowedUpdateType.ALL_UPDATES,
    },
  };
}
