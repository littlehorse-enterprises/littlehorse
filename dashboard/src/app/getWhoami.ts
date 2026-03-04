import lhConfig from '@/lhConfig'
import { ACLAction, ACLResource, Principal } from 'littlehorse-client/proto'
import { getServerSession } from 'next-auth'
import { WhoAmI } from '../types'
import { authOptions } from './api/auth/[...nextauth]/authOptions'

const getWhoAmI = async (): Promise<WhoAmI> => {
  const session = await getServerSession(authOptions)
  const client = lhConfig.getClient(session?.accessToken)

  const { id, perTenantAcls, globalAcls } = await client.whoami({})
  const tenants = hasGlobalAccess({ globalAcls }) ? await searchTenants(client) : Object.keys(perTenantAcls)

  return {
    user: session?.user || { name: id?.id },
    tenants,
  }
}

const hasGlobalAccess = ({ globalAcls }: Pick<Principal, 'globalAcls'>): boolean => {
  if (!globalAcls) return false
  return globalAcls.acls.some(
    ({ resources, allowedActions }) =>
      resources.includes(ACLResource.ACL_TENANT) || allowedActions.includes(ACLAction.ALL_ACTIONS)
  )
}

const searchTenants = async (client: ReturnType<typeof lhConfig.getClient>): Promise<string[]> => {
  try {
    const { results } = await client.searchTenant({})
    return results.map(t => t.id).filter((id): id is string => id !== undefined)
  } catch {
    return []
  }
}

export default getWhoAmI
