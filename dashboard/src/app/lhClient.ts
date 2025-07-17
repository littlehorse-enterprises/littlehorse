'use server'

import { getServerSession } from 'next-auth'
import { WithTenant } from '../types'
import { getClient } from '../lhConfig'
import { authOptions } from './api/auth/[...nextauth]/authOptions'

export const lhClient = async ({ tenantId }: WithTenant) => {
  const session = await getServerSession(authOptions)
  return getClient({ tenantId, accessToken: session?.accessToken })
}
