import { LHPath_Selector, VariableMutationType } from '../proto/common_wfspec'
import { TypeDefinition, VariableValue } from '../proto/type_definition'
import { VariableType } from '../proto/common_enums'
import { JsonIndex, ThreadVarDef, WfRunVariableAccessLevel } from '../proto/wf_spec'
import { objToVarVal, variableTypeFromValue } from './builder'
import { LHExpressionBase } from './expressions'
import type { WorkflowThread } from './WorkflowThread'

/** A variable in a WorkflowThread (compiles to a ThreadVarDef). */
export class WfRunVariable extends LHExpressionBase {
  typeDef: TypeDefinition
  defaultValue?: VariableValue
  isRequired = false
  isSearchable = false
  jsonIndexes: JsonIndex[] = []
  accessLevel: WfRunVariableAccessLevel = WfRunVariableAccessLevel.PRIVATE_VAR
  jsonPathStr?: string
  lhPath: LHPath_Selector[] = []

  constructor(
    readonly name: string,
    typeDef: TypeDefinition,
    readonly parent: WorkflowThread
  ) {
    super()
    this.typeDef = typeDef
  }

  static createPrimitive(
    name: string,
    typeOrDefaultVal: VariableType | unknown,
    parent: WorkflowThread
  ): WfRunVariable {
    if (typeOrDefaultVal === null || typeOrDefaultVal === undefined) {
      throw new Error("The 'typeOrDefaultVal' argument must be either a VariableType or a default value.")
    }
    if (typeof typeOrDefaultVal === 'number' && VariableType[typeOrDefaultVal] !== undefined) {
      return new WfRunVariable(
        name,
        TypeDefinition.create({
          definedType: { oneofKind: 'primitiveType', primitiveType: typeOrDefaultVal as VariableType },
        }),
        parent
      )
    }
    const val = objToVarVal(typeOrDefaultVal)
    const out = new WfRunVariable(
      name,
      TypeDefinition.create({
        definedType: { oneofKind: 'primitiveType', primitiveType: variableTypeFromValue(val) },
      }),
      parent
    )
    out.defaultValue = val
    return out
  }

  private get primitiveType(): VariableType | undefined {
    return this.typeDef.definedType.oneofKind === 'primitiveType' ? this.typeDef.definedType.primitiveType : undefined
  }

  jsonPath(path: string): WfRunVariable {
    if (this.jsonPathStr !== undefined) {
      throw new Error('Cannot use jsonPath() twice on same var!')
    }
    if (this.primitiveType !== VariableType.JSON_OBJ && this.primitiveType !== VariableType.JSON_ARR) {
      throw new Error(`jsonPath() not allowed on a non-JSON variable`)
    }
    const out = this.clone()
    out.jsonPathStr = path
    return out
  }

  get(fieldOrIndex: string | number): WfRunVariable {
    if (this.jsonPathStr !== undefined) {
      throw new Error('Cannot use jsonPath() and get() on same var!')
    }
    if (
      this.typeDef.definedType.oneofKind === 'primitiveType' &&
      this.primitiveType !== VariableType.JSON_OBJ &&
      this.primitiveType !== VariableType.JSON_ARR
    ) {
      throw new Error('Can only use get() on JSON_OBJ, JSON_ARR, Map, or Struct variables')
    }
    const out = this.clone()
    const selectorType: LHPath_Selector['selectorType'] =
      typeof fieldOrIndex === 'number'
        ? { oneofKind: 'index', index: fieldOrIndex }
        : { oneofKind: 'key', key: fieldOrIndex }
    out.lhPath.push({ selectorType })
    return out
  }

  searchable(): WfRunVariable {
    this.isSearchable = true
    return this
  }

  searchableOn(fieldPath: string, fieldType: VariableType): WfRunVariable {
    if (!fieldPath.startsWith('$.')) {
      throw new Error(`Invalid JsonPath: ${fieldPath}`)
    }
    if (this.primitiveType !== VariableType.JSON_OBJ && this.primitiveType !== VariableType.JSON_ARR) {
      throw new Error(`Non-Json ${this.name} variable cannot have a json index`)
    }
    this.jsonIndexes.push({ fieldPath, fieldType })
    return this
  }

  masked(): WfRunVariable {
    this.typeDef = { ...this.typeDef, masked: true }
    return this
  }

  required(): WfRunVariable {
    this.isRequired = true
    return this
  }

  withDefault(defaultVal: unknown): WfRunVariable {
    const val = objToVarVal(defaultVal)
    if (variableTypeFromValue(val) !== this.primitiveType) {
      throw new Error(`Default value type does not match LH variable type of ${this.name}`)
    }
    this.defaultValue = val
    return this
  }

  withAccessLevel(accessLevel: WfRunVariableAccessLevel): WfRunVariable {
    this.accessLevel = accessLevel
    return this
  }

  asPublic(): WfRunVariable {
    return this.withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR)
  }

  asInherited(): WfRunVariable {
    return this.withAccessLevel(WfRunVariableAccessLevel.INHERITED_VAR)
  }

  /** Assigns the result of an expression to this variable (on the innermost active thread). */
  assign(rhs: unknown): void {
    this.parent.workflow.mutateOnActiveThread(this, VariableMutationType.ASSIGN, rhs)
  }

  buildThreadVarDef(): ThreadVarDef {
    return ThreadVarDef.create({
      varDef: { name: this.name, typeDef: this.typeDef, defaultValue: this.defaultValue },
      required: this.isRequired,
      searchable: this.isSearchable,
      jsonIndexes: this.jsonIndexes,
      accessLevel: this.accessLevel,
    })
  }

  private clone(): WfRunVariable {
    const out = new WfRunVariable(this.name, this.typeDef, this.parent)
    out.defaultValue = this.defaultValue
    out.isRequired = this.isRequired
    out.isSearchable = this.isSearchable
    out.jsonIndexes = [...this.jsonIndexes]
    out.accessLevel = this.accessLevel
    if (this.jsonPathStr !== undefined) {
      out.jsonPathStr = this.jsonPathStr
    } else {
      out.lhPath = [...this.lhPath]
    }
    return out
  }
}
