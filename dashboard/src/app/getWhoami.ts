import lhConfig from '@/lhConfig'
import { ACLAction, ACLResource, Principal, ServerACLs } from 'littlehorse-client/proto'
import { getServerSession } from 'next-auth'
import { WhoAmI } from '../types'
import { authOptions } from './api/auth/[...nextauth]/authOptions'

const getWhoAmI = async (): Promise<WhoAmI> => {
  const session = await getServerSession(authOptions)
  const client = lhConfig.getClient(session?.accessToken)

  const { id, perTenantAcls, globalAcls } = await client.whoami({})

  const tenants = getTenants({ perTenantAcls, globalAcls })

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
  const result = acls.filter(({ resources, allowedActions }) => {
    return resources.includes(ACLResource.ACL_ALL_RESOURCES) && allowedActions.includes(ACLAction.ALL_ACTIONS)
  })
  return result.length > 0
}

export default getWhoAmI
