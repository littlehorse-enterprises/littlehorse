import { lhClient } from '@/utils/client/lhClient'
import WorkflowEventDefClient from './WorkflowEventDefClient'

interface WorkflowEventDefPageProps {
  params: Promise<{
    tenantId: string
    workflowEventDefId: string
  }>
}

export default async function WorkflowEventDefPage({ params }: WorkflowEventDefPageProps) {
  const { tenantId, workflowEventDefId } = await params
  const client = await lhClient(tenantId)
  const workflowEventDef = await client.getWorkflowEventDef({
    name: workflowEventDefId,
  })
  return <WorkflowEventDefClient workflowEventDef={workflowEventDef} />
}
