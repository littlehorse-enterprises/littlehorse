import { DefaultSession } from 'next-auth'

export type WhoAmI = {
  user: DefaultSession['user']
  tenants: string[]
}
