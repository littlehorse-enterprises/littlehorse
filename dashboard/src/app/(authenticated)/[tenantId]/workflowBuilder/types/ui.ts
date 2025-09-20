import type { Node as ReactFlowNode, Edge as ReactFlowEdge } from 'reactflow';

export enum UIActionType {
  SELECT_NODE = 'SELECT_NODE',
  SELECT_EDGE = 'SELECT_EDGE',
  SET_LOADING = 'SET_LOADING',
}

export interface UIState {
  selectedNode: ReactFlowNode | null;
  selectedEdge: ReactFlowEdge | null;
  loading: boolean;
}

export type UIAction =
  | { type: UIActionType.SELECT_NODE; payload: ReactFlowNode | null }
  | { type: UIActionType.SELECT_EDGE; payload: ReactFlowEdge | null }
  | { type: UIActionType.SET_LOADING; payload: boolean };

export interface UIContextValue {
  state: UIState;
  actions: {
    selectNode: (node: ReactFlowNode | null) => void;
    selectEdge: (edge: ReactFlowEdge | null) => void;
    setLoading: (loading: boolean) => void;
  };
}
