import { lhClient } from '@/lhClient'
import TaskDefClient from './TaskDefClient'

type PageProps = {
  params: Promise<{
    tenantId: string
    taskDefId: string
  }>
}

export default async function TaskDefPage({ params }: PageProps) {
  const { tenantId, taskDefId } = await params
  const client = await lhClient(tenantId)

  const taskDef = await client.getTaskDef({ name: taskDefId })

  return <TaskDefClient taskDef={taskDef} />
}
