import lhConfig from '@/lhConfig'
import { authOptions } from '../api/auth/[...nextauth]/authOptions'
import { DefaultSession, getServerSession } from 'next-auth'
import { ACLAction, ACLResource, Principal, ServerACLs } from 'littlehorse-client/dist/proto/acls'

type WhoAmI = {
  user: DefaultSession['user']
  tenants: string[]
}

const getWhoAmI = async (): Promise<WhoAmI> => {
  const session = await getServerSession(authOptions)
  if (session) {
    const { accessToken } = session
    const client = lhConfig.getClient(accessToken)

    const { perTenantAcls, globalAcls } = await client.whoami({})

    const tenants = getTenants({ perTenantAcls, globalAcls })

    return {
      user: session.user,
      tenants,
    }
  } else {
    return {
      user: {
        name: 'anonymous',
        email: 'anonymous',
      },
      tenants: ['default'],
    }
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
