import { useState, useCallback } from 'react';
import { useWorkflow } from '../contexts/workflow/provider';
import { useReactFlow } from 'reactflow';
import { useUI } from '../contexts/ui/provider';

interface UseNodeDataEditorResult {
  isEditing: boolean
  currentTaskName: string
  currentVarName: string
  editingTaskName: string
  editingVarName: string
  startEditing: () => void
  handleSave: () => void
  handleCancel: () => void
  handleDelete: (nodeId: string) => void
  updateTaskName: (value: string) => void
  updateVarName: (value: string) => void
}

export function useNodeDataEditor(nodeId: string): UseNodeDataEditorResult {
  const [isEditing, setIsEditing] = useState(false);
  const { state: wfState, actions: wfActions } = useWorkflow();
  const { actions: uiActions } = useUI();
  const { setNodes } = useReactFlow();

  const currentNodeData = wfState.spec.threadSpecs.entrypoint.nodes[nodeId]
  const currentTaskName =
    currentNodeData?.node?.$case === 'task' && currentNodeData.node.value.taskToExecute?.$case === 'taskDefId'
      ? currentNodeData.node.value.taskToExecute.value.name
      : '';

  const currentVarName =
    currentNodeData?.node?.$case === 'task' &&
    currentNodeData.node.value.variables?.[0]?.source?.$case === 'variableName'
      ? currentNodeData.node.value.variables[0].source.value
      : '';

  const [editingValues, setEditingValues] = useState({
    taskName: '',
    varName: '',
  });

  const startEditing = useCallback(() => {
    setEditingValues({
      taskName: currentTaskName,
      varName: currentVarName,
    })
    setIsEditing(true);
  }, [currentTaskName, currentVarName])

  const handleSave = useCallback(() => {
    wfActions.updateNodeData(nodeId, editingValues.taskName, editingValues.varName);
    setIsEditing(false);
  }, [wfActions, nodeId, editingValues])

  const handleCancel = useCallback(() => setIsEditing(false), []);

  const updateTaskName = useCallback((value: string) => {
    setEditingValues(prev => ({ ...prev, taskName: value }));
  }, [])

  const updateVarName = useCallback((value: string) => {
    setEditingValues(prev => ({ ...prev, varName: value }));
  }, [])

  const handleDelete = useCallback(
    (nodeId: string) => {
      setNodes(nodes => nodes.filter(node => node.id !== nodeId));
      wfActions.removeNode(nodeId);
      uiActions.selectNode(null);
    },
    [setNodes, wfActions, uiActions]
  );

  return {
    isEditing,
    currentTaskName,
    currentVarName,
    editingTaskName: editingValues.taskName,
    editingVarName: editingValues.varName,
    startEditing,
    handleSave,
    handleCancel,
    handleDelete,
    updateTaskName,
    updateVarName,
  }
}
