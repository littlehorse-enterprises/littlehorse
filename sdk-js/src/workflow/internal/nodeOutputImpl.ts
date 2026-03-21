import type { VariableAssignment } from '../../proto/common_wfspec'
import { VariableMutationType } from '../../proto/common_wfspec'
import { LHMisconfigurationException } from '../exceptions'
import type { NodeOutput } from '../nodeOutput'
import type { WorkflowRhs } from '../workflowRhs'
import { LHExpressionImpl } from './lhExpressionImpl'
import type { WorkflowThreadImpl } from './workflowThreadImpl'

export class NodeOutputImpl implements NodeOutput {
  readonly jsonPathValue?: string
  readonly lhSelectors: { selectorType?: { $case: 'key'; value: string } | { $case: 'index'; value: number } }[]

  constructor(
    readonly nodeName: string,
    readonly thread: WorkflowThreadImpl,
    jsonPath?: string,
    lhSelectors?: NodeOutputImpl['lhSelectors']
  ) {
    this.jsonPathValue = jsonPath
    this.lhSelectors = lhSelectors ?? []
  }

  toAssignment(): VariableAssignment {
    const path =
      this.jsonPathValue !== undefined
        ? { $case: 'jsonPath' as const, value: this.jsonPathValue }
        : this.lhSelectors.length > 0
          ? { $case: 'lhPath' as const, value: { path: [...this.lhSelectors] } }
          : undefined
    return {
      path,
      source: {
        $case: 'nodeOutput',
        value: { nodeName: this.nodeName },
      },
      targetType: undefined,
    }
  }

  jsonPath(path: string): NodeOutput {
    if (this.jsonPathValue !== undefined) {
      throw new LHMisconfigurationException('Cannot use jsonPath() twice on the same node output')
    }
    if (this.lhSelectors.length > 0) {
      throw new LHMisconfigurationException('Cannot use jsonPath() and get() on the same node output')
    }
    return new NodeOutputImpl(this.nodeName, this.thread, path, [])
  }

  get(field: string): NodeOutput {
    if (this.jsonPathValue !== undefined) {
      throw new LHMisconfigurationException('Cannot use jsonPath() and get() on the same node output')
    }
    const next = [...this.lhSelectors, { selectorType: { $case: 'key' as const, value: field } }]
    return new NodeOutputImpl(this.nodeName, this.thread, undefined, next)
  }

  add(other: WorkflowRhs): LHExpressionImpl {
    return new LHExpressionImpl(this, VariableMutationType.ADD, other)
  }

  subtract(other: WorkflowRhs): LHExpressionImpl {
    return new LHExpressionImpl(this, VariableMutationType.SUBTRACT, other)
  }

  multiply(other: WorkflowRhs): LHExpressionImpl {
    return new LHExpressionImpl(this, VariableMutationType.MULTIPLY, other)
  }

  divide(other: WorkflowRhs): LHExpressionImpl {
    return new LHExpressionImpl(this, VariableMutationType.DIVIDE, other)
  }
}
