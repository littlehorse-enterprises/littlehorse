import { LHStatus, LittleHorseDefinition } from 'littlehorse-client/proto'
import { LucideIcon } from 'lucide-react'
import { Edge as RFEdge, Node as RFNode } from '@xyflow/react'
import { Edge as LHEdge, Node as LHNode, NodeRun, ThreadVarDef } from 'littlehorse-client/proto'
import { FieldValues, FormState, UseFormRegister } from 'react-hook-form'
import { SEARCH_ENTITIES } from '@/constants'

// PageParams types
export type PageParams = {
  params: Promise<unknown>
  searchParams: Promise<unknown>
}

export type PathnameKeys = keyof Awaited<PageParams['params']>

// ExecuteRPC types
export type LittleHorseMethodRPCName = keyof typeof LittleHorseDefinition.methods
export type LittleHorseMethod = (typeof LittleHorseDefinition.methods)[LittleHorseMethodRPCName]
export type LittleHorseMethodName = LittleHorseMethod['name']
export type LittleHorseMethodInput = LittleHorseMethod['requestType']
export type LittleHorseMethodOutput = LittleHorseMethod['responseType']

export type RpcMethod = {
  name: LittleHorseMethodName
  inputType: LittleHorseMethodInput
  outputType: LittleHorseMethodOutput
  rpcName: LittleHorseMethodRPCName
  icon: LucideIcon
}

export type LHMethodParamType<M extends LittleHorseMethodRPCName> = ReturnType<
  (typeof LittleHorseDefinition.methods)[M]['requestType']['create']
>
export type LHMethodReturnType<M extends LittleHorseMethodRPCName> = ReturnType<
  (typeof LittleHorseDefinition.methods)[M]['responseType']['create']
>

// Forms types
export type FormFieldProp = {
  variables?: ThreadVarDef
  custom?: boolean
  formState: FormState<FieldValues>
  register: UseFormRegister<FieldValues>
}

// LeftSidebarTabs types
export type LeftSidebarTabId = 'WfSpec' | 'WfRuns' | 'ScheduledWfRuns'

// Node types
export type NodeData = {
  node: LHNode
  nodeRun?: NodeRun
  type: OneOfCases<LHNode['node']>
  label: string
}
export type CustomNode = RFNode<NodeData>

export type EdgeData = { edge: LHEdge }
export type CustomEdge = RFEdge<EdgeData>

// OneOf types
/** Extracts all the case names from a oneOf field. */
export type OneOfCases<T> = T extends { $case: infer U extends string } ? U : never

/** Extracts a union of all the value types from a oneOf field */
export type OneOfValues<T> = T extends { $case: infer U extends string; [key: string]: unknown } ? T[U] : never

/** Extracts the specific type of a oneOf case based on its field name */
export type OneOfCase<T, K extends OneOfCases<T>> = T extends {
  $case: K
  [key: string]: unknown
}
  ? T
  : never

/** Extracts the specific type of a value type from a oneOf field */
export type OneOfValue<T, K extends OneOfCases<T>> = T extends {
  $case: infer U extends K
  [key: string]: unknown
}
  ? T[U]
  : never

// Search types
export type SearchType = (typeof SEARCH_ENTITIES)[number]

// Withs types
export type WithTenant = {
  tenantId: string
}
export type WithBookmark = {
  bookmark?: string
}

export type NodeType = OneOfCases<LHNode['node']>

export interface TreeNode {
  id: string
  label: string
  type?: NodeType
  status?: LHStatus
  children: TreeNode[]
  level: number
}
