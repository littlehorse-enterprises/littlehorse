'use client'

import type { Node as ReactFlowNode } from 'reactflow'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { useNodeDataEditor } from '../../hooks/useNodeDataEditor'

const inputClass = 'bg-gray-800 border-gray-600 text-white mt-2 mb-4 placeholder:text-gray-400 focus-visible:ring-0'

export function NodeDataPanel({ node }: { node: ReactFlowNode }) {
  const {
    isEditing,
    currentTaskName,
    currentVarName,
    editingTaskName,
    editingVarName,
    startEditing,
    handleSave,
    handleCancel,
    handleDelete,
    updateTaskName,
    updateVarName,
  } = useNodeDataEditor(node.id)

  const taskName = isEditing ? editingTaskName : currentTaskName
  const varName = isEditing ? editingVarName : currentVarName

  return (
    <Card className="border-gray-700 bg-gray-900 text-white">
      <CardHeader>
        <CardTitle className="text-sm">Node Data Panel</CardTitle>
      </CardHeader>
      <CardContent>
        <Label htmlFor="node-id">Node ID</Label>
        <Input id="node-id" className={inputClass} value={node.id} disabled />

        <Label htmlFor="task-name">Task Name</Label>
        <Input
          id="task-name"
          className={inputClass}
          value={taskName}
          disabled={!isEditing}
          onChange={e => updateTaskName(e.target.value)}
        />

        <Label htmlFor="var-name">Variable Name</Label>
        <Input
          id="var-name"
          className={inputClass}
          value={varName}
          disabled={!isEditing}
          onChange={e => updateVarName(e.target.value)}
        />
        {isEditing ? (
          <>
            <Button onClick={handleSave} className="mr-2">
              Save
            </Button>
            <Button onClick={handleCancel} className="mt-2">
              Cancel
            </Button>
          </>
        ) : (
          <>
            <Button onClick={startEditing} className="mr-2">
              Edit
            </Button>
            <Button onClick={() => handleDelete(node.id)} className="mt-2">
              Delete
            </Button>
          </>
        )}
      </CardContent>
    </Card>
  )
}
