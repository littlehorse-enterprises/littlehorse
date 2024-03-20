import { DefaultSession } from 'next-auth'

export type WhoAmI = {
  user: DefaultSession['user']
  tenants: string[]
}

export type WithTenant = {
  tenantId?: string
}

export type WithBookmark = {
  bookmark?: string
} & WithTenant

export type VersionList = {
  versions: string[]
  bookmark?: string
}
