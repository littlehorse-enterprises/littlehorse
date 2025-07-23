import { ThreadVarDef } from 'littlehorse-client/proto'
import { DefaultSession } from 'next-auth'
import { FieldValues, UseFormRegister } from 'react-hook-form'

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

export type FormFieldProp = {
  variables?: ThreadVarDef
  custom?: boolean
  formState: any
  register: UseFormRegister<FieldValues>
}
