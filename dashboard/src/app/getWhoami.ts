import lhConfig from '@/lhConfig'
import { getServerSession } from 'next-auth'
import { WhoAmI } from '../types'
import { authOptions } from './api/auth/[...nextauth]/authOptions'

const getWhoAmI = async (): Promise<WhoAmI> => {
  const session = await getServerSession(authOptions)
  const client = lhConfig.getClient(session?.accessToken)

  const { id } = await client.whoami({})
  const { results } = await client.searchTenant({})
  const tenants = results.map(t => t.id).filter((id): id is string => id !== undefined)

  return {
    user: session?.user || { name: id?.id },
    tenants,
  }
}

export default getWhoAmI
