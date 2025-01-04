'use server'

import { auth } from '@/auth'
import { getClient } from '@/lhConfig'
import { TaskDefId } from 'littlehorse-client/proto'

export const getTaskDef = async (tenantId: string, request: TaskDefId) => {
  const session = await auth()
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  return client.getTaskDef(request)
}
