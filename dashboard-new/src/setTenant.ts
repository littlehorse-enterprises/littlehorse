'use server'
import { cookies } from 'next/headers'

export const setTenant = async (tenantId: string) => {
  cookies().set('tenantId', tenantId)
}
