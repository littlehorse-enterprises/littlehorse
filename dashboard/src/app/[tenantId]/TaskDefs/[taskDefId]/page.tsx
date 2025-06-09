import { lhClient } from "@/utils/lhClient"
import TaskDefClient from "./TaskDefClient"

type PageProps = {
  params: Promise<{
    tenantId: string
    taskDefId: string
  }>
}

export default async function TaskDefPage({ params }: PageProps) {
  const { tenantId, taskDefId } = await params

  try {
    const client = await lhClient(tenantId)

    const [taskDef, { results: wfSpecIds }] = await Promise.all([
      client.getTaskDef({ name: taskDefId }),
      client.searchWfSpec({ taskDefName: taskDefId, limit: 10 }),
    ])

    return (
      <TaskDefClient
        taskDef={taskDef}
        wfSpecIds={wfSpecIds}
        tenantId={tenantId}
        taskDefId={taskDefId}
      />
    )
  } catch (error) {
    console.error("Error fetching TaskDef:", error)
    // Handle error appropriately
    throw error
  }
}
