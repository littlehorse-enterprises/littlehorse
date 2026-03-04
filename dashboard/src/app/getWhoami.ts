import lhConfig from '@/lhConfig'
import { ACLAction, ACLResource, Principal, ServerACLs } from 'littlehorse-client/proto'
import { getServerSession } from 'next-auth'
import { WhoAmI } from '../types'
import { authOptions } from './api/auth/[...nextauth]/authOptions'

const getWhoAmI = async (): Promise<WhoAmI> => {
  const session = await getServerSession(authOptions)
  const client = lhConfig.getClient(session?.accessToken)

  const { id, perTenantAcls, globalAcls } = await client.whoami({})
  const tenants = await getTenants(client, { perTenantAcls, globalAcls })

  return {
    user: session?.user || { name: id?.id },
    tenants,
  }
}

const getTenants = async (
  client: ReturnType<typeof lhConfig.getClient>,
  { perTenantAcls, globalAcls }: Pick<Principal, 'globalAcls' | 'perTenantAcls'>
): Promise<string[]> => {
  if (globalAcls && hasGlobalAccess(globalAcls)) {
    const { results } = await client.searchTenant({})
    return results.map(t => t.id).filter((id): id is string => id !== undefined)
  }
  return Object.keys(perTenantAcls)
}

const hasGlobalAccess = ({ acls }: ServerACLs): boolean => {
  return acls.some(
    ({ resources, allowedActions }) =>
      resources.includes(ACLResource.ACL_TENANT) && allowedActions.includes(ACLAction.ALL_ACTIONS)
  )
}

export default getWhoAmI
