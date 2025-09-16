import type { UIAction } from '../../types';
import { UIActionType } from '../../types';
import type { Node as ReactFlowNode, Edge as ReactFlowEdge } from 'reactflow';

export const createUIActions = (
  dispatch: React.Dispatch<UIAction>
) => ({
  selectNode: (node: ReactFlowNode | null) => {
    dispatch({
      type: UIActionType.SELECT_NODE,
      payload: node 
    });
  },
  selectEdge: (edge: ReactFlowEdge | null) => {
    dispatch({
      type: UIActionType.SELECT_EDGE,
      payload: edge
    });
  },
  setLoading: (loading: boolean) => {
    dispatch({
      type: UIActionType.SET_LOADING,
      payload: loading
    });
  }
});
