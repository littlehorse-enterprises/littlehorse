import { VariableType } from '../proto/common_enums'
import { VariableDef, TypeDefinition, InlineStructDef, StructFieldDef } from '../proto/common_wfspec'
import {
  PutStructDefRequest,
  StructDefCompatibilityType,
} from '../proto/service'
import { VariableValue, StructField } from '../proto/variable'

// ── Metadata storage ──────────────────────────────────────────────────

interface FieldMeta {
  type: VariableType
  /** If type refers to a struct, this is the struct class. */
  structRef?: Constructor
  /** Default value for the field. If provided, the field is optional in the schema. */
  defaultValue?: unknown
  /** Whether the field value is sensitive and should be masked. */
  masked?: boolean
}

interface StructMeta {
  name: string
  description?: string
}

type Constructor<T = any> = new (...args: any[]) => T

const STRUCT_META_KEY = Symbol('lh:struct')
const FIELD_META_KEY = Symbol('lh:fields')

// ── @LHStruct decorator ──────────────────────────────────────────────

export interface LHStructOptions {
  /** The name for the StructDef on the server. Defaults to the class name. */
  name?: string
  /** Optional description. */
  description?: string
}

/**
 * Class decorator that marks a class as a LittleHorse StructDef.
 *
 * ```ts
 * @LHStruct({ name: 'Car', description: 'A car.' })
 * class Car {
 *   @LHField(VariableType.STR) make!: string
 *   @LHField(VariableType.INT) year!: number
 * }
 * ```
 */
export function LHStruct(options?: LHStructOptions): ClassDecorator {
  return (target: Function) => {
    const meta: StructMeta = {
      name: options?.name ?? target.name,
      description: options?.description,
    }
    ;(target as any)[STRUCT_META_KEY] = meta
  }
}

// ── @LHField decorator ──────────────────────────────────────────────

export interface LHFieldOptions {
  /** The primitive VariableType for this field. Mutually exclusive with `struct`. */
  type?: VariableType
  /** Reference to another @LHStruct-decorated class. Mutually exclusive with `type`. */
  struct?: Constructor
  /** Whether the field value is sensitive and should be masked. */
  masked?: boolean
}

/**
 * Property decorator that registers a field in an @LHStruct-decorated class.
 *
 * Can be called with just a VariableType for convenience:
 * ```ts
 * @LHField(VariableType.STR) make!: string
 * ```
 *
 * Or with full options for struct references or masking:
 * ```ts
 * @LHField({ struct: Engine }) engine!: Engine
 * @LHField({ type: VariableType.STR, masked: true }) ssn!: string
 * ```
 */
export function LHField(typeOrOptions: VariableType | LHFieldOptions): PropertyDecorator {
  return (target: Object, propertyKey: string | symbol) => {
    const ctor = target.constructor as any
    if (!ctor[FIELD_META_KEY]) {
      ctor[FIELD_META_KEY] = new Map<string, FieldMeta>()
    }
    const fields: Map<string, FieldMeta> = ctor[FIELD_META_KEY]

    let meta: FieldMeta
    if (typeof typeOrOptions === 'string') {
      // Called as @LHField(VariableType.STR)
      meta = { type: typeOrOptions as VariableType }
    } else {
      const opts = typeOrOptions as LHFieldOptions
      if (opts.struct) {
        // The field is a nested struct
        meta = { type: VariableType.JSON_OBJ, structRef: opts.struct, masked: opts.masked }
      } else {
        meta = { type: opts.type!, masked: opts.masked }
      }
    }

    fields.set(String(propertyKey), meta)
  }
}

// ── Utility: extract metadata ────────────────────────────────────────

function getStructMeta(cls: Constructor): StructMeta {
  const meta = (cls as any)[STRUCT_META_KEY] as StructMeta | undefined
  if (!meta) {
    throw new Error(
      `Class "${cls.name}" is not decorated with @LHStruct. ` +
        `Add @LHStruct() to the class declaration.`
    )
  }
  return meta
}

function getFieldMetas(cls: Constructor): Map<string, FieldMeta> {
  const fields = (cls as any)[FIELD_META_KEY] as Map<string, FieldMeta> | undefined
  if (!fields || fields.size === 0) {
    throw new Error(
      `Class "${cls.name}" has no @LHField-decorated properties. ` +
        `Add @LHField(...) to at least one property.`
    )
  }
  return fields
}

// ── Public API: generate proto objects from decorated classes ─────────

/**
 * Returns the StructDef name for an @LHStruct-decorated class.
 */
export function getStructDefName(cls: Constructor): string {
  return getStructMeta(cls).name
}

/**
 * Builds a `PutStructDefRequest` from an @LHStruct-decorated class.
 *
 * ```ts
 * const request = buildPutStructDefRequest(Car)
 * await client.putStructDef(request)
 * ```
 */
export function buildPutStructDefRequest(
  cls: Constructor,
  allowedUpdates: StructDefCompatibilityType = StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES
): PutStructDefRequest {
  const structMeta = getStructMeta(cls)
  const fields = getFieldMetas(cls)

  const structDefFields: { [key: string]: StructFieldDef } = {}

  for (const [fieldName, fieldMeta] of fields) {
    const typeDef = fieldMetaToTypeDef(fieldMeta)
    structDefFields[fieldName] = { fieldType: typeDef }
  }

  return {
    name: structMeta.name,
    description: structMeta.description,
    allowedUpdates,
    structDef: { fields: structDefFields },
  }
}

/**
 * Builds a `VariableDef` for a task input parameter whose type is an
 * @LHStruct-decorated class.
 *
 * ```ts
 * const inputVars = [buildStructVariableDef('car', Car)]
 * ```
 */
export function buildStructVariableDef(paramName: string, cls: Constructor): VariableDef {
  const structMeta = getStructMeta(cls)
  return {
    name: paramName,
    typeDef: {
      definedType: {
        $case: 'structDefId',
        value: { name: structMeta.name, version: 0 },
      },
      masked: false,
    },
  }
}

/**
 * Returns all @LHStruct-decorated classes referenced (transitively) by `cls`,
 * in dependency order (deepest first), including `cls` itself.
 * Useful for registering nested struct dependencies in the right order.
 */
export function getStructDependencies(cls: Constructor): Constructor[] {
  const visited = new Set<Constructor>()
  const result: Constructor[] = []

  function walk(c: Constructor) {
    if (visited.has(c)) return
    visited.add(c)
    const fields = getFieldMetas(c)
    for (const fieldMeta of fields.values()) {
      if (fieldMeta.structRef) {
        walk(fieldMeta.structRef)
      }
    }
    result.push(c)
  }

  walk(cls)
  return result
}

// ── Internal helpers ─────────────────────────────────────────────────

function fieldMetaToTypeDef(meta: FieldMeta): TypeDefinition {
  if (meta.structRef) {
    const nestedMeta = getStructMeta(meta.structRef)
    return {
      definedType: {
        $case: 'structDefId',
        value: { name: nestedMeta.name, version: 0 },
      },
      masked: meta.masked ?? false,
    }
  }

  return {
    definedType: {
      $case: 'primitiveType',
      value: meta.type,
    },
    masked: meta.masked ?? false,
  }
}

// ── Struct value serialization ───────────────────────────────────────

// Lazy import to avoid circular dependency — toVariableValue is only
// needed at runtime, not at import/decoration time.
let _toVariableValue: ((value: unknown) => VariableValue) | undefined

function getToVariableValue(): (value: unknown) => VariableValue {
  if (!_toVariableValue) {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    _toVariableValue = require('./variableMapping').toVariableValue
  }
  return _toVariableValue!
}

/**
 * Converts a plain JS object into a Struct-typed `VariableValue` using
 * the field metadata from an `@LHStruct`-decorated class.
 *
 * Fields decorated with `@LHField({ struct: SomeClass })` are recursively
 * serialized as nested `struct` values instead of `jsonObj`.
 *
 * ```ts
 * const carValue = toStructVariableValue(myCar, Car)
 * ```
 *
 * @param value - The plain object whose keys correspond to `@LHField` properties.
 * @param cls - The `@LHStruct`-decorated class describing the schema.
 * @param structDefVersion - Optional version of the StructDef (default 0).
 */
export function toStructVariableValue(
  value: Record<string, unknown>,
  cls: Constructor,
  structDefVersion?: number
): VariableValue
/**
 * Converts a plain JS object into a Struct-typed `VariableValue` using
 * a structDefName string. All fields are serialized as primitives (no
 * nested struct awareness). Prefer the class-based overload when using
 * `@LHStruct` decorators.
 */
export function toStructVariableValue(
  value: Record<string, unknown>,
  structDefName: string,
  structDefVersion?: number
): VariableValue
export function toStructVariableValue(
  value: Record<string, unknown>,
  clsOrName: Constructor | string,
  structDefVersion: number = 0
): VariableValue {
  if (typeof clsOrName === 'string') {
    // Legacy string-based path: no field metadata available
    return toStructVariableValueFromName(value, clsOrName, structDefVersion)
  }

  const cls = clsOrName
  const structMeta = getStructMeta(cls)
  const fieldMetas = getFieldMetas(cls)
  const toVal = getToVariableValue()

  const fields: { [key: string]: StructField } = {}
  for (const [key, val] of Object.entries(value)) {
    const fieldMeta = fieldMetas.get(key)
    if (fieldMeta?.structRef && val !== null && val !== undefined && typeof val === 'object') {
      // Recursively serialize nested struct
      fields[key] = {
        value: toStructVariableValue(val as Record<string, unknown>, fieldMeta.structRef),
      }
    } else {
      fields[key] = { value: toVal(val) }
    }
  }

  return {
    value: {
      $case: 'struct',
      value: {
        structDefId: { name: structMeta.name, version: structDefVersion },
        struct: { fields },
      },
    },
  }
}

/**
 * Simple string-name-based struct serialization (no decorator metadata).
 * All fields are serialized as primitive VariableValues.
 */
function toStructVariableValueFromName(
  value: Record<string, unknown>,
  structDefName: string,
  structDefVersion: number
): VariableValue {
  const toVal = getToVariableValue()
  const fields: { [key: string]: StructField } = {}
  for (const [key, val] of Object.entries(value)) {
    fields[key] = { value: toVal(val) }
  }
  return {
    value: {
      $case: 'struct',
      value: {
        structDefId: { name: structDefName, version: structDefVersion },
        struct: { fields },
      },
    },
  }
}

// ── Struct-aware result serialization ────────────────────────────────

/**
 * Checks whether `value` is an instance of an `@LHStruct`-decorated class.
 * If so, serializes it as a Struct-typed `VariableValue` and returns it.
 * Otherwise, returns `undefined` so the caller can fall back to `toVariableValue`.
 *
 * This is used internally by `LHTaskWorker.executeTask` so that task functions
 * returning struct instances automatically produce proper Struct values,
 * mirroring the Java SDK behavior.
 */
export function trySerializeAsStruct(value: unknown): VariableValue | undefined {
  if (value === null || value === undefined || typeof value !== 'object') {
    return undefined
  }
  const ctor = (value as any).constructor as Constructor | undefined
  if (!ctor) return undefined

  const meta = (ctor as any)[STRUCT_META_KEY] as StructMeta | undefined
  if (!meta) return undefined

  // It's an @LHStruct instance — serialize using field metadata
  return toStructVariableValue(value as Record<string, unknown>, ctor)
}
