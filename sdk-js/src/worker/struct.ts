/**
 * Functional, schema-first API for defining LittleHorse StructDefs.
 *
 * Instead of classes and decorators, structs are defined as plain data:
 *
 * ```ts
 * import { lhStruct, lh, Infer } from 'littlehorse-client'
 *
 * const Address = lhStruct('address', {
 *   houseNumber: lh.INT,
 *   street: lh.STR,
 *   city: lh.STR,
 * })
 *
 * const Person = lhStruct('person', {
 *   firstName: lh.STR,
 *   lastName: lh.STR,
 *   homeAddress: lh.struct(Address),
 * })
 *
 * // TypeScript infers the shape — no manual interface needed
 * type Person = Infer<typeof Person>
 * // → { firstName: string, lastName: string, homeAddress: { houseNumber: number, ... } }
 * ```
 *
 * @module
 */

import { VariableType } from '../proto/common_enums'
import { VariableDef, TypeDefinition, StructFieldDef } from '../proto/common_wfspec'
import {
  PutStructDefRequest,
  StructDefCompatibilityType,
} from '../proto/service'
import { VariableValue, StructField } from '../proto/variable'
import { toVariableValue } from './variableMapping'

// ── Field definitions ────────────────────────────────────────────────

/**
 * A primitive field definition (string, int, double, bool, bytes).
 * The phantom `_phantom` property carries the TypeScript type for inference.
 */
export interface PrimitiveField<T = unknown> {
  readonly _tag: 'primitive'
  readonly variableType: VariableType
  readonly masked: boolean
  /** @internal Phantom property for type inference — never set at runtime. */
  readonly _phantom?: T
}

/**
 * A field that references another struct schema.
 */
export interface StructRefField<S extends LHStructSchema = LHStructSchema> {
  readonly _tag: 'struct'
  readonly schema: S
  readonly masked: boolean
}

/** A field in an `lhStruct` definition. */
export type FieldDef = PrimitiveField | StructRefField

// ── Schema object ────────────────────────────────────────────────────

/**
 * A struct schema created by {@link lhStruct}. Carries the struct name,
 * optional description, and the field definitions.
 *
 * Use `Infer<typeof MySchema>` to derive the TypeScript type.
 */
export interface LHStructSchema<
  F extends Record<string, FieldDef> = Record<string, FieldDef>,
> {
  readonly name: string
  readonly description?: string
  readonly fields: F
}

// ── Type inference ───────────────────────────────────────────────────

/** Infers the TypeScript type of a single field. */
type InferField<F extends FieldDef> =
  F extends PrimitiveField<infer T> ? T :
  F extends StructRefField<infer S> ? Infer<S> :
  never

/**
 * Infers the TypeScript type from a struct schema.
 *
 * ```ts
 * const Car = lhStruct('car', { make: lh.STR, year: lh.INT })
 * type Car = Infer<typeof Car>
 * // → { make: string, year: number }
 * ```
 */
export type Infer<S extends LHStructSchema> = {
  [K in keyof S['fields']]: InferField<S['fields'][K]>
}

// ── Field helpers (the `lh` namespace) ───────────────────────────────

function primitiveField<T>(variableType: VariableType): PrimitiveField<T> {
  return { _tag: 'primitive', variableType, masked: false }
}

/**
 * Field-type helpers for use inside `lhStruct` definitions.
 *
 * ```ts
 * const Car = lhStruct('car', {
 *   make: lh.STR,
 *   year: lh.INT,
 *   engine: lh.struct(Engine),
 * })
 * ```
 */
export const lh = {
  /** A string field. */
  STR: primitiveField<string>(VariableType.STR),
  /** An integer field. */
  INT: primitiveField<number>(VariableType.INT),
  /** A double/float field. */
  DOUBLE: primitiveField<number>(VariableType.DOUBLE),
  /** A boolean field. */
  BOOL: primitiveField<boolean>(VariableType.BOOL),
  /** A bytes field. */
  BYTES: primitiveField<Uint8Array>(VariableType.BYTES),

  /**
   * A nested struct field. Pass another schema created with `lhStruct`.
   *
   * ```ts
   * const Person = lhStruct('person', {
   *   homeAddress: lh.struct(Address),
   * })
   * ```
   */
  struct<S extends LHStructSchema>(schema: S): StructRefField<S> {
    return { _tag: 'struct', schema, masked: false }
  },

  /**
   * Creates a masked (sensitive) variant of a primitive field.
   *
   * ```ts
   * const User = lhStruct('user', {
   *   ssn: lh.masked(lh.STR),
   * })
   * ```
   */
  masked<F extends PrimitiveField>(field: F): F {
    return { ...field, masked: true }
  },
} as const

// ── lhStruct factory ─────────────────────────────────────────────────

export interface LHStructOptions {
  /** Optional description for the StructDef. */
  description?: string
}

/**
 * Defines a LittleHorse struct schema.
 *
 * ```ts
 * const Car = lhStruct('car', {
 *   make: lh.STR,
 *   model: lh.STR,
 *   year: lh.INT,
 * })
 * ```
 *
 * @param name - The StructDef name on the server.
 * @param fields - An object mapping field names to field types (`lh.STR`, `lh.INT`, `lh.struct(...)`, etc.).
 * @param options - Optional additional settings like `description`.
 */
export function lhStruct<F extends Record<string, FieldDef>>(
  name: string,
  fields: F,
  options?: LHStructOptions,
): LHStructSchema<F> {
  return { name, fields, description: options?.description }
}

// ── Proto generation ─────────────────────────────────────────────────

/**
 * Returns the StructDef name from a schema.
 */
export function getStructDefName(schema: LHStructSchema): string {
  return schema.name
}

/**
 * Builds a `PutStructDefRequest` from a struct schema.
 *
 * ```ts
 * const req = buildPutStructDefRequest(Car)
 * await client.putStructDef(req)
 * ```
 */
export function buildPutStructDefRequest(
  schema: LHStructSchema,
  allowedUpdates: StructDefCompatibilityType = StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES,
): PutStructDefRequest {
  const structDefFields: { [key: string]: StructFieldDef } = {}

  for (const [fieldName, field] of Object.entries(schema.fields)) {
    structDefFields[fieldName] = { fieldType: fieldDefToTypeDef(field) }
  }

  return {
    name: schema.name,
    description: schema.description,
    allowedUpdates,
    structDef: { fields: structDefFields },
  }
}

/**
 * Builds a `VariableDef` for a task input parameter whose type is a struct.
 *
 * ```ts
 * const inputVars = [buildStructVariableDef('car', CarSchema)]
 * ```
 */
export function buildStructVariableDef(
  paramName: string,
  schema: LHStructSchema,
): VariableDef {
  return {
    name: paramName,
    typeDef: {
      definedType: {
        $case: 'structDefId',
        value: { name: schema.name, version: 0 },
      },
      masked: false,
    },
  }
}

/**
 * Returns all struct schemas referenced (transitively) by `schema`,
 * in dependency order (deepest first), including `schema` itself.
 *
 * Useful for registering nested struct dependencies in the right order.
 */
export function getStructDependencies(schema: LHStructSchema): LHStructSchema[] {
  const visited = new Set<string>()
  const result: LHStructSchema[] = []

  function walk(s: LHStructSchema) {
    if (visited.has(s.name)) return
    visited.add(s.name)
    for (const field of Object.values(s.fields)) {
      if (field._tag === 'struct') {
        walk(field.schema)
      }
    }
    result.push(s)
  }

  walk(schema)
  return result
}

// ── Struct value serialization ───────────────────────────────────────

/**
 * Converts a plain JS object into a Struct-typed `VariableValue`,
 * recursively handling nested struct fields.
 *
 * ```ts
 * const personValue = toStructVariableValue(
 *   { firstName: 'Obi-Wan', homeAddress: { houseNumber: 124, ... } },
 *   PersonSchema,
 * )
 * ```
 */
export function toStructVariableValue(
  value: Record<string, unknown>,
  schema: LHStructSchema,
  structDefVersion: number = 0,
): VariableValue {
  const fields: { [key: string]: StructField } = {}

  for (const [key, val] of Object.entries(value)) {
    const fieldDef = schema.fields[key]
    if (fieldDef?._tag === 'struct' && val !== null && val !== undefined && typeof val === 'object') {
      fields[key] = {
        value: toStructVariableValue(val as Record<string, unknown>, fieldDef.schema),
      }
    } else {
      fields[key] = { value: toVariableValue(val) }
    }
  }

  return {
    value: {
      $case: 'struct',
      value: {
        structDefId: { name: schema.name, version: structDefVersion },
        struct: { fields },
      },
    },
  }
}

// ── Internal helpers ─────────────────────────────────────────────────

function fieldDefToTypeDef(field: FieldDef): TypeDefinition {
  if (field._tag === 'struct') {
    return {
      definedType: {
        $case: 'structDefId',
        value: { name: field.schema.name, version: 0 },
      },
      masked: field.masked,
    }
  }

  return {
    definedType: {
      $case: 'primitiveType',
      value: field.variableType,
    },
    masked: field.masked,
  }
}
