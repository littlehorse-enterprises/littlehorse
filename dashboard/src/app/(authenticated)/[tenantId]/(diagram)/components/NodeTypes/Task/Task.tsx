import { getVariable } from '@/app/utils'
import { TaskNode } from 'littlehorse-client/proto'
import { SettingsIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { DiagramNodeCard, DiagramNodeShell } from '../DiagramNodeChrome'
import { orangeNodeTheme } from '../nodeThemes'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'task', TaskNode>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList, taskToExecute } = data
  if (!taskToExecute) return null
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell id={id} label="Task" icon={SettingsIcon} theme={orangeNodeTheme}>
          <div className="relative">
            <DiagramNodeCard selected={selected} theme={orangeNodeTheme}>
              {getTaskName(taskToExecute)}
            </DiagramNodeCard>
            <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
            <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const Task = memo(Node)

export const getTaskName = (task: TaskNode['taskToExecute']): string => {
  if (!task) return ''

  if (task.$case === 'taskDefId') return task.value.name
  if (task.$case === 'dynamicTask') return getVariable(task.value)
  return ''
}
