import lhConfig from '@/lhConfig'
import { ACLAction, ACLResource, Principal, ServerACLs } from 'littlehorse-client/proto'
import { getServerSession } from 'next-auth'
import { redirect } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WhoAmI } from '../types'
import { authOptions } from './api/auth/[...nextauth]/authOptions'

const getWhoAmI = async (): Promise<WhoAmI> => {
  const session = await getServerSession(authOptions)
  const client = lhConfig.getClient(session?.accessToken)

  let principal: Principal
  try {
    principal = await client.whoami({})
  } catch (error: unknown) {
    if (error instanceof ClientError && error.code === Status.UNAUTHENTICATED) {
      redirect('/api/auth/signin')
    } else {
      throw error
    }
  }

  const { id, perTenantAcls, globalAcls } = principal

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
