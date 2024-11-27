'use server'

import { authOptions } from '@/app/api/auth/[...nextauth]/authOptions'
import { getClient } from '@/lhConfig'
import { TaskDefId } from 'littlehorse-client/proto'
import { getServerSession } from 'next-auth'

export const getTaskDef = async (tenantId: string, request: TaskDefId) => {
  const session = await getServerSession(authOptions)
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  return client.getTaskDef(request)
}
