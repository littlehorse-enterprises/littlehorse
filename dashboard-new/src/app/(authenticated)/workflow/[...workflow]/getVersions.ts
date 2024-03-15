'use server'
import { authOptions } from '@/app/api/auth/[...nextauth]/authOptions'
import { getClient } from '@/lhConfig'
import { getServerSession } from 'next-auth'

type GetWfSpecProps = {
  name: string
  tenantId?: string
}

export const getWfSpecVersions = async ({ name, tenantId }: GetWfSpecProps): Promise<string[]> => {
  const session = await getServerSession(authOptions)
  const client = getClient({ tenantId, accessToken: session?.accessToken })

  const specs = await client.searchWfSpec({ name })

  return specs.results.map(({ majorVersion, revision }) => {
    return `${majorVersion}.${revision}`
  })
}
