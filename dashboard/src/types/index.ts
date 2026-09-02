import { TypeDefinition } from 'littlehorse-client/proto'
import { DefaultSession } from 'next-auth'

export type WhoAmI = {
  user: DefaultSession['user']
  tenants: string[]
}

export type WithTenant = {
  tenantId: string
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
  createdAt: Date | undefined
  parentWfSpec?: {
    wfSpecName: string
    wfSpecMajorVersion: number
  }
}

export type TaskDefData = {
  name: string
  createdAt: Date | undefined
  description?: string
  inputVarCount: number
  returnType?: TypeDefinition['definedType']
  /** null when the server did not return worker group info */
  connectedWorkers: number | null
  /** null when queue depth is unavailable on this server */
  queueDepth: number | null
}
