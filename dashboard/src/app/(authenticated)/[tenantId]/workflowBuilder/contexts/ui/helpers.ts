import type { UIState } from '../../types';

export const createInitialUIState = (): UIState => ({
  selectedNode: null,
  selectedEdge: null,
  loading: false,
});
