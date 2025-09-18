import type { WorkflowAction } from '../../types'
import { WorkflowActionType } from '../../types'
import type { Edge as ReactFlowEdge } from 'reactflow'
import { NodeType } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/extractNodes'

export const createWorkflowActions = (dispatch: React.Dispatch<WorkflowAction>) => ({
  setWorkflowName: (name: string) => {
    dispatch({
      type: WorkflowActionType.SET_WORKFLOW_NAME,
      payload: name,
    })
  },

  addNode: (nodeId: string, nodeType: NodeType, taskName?: string, varName?: string) => {
    dispatch({
      type: WorkflowActionType.ADD_NODE,
      payload: { nodeId, nodeType, taskName, varName },
    })
  },

  removeNode: (nodeId: string) => {
    dispatch({
      type: WorkflowActionType.REMOVE_NODE,
      payload: nodeId,
    })
  },

  resetWorkflow: () => {
    dispatch({ type: WorkflowActionType.RESET_WORKFLOW })
  },

  updateNodeData: (nodeId: string, taskName?: string, varName?: string) => {
    dispatch({
      type: WorkflowActionType.UPDATE_NODE_DATA,
      payload: { nodeId, taskName, varName },
    })
  },

  setOutgoingEdges: (edges: ReactFlowEdge[]) => {
    dispatch({
      type: WorkflowActionType.SET_OUTGOING_EDGES,
      payload: { edges },
    })
  },
})
