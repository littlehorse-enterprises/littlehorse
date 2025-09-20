import type { WorkflowState, WorkflowAction } from '../../types';
import { WorkflowActionType } from '../../types';
import {
  handleAddNode,
  handleSetWorkflowName,
  handleRemoveNode,
  createInitialState,
  handleUpdateNodeData,
  handleSetOutgoingEdges,
} from './helpers';

export function workflowReducer(state: WorkflowState, action: WorkflowAction): WorkflowState {
  switch (action.type) {
    case WorkflowActionType.SET_WORKFLOW_NAME:
      return handleSetWorkflowName(state, action);

    case WorkflowActionType.ADD_NODE:
      return handleAddNode(state, action);

    case WorkflowActionType.REMOVE_NODE:
      return handleRemoveNode(state, action);

    case WorkflowActionType.RESET_WORKFLOW:
      return createInitialState();

    case WorkflowActionType.UPDATE_NODE_DATA:
      return handleUpdateNodeData(state, action);

    case WorkflowActionType.SET_OUTGOING_EDGES:
      return handleSetOutgoingEdges(state, action);

    default:
      return state;
  }
}
