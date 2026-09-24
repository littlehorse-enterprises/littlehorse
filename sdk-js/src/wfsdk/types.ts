import type { ZodTypeAny } from 'zod'
import { TypeDefinition } from '../proto/type_definition'
import { VariableType } from '../proto/common_enums'
import { getStructName } from '../worker/zodSchema'

/**
 * Describes the type of a native LittleHorse variable, for the typed
 * collection and struct declarations (`declareArray`, `declareMap`,
 * `declareStruct`). Java expresses these with `Class<?>`; JS has no runtime
 * class metadata, so types are described explicitly.
 *
 * Note the distinction from `declareJsonArr`/`declareJsonObj`: those produce
 * schemaless JSON_ARR/JSON_OBJ values, whereas an `arrayOf`/`mapOf` type is a
 * native, element-typed LH collection (`InlineArrayDef`/`InlineMapDef`).
 */
export type LHType =
  | VariableType
  | ZodTypeAny
  | { kind: 'array'; element: LHType }
  | { kind: 'map'; key: LHType; value: LHType }
  | { kind: 'struct'; name: string; version?: number }

/** Native LH array of `element` (not a JSON_ARR). */
export function arrayOf(element: LHType): LHType {
  return { kind: 'array', element }
}

/** Native LH map with typed keys and values (not a JSON_OBJ). */
export function mapOf(key: LHType, value: LHType): LHType {
  return { kind: 'map', key, value }
}

/** Reference to a registered StructDef. Version -1 (the default) means latest. */
export function structOf(name: string, version?: number): LHType {
  return { kind: 'struct', name, version }
}

function isZodSchema(value: unknown): value is ZodTypeAny {
  return typeof value === 'object' && value !== null && '_def' in value
}

/** Converts an LHType descriptor into its proto TypeDefinition. */
export function toTypeDefinition(type: LHType): TypeDefinition {
  if (typeof type === 'number') {
    return TypeDefinition.create({
      definedType: { oneofKind: 'primitiveType', primitiveType: type },
    })
  }

  if ('kind' in type) {
    switch (type.kind) {
      case 'array': {
        const arrayType = toTypeDefinition(type.element)
        ensureNoJsonPrimitive(arrayType, 'array element')
        return TypeDefinition.create({
          definedType: { oneofKind: 'inlineArrayDef', inlineArrayDef: { arrayType } },
        })
      }
      case 'map': {
        const keyType = toTypeDefinition(type.key)
        const valueType = toTypeDefinition(type.value)
        ensureNoJsonPrimitive(keyType, 'map key')
        ensureNoJsonPrimitive(valueType, 'map value')
        return TypeDefinition.create({
          definedType: { oneofKind: 'inlineMapDef', inlineMapDef: { keyType, valueType } },
        })
      }
      case 'struct':
        return TypeDefinition.create({
          definedType: {
            oneofKind: 'structDefId',
            structDefId: { name: type.name, version: type.version ?? -1 },
          },
        })
    }
  }

  if (isZodSchema(type)) {
    const structName = getStructName(type)
    if (structName === undefined) {
      throw new Error(
        'Only lhStruct(...)-annotated zod schemas describe an LH type here; use a VariableType for primitives.'
      )
    }
    return toTypeDefinition(structOf(structName))
  }

  throw new Error(`Unrecognized LH type descriptor: ${JSON.stringify(type)}`)
}

/**
 * Native collections cannot hold schemaless JSON values (mirrors Java's
 * LHTypeConstraintValidator#ensureNoJsonPrimitiveTypes).
 */
function ensureNoJsonPrimitive(typeDef: TypeDefinition, position: string): void {
  if (typeDef.definedType.oneofKind !== 'primitiveType') return
  const primitive = typeDef.definedType.primitiveType
  if (primitive === VariableType.JSON_OBJ || primitive === VariableType.JSON_ARR) {
    throw new Error(`${VariableType[primitive]} is not allowed as a native ${position} type`)
  }
}
