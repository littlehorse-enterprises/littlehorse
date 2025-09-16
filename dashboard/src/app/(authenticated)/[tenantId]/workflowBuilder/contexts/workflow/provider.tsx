'use client';

import { createContext, useContext, useReducer, useMemo } from 'react';
import { createInitialState } from './helpers';
import { workflowReducer } from './reducer';
import { createWorkflowActions } from './actions';
import type { WorkflowContextValue } from '../../types';

const WorkflowContext = createContext<WorkflowContextValue | undefined>(undefined);

export function useWorkflow() {
  const context = useContext(WorkflowContext);
  if (!context) {
    throw new Error('useWorkflow must be used within a WorkflowProvider');
  }
  return context;
}

export function WorkflowProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(workflowReducer, createInitialState());

  const actions = useMemo(() => createWorkflowActions(dispatch), [dispatch]);
  const contextValue = useMemo(() => ({ state, actions }), [state, actions]);

  return (
    <WorkflowContext.Provider value={contextValue}>
      {children}
    </WorkflowContext.Provider>
  );
}
