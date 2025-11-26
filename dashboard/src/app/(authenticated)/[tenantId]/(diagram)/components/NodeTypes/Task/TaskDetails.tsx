'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { getVariable } from '@/app/utils'
import { useQuery } from '@tanstack/react-query'
import { TaskNode } from 'littlehorse-client/proto'
import { ExternalLinkIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, useState } from 'react'
import { NodeProps } from '..'
import { getTaskRun } from './getTaskRun'

export const TaskDetails: FC<NodeProps<'task', TaskNode>> = ({ data }) => {
  const { nodeRunsList } = data
  const [nodeRunsIndex, setNodeRunsIndex] = useState(0)
  const tenantId = useParams().tenantId as string

  const { data: taskRunData } = useQuery({
    queryKey: ['taskRun', tenantId, nodeRunsIndex],
    queryFn: async () => {
      if (!nodeRunsList[nodeRunsIndex].nodeType.value.taskRunId) return null
      return await getTaskRun({ tenantId, ...nodeRunsList[nodeRunsIndex].nodeType.value.taskRunId })
    },
  })

  taskRunData?.attempts.sort((a, b) => new Date(b.startTime ?? 0).getTime() - new Date(a.startTime ?? 0).getTime())

  return null
}

export const TaskLink: FC<{ taskToExecute: NonNullable<TaskNode['taskToExecute']> }> = ({ taskToExecute }) => {
  const taskName = getTaskName(taskToExecute)
  return (
    <LinkWithTenant
      className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
      target="_blank"
      href={`/taskDef/${taskName}`}
    >
      {`${taskName}`} <ExternalLinkIcon className="h-4 w-4" />
    </LinkWithTenant>
  )
}

export const getTaskName = (task: TaskNode['taskToExecute']): string => {
  if (!task) return ''

  if (task.$case === 'taskDefId') return task.value.name
  if (task.$case === 'dynamicTask') return getVariable(task.value)
  return ''
}
