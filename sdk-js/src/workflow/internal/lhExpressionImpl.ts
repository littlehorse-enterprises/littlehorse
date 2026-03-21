import type { VariableAssignment } from '../../proto/common_wfspec'
import { VariableMutationType } from '../../proto/common_wfspec'
import type { LHExpression } from '../lhExpression'
import type { WorkflowRhs } from '../workflowRhs'

export class LHExpressionImpl implements LHExpression {
  constructor(
    readonly lhs: WorkflowRhs,
    readonly operation: VariableMutationType,
    readonly rhs: WorkflowRhs
  ) {}

  add(other: WorkflowRhs): LHExpression {
    return new LHExpressionImpl(this, VariableMutationType.ADD, other)
  }

  subtract(other: WorkflowRhs): LHExpression {
    return new LHExpressionImpl(this, VariableMutationType.SUBTRACT, other)
  }

  multiply(other: WorkflowRhs): LHExpression {
    return new LHExpressionImpl(this, VariableMutationType.MULTIPLY, other)
  }

  divide(other: WorkflowRhs): LHExpression {
    return new LHExpressionImpl(this, VariableMutationType.DIVIDE, other)
  }

  toAssignment(assign: (r: WorkflowRhs) => VariableAssignment): VariableAssignment {
    return {
      path: undefined,
      source: {
        $case: 'expression',
        value: {
          lhs: assign(this.lhs),
          operation: { $case: 'mutationType', value: this.operation },
          rhs: assign(this.rhs),
        },
      },
      targetType: undefined,
    }
  }
}
