'use server'

import { auth } from '@/auth'
import { getClient } from '../lhConfig'
import { WithTenant } from '../types'

export const lhClient = async ({ tenantId }: WithTenant) => {
  const session = await auth()
  return getClient({ tenantId, accessToken: session?.accessToken })
}
