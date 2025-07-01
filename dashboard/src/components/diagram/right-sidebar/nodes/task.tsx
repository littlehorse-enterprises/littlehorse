'use client'

import { getVariable } from '@/utils/data/variables'
import { Node, TaskNode } from 'littlehorse-client/proto'
import { BaseNodeComponent } from './base-node'

interface TaskNodeComponentProps {
  taskNode: Node & { task: TaskNode }
}

export function TaskNodeComponent({ taskNode }: TaskNodeComponentProps) {
  const mainContent = (
    <>
      <div className="flex justify-between">
        <span className="text-[#656565]">Timeout:</span>
        <span className="font-mono">{taskNode.task.timeoutSeconds || 'N/A'} s</span>
      </div>
      <div className="flex justify-between">
        <span className="text-[#656565]">Retries:</span>
        <span className="font-mono">{taskNode.task.retries || 'N/A'}</span>
      </div>
      {taskNode.task.taskToExecute?.$case === 'taskDefId' && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Task Def:</span>
          <span className="font-mono text-blue-600">{taskNode.task.taskToExecute.taskDefId.name}</span>
        </div>
      )}
    </>
  )

  const additionalSections = taskNode.task.variables && Object.keys(taskNode.task.variables).length > 0 ? [
    {
      title: "Variables",
      content: (
        <div className="space-y-1">
          {Object.entries(taskNode.task.variables).map(([key, variable]) => (
            <div key={key} className="font-mono text-xs">
              <span className="text-purple-600">{key}:</span>{' '}
              <span className="text-blue-600">{getVariable(variable)}</span>
            </div>
          ))}
        </div>
      )
    }
  ] : undefined

  return (
    <BaseNodeComponent
      title="Task Properties"
      type="TASK"
      additionalSections={additionalSections}
    >
      {mainContent}
    </BaseNodeComponent>
  )
}
