'use server'
import { authOptions } from '@/app/api/auth/[...nextauth]/authOptions'
import { getClient } from '@/lhConfig'
import { WithTenant } from '@/types'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { getServerSession } from 'next-auth'

type GetWfSpecProps = {
  name: string
  version: string
} & WithTenant

export const getWfSpec = async ({ name, version, tenantId }: GetWfSpecProps): Promise<WfSpec> => {
  const session = await getServerSession(authOptions)
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  if (/[0-9]+\.[0-9]+/.test(version)) {
    const [majorVersion, revision] = version.split('.')
    return client.getWfSpec({ name, majorVersion: parseInt(majorVersion), revision: parseInt(revision) })
  }

  return client.getLatestWfSpec({ name })
}
