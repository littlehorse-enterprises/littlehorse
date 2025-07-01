import { lhClient } from '@/utils/client/lhClient'
import UserTaskDefClient from './UserTaskDefClient'

interface UserTaskDefPageProps {
  params: Promise<{
    tenantId: string
    userTaskDefId: string
  }>
}

export default async function UserTaskDefPage({ params }: UserTaskDefPageProps) {
  const { tenantId, userTaskDefId } = await params
  const client = await lhClient(tenantId)
  // Fetch the latest version for now; can be extended to support version selection
  const userTaskDef = await client.getUserTaskDef({ name: userTaskDefId })
  return <UserTaskDefClient userTaskDef={userTaskDef} />
}
