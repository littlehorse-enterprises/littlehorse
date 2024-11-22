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
}

export type VersionList = {
  versions: string[]
  bookmark?: string
}

export type WfSpecData = {
  name: string
  latestVersion: string
}
