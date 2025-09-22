'use client';

import { createContext, useContext, useReducer, useMemo } from 'react';
import { createInitialUIState } from './helpers';
import { UIReducer } from './reducer';
import { createUIActions } from './actions';
import type { UIContextValue } from '../../types';

const UIContext = createContext<UIContextValue | undefined>(undefined);

export function useUI() {
  const context = useContext(UIContext);
  if (!context) {
    throw new Error('useUI must be used within a UIProvider');
  }
  return context;
}

export function UIProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(UIReducer, createInitialUIState());

  const actions = useMemo(() => createUIActions(dispatch), [dispatch]);
  const contextValue = useMemo(() => ({ state, actions }), [state, actions]);

  return <UIContext.Provider value={contextValue}>{children}</UIContext.Provider>;
}
