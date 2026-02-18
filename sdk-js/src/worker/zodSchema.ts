/**
 * Zod-based schema API for defining LittleHorse StructDefs and typed task inputs.
 *
 * Use standard Zod schemas with `lhStruct()` to define StructDefs, and
 * `zodToVariableDefs()` to derive typed `VariableDef[]` for TaskDef registration.
 *
 * ```ts
 * import { z } from 'zod'
 * import { lhStruct, zodToVariableDefs } from 'littlehorse-client'
 *
 * const Address = lhStruct('address', z.object({
 *   houseNumber: z.number().int(),
 *   street: z.string(),
 *   city: z.string(),
 * }))
 *
 * const Person = lhStruct('person', z.object({
 *   firstName: z.string(),
 *   lastName: z.string(),
 *   homeAddress: Address,
 * }))
 *
 * // TypeScript infers the shape from the Zod schema automatically
 * type Person = z.infer<typeof Person>
 *
 * // Derive VariableDefs for a task that takes (report: ParkingTicketReport, ctx: WorkerContext)
 * const inputVars = zodToVariableDefs({ report: ParkingTicketReport })
 * ```
 *
 * @module
 */

import { z, type ZodTypeAny, type ZodObject, type ZodRawShape } from 'zod'
import { VariableType } from '../proto/common_enums'
import { VariableDef, TypeDefinition, StructFieldDef } from '../proto/common_wfspec'
import {
  PutStructDefRequest,
  StructDefCompatibilityType,
} from '../proto/service'
import { VariableValue, StructField } from '../proto/variable'
import { toVariableValue } from './variableMapping'

// ── Metadata key for struct name ─────────────────────────────────────

const LH_STRUCT_NAME_KEY = '__lh_struct_name'

/**
 * Wraps a Zod object schema and associates it with a LittleHorse StructDef name.
 * The returned schema is still a normal Zod schema for parsing/validation, but
 * it carries metadata that the SDK uses for StructDef registration and
 * VariableDef generation.
 *
 * ```ts
 * const Address = lhStruct('address', z.object({
 *   houseNumber: z.number().int(),
 *   street: z.string(),
 *   city: z.string(),
 * }))
 *
 * type Address = z.infer<typeof Address>
 * ```
 *
 * @param name - The StructDef name on the server.
 * @param schema - A Zod object schema describing the struct fields.
 * @returns A Zod schema with LH struct metadata attached.
 */
export function lhStruct<T extends ZodRawShape>(
  name: string,
  schema: ZodObject<T>,
): ZodObject<T> {
  // Attach the struct name as Zod metadata using .describe() isn't ideal
  // because it overwrites. Instead we store it as a property on the schema.
  const tagged = schema as ZodObject<T> & { [LH_STRUCT_NAME_KEY]?: string }
  tagged[LH_STRUCT_NAME_KEY] = name
  return tagged
}

/**
 * Retrieves the LH struct name from a Zod schema created with `lhStruct()`.
 * Returns `undefined` if the schema doesn't have a struct name.
 */
export function getStructName(schema: ZodTypeAny): string | undefined {
  return (schema as any)[LH_STRUCT_NAME_KEY]
}

/**
 * Returns true if the given Zod schema was created with `lhStruct()`.
 */
export function isLHStruct(schema: ZodTypeAny): boolean {
  return getStructName(schema) !== undefined
}

// ── Zod → LH type mapping ───────────────────────────────────────────

/**
 * Maps a Zod schema to a LittleHorse `TypeDefinition`.
 *
 * Supported mappings:
 * - `z.string()` → `STR`
 * - `z.number()` → `DOUBLE` (or `INT` if `.int()` refinement is present)
 * - `z.boolean()` → `BOOL`
 * - `z.instanceof(Uint8Array)` / `z.instanceof(Buffer)` → `BYTES`
 * - `lhStruct('name', z.object({...}))` → struct reference
 * - `z.object({...})` without `lhStruct` → `JSON_OBJ`
 * - `z.array(...)` → `JSON_ARR`
 *
 * @internal
 */
export function zodToTypeDef(schema: ZodTypeAny): TypeDefinition {
  // Unwrap optionals/nullables/defaults
  const unwrapped = unwrapZod(schema)

  // Check for LH struct
  const structName = getStructName(unwrapped)
  if (structName) {
    return {
      definedType: {
        $case: 'structDefId',
        value: { name: structName, version: 0 },
      },
      masked: false,
    }
  }

  const typeName = (unwrapped._def as any).type as string

  switch (typeName) {
    case 'string':
      return primitiveDef(VariableType.STR)

    case 'number': {
      // Check if there's a `.int()` check on the number (Zod v4 stores checks array)
      const checks = (unwrapped._def as any).checks as Array<{ isInt?: boolean }> | undefined
      const isInt = checks?.some((c) => c.isInt) ?? false
      return primitiveDef(isInt ? VariableType.INT : VariableType.DOUBLE)
    }

    case 'boolean':
      return primitiveDef(VariableType.BOOL)

    case 'uint8Array':
      // z.instanceof(Uint8Array) or z.instanceof(Buffer)
      return primitiveDef(VariableType.BYTES)

    case 'object':
      // Plain z.object() without lhStruct → JSON_OBJ
      return primitiveDef(VariableType.JSON_OBJ)

    case 'array':
      return primitiveDef(VariableType.JSON_ARR)

    default:
      // Fall back to STR for enums, literals, unions, etc.
      return primitiveDef(VariableType.STR)
  }
}

/**
 * Converts a record of `{ paramName: ZodSchema }` into `VariableDef[]` for
 * TaskDef registration. Each key becomes the variable name, and the Zod schema
 * determines the LH type.
 *
 * ```ts
 * const inputVars = zodToVariableDefs({
 *   name: z.string(),
 *   age: z.number().int(),
 *   report: ParkingTicketReportSchema,
 * })
 * ```
 *
 * @param inputs - A record mapping parameter names to Zod schemas.
 * @returns An array of `VariableDef` for use in `PutTaskDefRequest`.
 */
export function zodToVariableDefs(inputs: Record<string, ZodTypeAny>): VariableDef[] {
  return Object.entries(inputs).map(([name, schema]) => ({
    name,
    typeDef: zodToTypeDef(schema),
  }))
}

// ── StructDef registration helpers ───────────────────────────────────

/**
 * Builds a `PutStructDefRequest` from a Zod schema created with `lhStruct()`.
 *
 * ```ts
 * const req = buildPutStructDefRequest(PersonSchema)
 * await client.putStructDef(req)
 * ```
 */
export function buildPutStructDefRequest(
  schema: ZodTypeAny,
  allowedUpdates: StructDefCompatibilityType = StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES,
): PutStructDefRequest {
  const name = getStructName(schema)
  if (!name) {
    throw new Error('buildPutStructDefRequest requires a schema created with lhStruct()')
  }

  const unwrapped = unwrapZod(schema)
  const shape = (unwrapped._def as any).shape?.() ?? (unwrapped._def as any).shape ?? {}
  const structDefFields: Record<string, StructFieldDef> = {}

  for (const [fieldName, fieldSchema] of Object.entries(shape) as [string, ZodTypeAny][]) {
    structDefFields[fieldName] = { fieldType: zodToTypeDef(fieldSchema) }
  }

  return {
    name,
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
  schema: ZodTypeAny,
): VariableDef {
  const name = getStructName(schema)
  if (!name) {
    throw new Error('buildStructVariableDef requires a schema created with lhStruct()')
  }

  return {
    name: paramName,
    typeDef: {
      definedType: {
        $case: 'structDefId',
        value: { name, version: 0 },
      },
      masked: false,
    },
  }
}

/**
 * Returns all LH struct schemas referenced (transitively) by `schema`,
 * in dependency order (deepest first), including `schema` itself.
 *
 * Useful for registering nested struct dependencies in the right order.
 */
export function getStructDependencies(schema: ZodTypeAny): ZodTypeAny[] {
  const visited = new Set<string>()
  const result: ZodTypeAny[] = []

  function walk(s: ZodTypeAny) {
    const unwrapped = unwrapZod(s)
    const name = getStructName(unwrapped)
    if (!name) return
    if (visited.has(name)) return
    visited.add(name)

    // Walk into object fields to find nested struct refs
    const shape = (unwrapped._def as any).shape?.() ?? (unwrapped._def as any).shape ?? {}
    for (const fieldSchema of Object.values(shape) as ZodTypeAny[]) {
      walk(fieldSchema)
    }
    result.push(unwrapped)
  }

  walk(schema)
  return result
}

// ── Struct value serialization ───────────────────────────────────────

/**
 * Converts a plain JS object into a Struct-typed `VariableValue`,
 * using the Zod schema to determine nested struct boundaries.
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
  schema: ZodTypeAny,
  structDefVersion: number = 0,
): VariableValue {
  const name = getStructName(schema)
  if (!name) {
    throw new Error('toStructVariableValue requires a schema created with lhStruct()')
  }

  const unwrapped = unwrapZod(schema)
  const shape = (unwrapped._def as any).shape?.() ?? (unwrapped._def as any).shape ?? {}
  const fields: Record<string, StructField> = {}

  for (const [key, val] of Object.entries(value)) {
    const fieldSchema = (shape as Record<string, ZodTypeAny>)[key]
    const unwrappedField = fieldSchema ? unwrapZod(fieldSchema) : undefined
    if (unwrappedField && getStructName(unwrappedField) && val !== null && val !== undefined && typeof val === 'object') {
      fields[key] = {
        value: toStructVariableValue(val as Record<string, unknown>, unwrappedField, structDefVersion),
      }
    } else {
      fields[key] = { value: toVariableValue(val) }
    }
  }

  return {
    value: {
      $case: 'struct',
      value: {
        structDefId: { name, version: structDefVersion },
        struct: { fields },
      },
    },
  }
}

// ── Internal helpers ─────────────────────────────────────────────────

function primitiveDef(type: VariableType): TypeDefinition {
  return {
    definedType: { $case: 'primitiveType', value: type },
    masked: false,
  }
}

/**
 * Unwraps optional/nullable/default/branded wrappers from a Zod schema
 * to get the underlying type.
 */
function unwrapZod(schema: ZodTypeAny): ZodTypeAny {
  // Preserve struct name through unwrapping
  const name = (schema as any)[LH_STRUCT_NAME_KEY]

  let current = schema
  const typeName = (current._def as any).type as string
  if (typeName === 'optional' || typeName === 'nullable' || typeName === 'default') {
    current = (current._def as any).innerType
  }

  // Re-attach struct name if present on the outer schema
  if (name && !getStructName(current)) {
    ;(current as any)[LH_STRUCT_NAME_KEY] = name
  }

  return current
}
