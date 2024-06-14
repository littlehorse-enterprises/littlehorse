'use server'

import { authOptions } from '@/app/api/auth/[...nextauth]/authOptions'
import { getClient } from '@/lhConfig'
import { TaskDefId } from 'littlehorse-client'
import { getServerSession } from 'next-auth'
import { cookies } from 'next/headers'

export const getTaskDef = async (request: TaskDefId) => {
  const session = await getServerSession(authOptions)
  const tenantId = cookies().get('tenantId')?.value
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  return client.getTaskDef(request)
}
