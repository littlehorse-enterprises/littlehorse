import type { ExponentialBackoffRetryPolicy } from '../proto/common_wfspec'
import type { LHFormatString } from './lhFormatString'
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

  execute(taskName: string | WfRunVariable | LHFormatString, ...args: WorkflowRhs[]): TaskNodeOutput

  format(template: string, ...args: WorkflowRhs[]): LHFormatString

  complete(result?: WorkflowRhs): void
}
