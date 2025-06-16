'use client'

import { Node, TaskNode } from 'littlehorse-client/proto'

interface TaskNodeComponentProps {
  taskNode: Node & { task: TaskNode }
}

export function TaskNodeComponent({ taskNode }: TaskNodeComponentProps) {
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
            <span className="font-mono">{taskNode.task.timeoutSeconds || 'N/A'} s</span>
          </div>
          <div className="flex justify-between">
            <span className="text-[#656565]">Retries:</span>
            <span className="font-mono">{taskNode.task.retries || 'N/A'}</span>
          </div>
          {taskNode.task.taskDefId && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Task Def:</span>
              <span className="font-mono text-blue-600">{taskNode.task.taskDefId.name}</span>
            </div>
          )}
        </div>
      </div>

      {taskNode.task.variables && Object.keys(taskNode.task.variables).length > 0 && (
        <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
          <h4 className="mb-2 text-xs font-medium">Variables</h4>
          <div className="space-y-1">
            {Object.entries(taskNode.task.variables).map(([key, variable]) => (
              <div key={key} className="font-mono text-xs">
                <span className="text-purple-600">{key}:</span>{' '}
                <span className="text-blue-600">{JSON.stringify(variable)}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
