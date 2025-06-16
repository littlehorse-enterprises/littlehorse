'use client'

import VariableDisplay from '@/components/ui/variable-display'
import { Node, TaskNode } from 'littlehorse-client/proto'

interface TaskNodeDefinitionProps {
  nodeDef: Node | undefined
}

export function TaskNodeDefinition({ nodeDef }: TaskNodeDefinitionProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Task Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">TASK</span>
          </div>
          <div className="flex justify-between">
            <span className="text-[#656565]">Timeout:</span>
            <span className="font-mono">{nodeDef?.task?.timeoutSeconds || 'N/A'} s</span>
          </div>
          <div className="flex justify-between">
            <span className="text-[#656565]">Retries:</span>
            <span className="font-mono">{nodeDef?.task?.retries || 'N/A'}</span>
          </div>
        </div>
      </div>

      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Input Variables</h4>
        <div className="space-y-1">
          {nodeDef?.task?.taskDefId && (
            <div className="font-mono text-xs">
              <span className="text-purple-600">TaskDef:</span>{' '}
              <span className="text-blue-600">{nodeDef.task.taskDefId.name}</span>
            </div>
          )}
          {/* Display task variables if available */}
          {nodeDef?.task?.variables &&
            Object.entries(nodeDef.task.variables).map(([key, variable]) => (
              <VariableDisplay
                key={key}
                name={key}
                type={variable.variableName || 'UNKNOWN'}
                value={variable.jsonPath || variable.literalValue || 'N/A'}
              />
            ))}
        </div>
      </div>

      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Output Type</h4>
        <div className="font-mono text-xs">
          <span className="text-purple-600">Object</span> <span className="text-blue-600">result</span>
        </div>
      </div>
    </div>
  )
} 

interface TaskNodeComponentProps {
  taskNode: Node & { task: TaskNode }
}

export function TaskNodeComponent({ }: TaskNodeComponentProps) {
  return (
    <div>
      <h4 className="mb-2 text-xs font-medium">Task Run</h4>
    </div>
  )
}
