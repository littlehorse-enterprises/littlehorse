import type { VariableType } from '../../proto/common_enums'
import { VariableType as VT } from '../../proto/common_enums'
import type { LHPath_Selector, TypeDefinition, VariableAssignment, VariableDef } from '../../proto/common_wfspec'
import { VariableMutationType } from '../../proto/common_wfspec'
import type { StructDefId } from '../../proto/object_id'
import type { VariableValue } from '../../proto/variable'
import type { ThreadVarDef } from '../../proto/wf_spec'
import { WfRunVariableAccessLevel } from '../../proto/wf_spec'
import { LHMisconfigurationException } from '../exceptions'
import type { WfRunVariable } from '../wfRunVariable'
import type { WorkflowRhs } from '../workflowRhs'
import { LHExpressionImpl } from './lhExpressionImpl'
import type { WorkflowThreadImpl } from './workflowThreadImpl'

export class WfRunVariableImpl implements WfRunVariable {
  readonly jsonPathValue?: string
  readonly lhSelectors: LHPath_Selector[]

  constructor(
    readonly name: string,
    readonly typeDef: TypeDefinition,
    readonly defaultValue: VariableValue | undefined,
    private readonly thread: WorkflowThreadImpl,
    jsonPath?: string,
    lhSelectors?: LHPath_Selector[]
  ) {
    this.jsonPathValue = jsonPath
    this.lhSelectors = lhSelectors ?? []
  }

  toThreadVarDef(required = false): ThreadVarDef {
    const varDef: VariableDef = {
      name: this.name,
      type: undefined,
      defaultValue: this.defaultValue,
      maskedValue: undefined,
      typeDef: this.typeDef,
    }
    return {
      varDef,
      required,
      searchable: false,
      jsonIndexes: [],
      accessLevel: WfRunVariableAccessLevel.PRIVATE_VAR,
    }
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
      source: { $case: 'variableName', value: this.name },
      targetType: undefined,
    }
  }

  jsonPath(path: string): WfRunVariable {
    if (this.jsonPathValue !== undefined) {
      throw new LHMisconfigurationException('Cannot use jsonPath() twice on the same variable')
    }
    if (this.lhSelectors.length > 0) {
      throw new LHMisconfigurationException('Cannot use jsonPath() and get() on the same variable')
    }
    const prim = this.typeDef.definedType?.$case
    if (prim !== 'primitiveType') {
      throw new LHMisconfigurationException('jsonPath is only valid on JSON variables')
    }
    const p = this.typeDef.definedType!.value
    if (p !== VT.JSON_OBJ && p !== VT.JSON_ARR) {
      throw new LHMisconfigurationException(`jsonPath is not valid for variable type ${p}`)
    }
    return new WfRunVariableImpl(this.name, this.typeDef, this.defaultValue, this.thread, path, [])
  }

  get(field: string): WfRunVariable {
    if (this.jsonPathValue !== undefined) {
      throw new LHMisconfigurationException('Cannot use jsonPath() and get() on the same variable')
    }
    const prim = this.typeDef.definedType?.$case
    if (prim !== 'primitiveType') {
      return this.structFieldRef(field)
    }
    const p = this.typeDef.definedType!.value
    if (p !== VT.JSON_OBJ && p !== VT.JSON_ARR) {
      throw new LHMisconfigurationException('get() is only valid on JSON_OBJ, JSON_ARR, or struct variables')
    }
    const next = [...this.lhSelectors, { selectorType: { $case: 'key' as const, value: field } }]
    return new WfRunVariableImpl(this.name, this.typeDef, this.defaultValue, this.thread, undefined, next)
  }

  private structFieldRef(field: string): WfRunVariable {
    if (this.typeDef.definedType?.$case !== 'structDefId') {
      throw new LHMisconfigurationException('get() is only valid on JSON_OBJ, JSON_ARR, or struct variables')
    }
    const next = [...this.lhSelectors, { selectorType: { $case: 'key' as const, value: field } }]
    return new WfRunVariableImpl(this.name, this.typeDef, this.defaultValue, this.thread, undefined, next)
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

  assign(rhs: WorkflowRhs): void {
    this.thread.mutate(this, VariableMutationType.ASSIGN, rhs)
  }
}

export function primitiveTypeDef(t: VariableType): TypeDefinition {
  return {
    definedType: { $case: 'primitiveType', value: t },
    masked: false,
  }
}

export function structTypeDef(structDefName: string): TypeDefinition {
  const id: StructDefId = { name: structDefName, version: 0 }
  return {
    definedType: { $case: 'structDefId', value: id },
    masked: false,
  }
}
