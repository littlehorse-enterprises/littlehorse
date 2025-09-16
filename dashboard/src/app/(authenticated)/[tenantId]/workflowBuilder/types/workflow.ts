import type { PutWfSpecRequest, WfSpecId } from "littlehorse-client/proto";
import type { Edge as ReactFlowEdge } from 'reactflow';

export enum WorkflowActionType {
  SET_WORKFLOW_NAME = "SET_WORKFLOW_NAME",
  ADD_NODE = "ADD_NODE",
  REMOVE_NODE = "REMOVE_NODE",
  RESET_WORKFLOW = "RESET_WORKFLOW",
  UPDATE_NODE_DATA = "UPDATE_NODE_DATA",
  SET_OUTGOING_EDGES = "SET_OUTGOING_EDGES"
}

export enum NodeType {
  ENTRY_POINT = 'entryPoint',
  EXIT_POINT = 'exitPoint',
  TASK_NODE = 'taskNode',
}

export interface WorkflowState {
  spec: PutWfSpecRequest;
}

export type WorkflowAction =
  | { type: WorkflowActionType.SET_WORKFLOW_NAME; payload: string }
  | { type: WorkflowActionType.ADD_NODE; payload: { nodeId: string; nodeType: NodeType; taskName?: string; varName?: string } }
  | { type: WorkflowActionType.REMOVE_NODE; payload: string }
  | { type: WorkflowActionType.RESET_WORKFLOW }
  | { type: WorkflowActionType.UPDATE_NODE_DATA; payload: { nodeId: string; taskName?: string; varName?: string } }
  | { type: WorkflowActionType.SET_OUTGOING_EDGES; payload: { edges: ReactFlowEdge[] } }

export interface WorkflowContextValue {
  state: WorkflowState;
  actions: {
    setWorkflowName: (name: string) => void;
    addNode: (nodeId: string, nodeType: NodeType, taskName?: string, varName?: string) => void;
    removeNode: (nodeId: string) => void;
    resetWorkflow: () => void;
    updateNodeData: (nodeId: string, taskName?: string, varName?: string) => void;
    setOutgoingEdges: (edges: ReactFlowEdge[]) => void;
  };
}

export type DeployWorkflowResult = {
  success: true;
  wfSpecId: WfSpecId | undefined
};
