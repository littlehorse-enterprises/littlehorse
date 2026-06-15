import type { VariableType } from '../proto/common_enums'
import type { InlineLHStructBuilder } from './inlineLHStructBuilder'
import type { LHFormatString } from './lhFormatString'
import type { LHStructBuilder } from './lhStructBuilder'
import type { TaskNodeOutput } from './taskNodeOutput'
import type { WfRunVariable } from './wfRunVariable'
import type { WorkflowRhs } from './workflowRhs'

export interface WorkflowThread {
  declareStr(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareInt(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareDouble(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareBool(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareJsonObj(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareStruct(name: string, structDefName: string): WfRunVariable
  declareArray(name: string, elementType: VariableType): WfRunVariable

  execute(taskName: string | WfRunVariable | LHFormatString, ...args: WorkflowRhs[]): TaskNodeOutput

  format(template: string, ...args: WorkflowRhs[]): LHFormatString

  buildStruct(structDefName: string): LHStructBuilder
  buildInlineStruct(): InlineLHStructBuilder

  complete(result?: WorkflowRhs): void
}
