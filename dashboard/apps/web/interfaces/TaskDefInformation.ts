import type { InputVarsTaskDef } from './InputVarsTaskDef'

export interface TaskDefInformation {
    createdAt: string;
    inputVars: InputVarsTaskDef[];
    name: string;
}