import type { UIState, UIAction } from '../../types';
import { UIActionType } from '../../types';
import { createInitialUIState } from './helpers';

export function UIReducer(state: UIState, action: UIAction): UIState {
  switch (action.type) {
    case UIActionType.SELECT_NODE:
      return {
        ...state,
        selectedNode: action.payload,
        selectedEdge: null,
      };

    case UIActionType.SELECT_EDGE:
      return {
        ...state,
        selectedNode: null,
        selectedEdge: action.payload,
      };

    case UIActionType.SET_LOADING:
      return {
        ...state,
        loading: action.payload,
      };

    case UIActionType.RESET_UI:
      return createInitialUIState();

    default:
      return state;
  }
}
