import type { VariableAssignment } from '../../proto/common_wfspec'
import { toVariableValue } from '../../utils/variableValueConvert'
import type { LHFormatString } from '../lhFormatString'
import type { WorkflowRhs } from '../workflowRhs'

export class LHFormatStringImpl implements LHFormatString {
  readonly _lhFormatString = true as const

  constructor(
    readonly template: string,
    readonly argAssignments: VariableAssignment[]
  ) {}

  toAssignment(assign: (r: WorkflowRhs) => VariableAssignment): VariableAssignment {
    return {
      path: undefined,
      source: {
        $case: 'formatString',
        value: {
          format: {
            path: undefined,
            source: { $case: 'literalValue', value: toVariableValue(this.template) },
            targetType: undefined,
          },
          args: this.argAssignments,
        },
      },
      targetType: undefined,
    }
  }
}
