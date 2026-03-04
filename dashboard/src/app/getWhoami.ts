import lhConfig from '@/lhConfig'
import { ACLAction, ACLResource, Principal, ServerACLs } from 'littlehorse-client/proto'
import { getServerSession } from 'next-auth'
import { WhoAmI } from '../types'
import { authOptions } from './api/auth/[...nextauth]/authOptions'

const getWhoAmI = async (): Promise<WhoAmI> => {
  const session = await getServerSession(authOptions)
  const client = lhConfig.getClient(session?.accessToken)

  const { id, perTenantAcls, globalAcls } = await client.whoami({})
  const aclTenants = getTenants({ perTenantAcls, globalAcls })
  const searchTenants = await getSearchTenants(client)
  const tenants = Array.from(new Set([...aclTenants, ...searchTenants]))

  return {
    user: session?.user || { name: id?.id },
    tenants,
  }
}

const getTenants = ({ perTenantAcls, globalAcls }: Pick<Principal, 'globalAcls' | 'perTenantAcls'>): string[] => {
  let tenants: string[] = []
  if (globalAcls && hasDefaultAccess(globalAcls)) {
    tenants = ['default']
  }
  return [...tenants, ...Object.keys(perTenantAcls)]
}

const hasDefaultAccess = ({ acls }: ServerACLs): boolean => {
  return acls.some(
    ({ resources, allowedActions }) =>
      resources.includes(ACLResource.ACL_ALL_RESOURCES) && allowedActions.includes(ACLAction.ALL_ACTIONS)
  )
}

const getSearchTenants = async (client: ReturnType<typeof lhConfig.getClient>): Promise<string[]> => {
  try {
    const { results } = await client.searchTenant({})
    return results.map(t => t.id).filter((id): id is string => id !== undefined)
  } catch {
    return []
  }
}

export default getWhoAmI
