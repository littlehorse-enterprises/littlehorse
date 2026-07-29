import type { ZodTypeAny } from 'zod'
import type { LHPublicClient } from '../client'
import type { LHConfig } from '../LHConfig'
import { PutUserTaskDefRequest } from '../proto/service'
import { UserTaskDef, UserTaskField } from '../proto/user_tasks'
import { VariableType } from '../proto/common_enums'
import { zodToTypeDef } from '../worker/zodSchema'
import { LHMisconfigurationError } from '../common'

/**
 * User Task schemas (Java: usertask/UserTaskSchema + @UserTaskField).
 *
 * Java derives the schema by reflecting over an annotated class. JS has no
 * equivalent annotations, so fields are described with zod — the same choice
 * the worker already makes for task inputs and structs — plus the presentation
 * metadata (display name, description) that zod has no concept of.
 */

/** Presentation metadata for one field of a User Task form. */
export interface UserTaskFieldOptions {
  /** Zod schema; determines the field's VariableType. */
  schema: ZodTypeAny
  /** Human-readable label. Defaults to the field name. */
  displayName?: string
  description?: string
  /** Defaults to true unless the schema is optional/nullable. */
  required?: boolean
}

export type UserTaskFieldsInput = Record<string, ZodTypeAny | UserTaskFieldOptions>

function isFieldOptions(value: ZodTypeAny | UserTaskFieldOptions): value is UserTaskFieldOptions {
  return typeof value === 'object' && value !== null && 'schema' in value
}

/** True when a zod schema is optional, nullable, or has a default. */
function isOptionalSchema(schema: ZodTypeAny): boolean {
  return schema.safeParse(undefined).success
}

/**
 * A User Task form definition that compiles to a PutUserTaskDefRequest.
 *
 * ```ts
 * const approval = userTaskSchema('approve-request', {
 *   approved: z.boolean(),
 *   notes: { schema: z.string(), displayName: 'Reviewer notes', required: false },
 * })
 * await approval.register(config)
 * ```
 */
export class UserTaskSchema {
  private compiled?: PutUserTaskDefRequest

  constructor(
    readonly name: string,
    private readonly fields: UserTaskFieldsInput,
    private readonly description?: string
  ) {
    if (Object.keys(fields).length === 0) {
      throw new LHMisconfigurationError(`UserTaskDef '${name}' must declare at least one field`)
    }
  }

  /** Compiles to the proto the server accepts (Java: UserTaskSchema#compile). */
  compile(): PutUserTaskDefRequest {
    if (this.compiled !== undefined) return this.compiled

    const fields: UserTaskField[] = Object.entries(this.fields).map(([fieldName, definition]) => {
      const options: UserTaskFieldOptions = isFieldOptions(definition) ? definition : { schema: definition }
      return UserTaskField.create({
        name: fieldName,
        type: userTaskFieldType(fieldName, options.schema),
        displayName: options.displayName ?? fieldName,
        description: options.description,
        required: options.required ?? !isOptionalSchema(options.schema),
      })
    })

    this.compiled = PutUserTaskDefRequest.create({
      name: this.name,
      fields,
      description: this.description,
    })
    return this.compiled
  }

  /** Registers the UserTaskDef with the server. */
  async register(configOrClient: LHConfig | LHPublicClient): Promise<UserTaskDef> {
    const client = isClient(configOrClient) ? configOrClient : configOrClient.getClient()
    return client.putUserTaskDef(this.compile())
  }
}

function isClient(value: LHConfig | LHPublicClient): value is LHPublicClient {
  return typeof (value as LHPublicClient).putUserTaskDef === 'function'
}

/**
 * A User Task field is a primitive the form can render, so struct and
 * collection schemas are rejected rather than silently becoming JSON.
 */
function userTaskFieldType(fieldName: string, schema: ZodTypeAny): VariableType {
  const typeDef = zodToTypeDef(schema)
  if (typeDef.definedType.oneofKind !== 'primitiveType') {
    throw new LHMisconfigurationError(`UserTaskDef field '${fieldName}' must be a primitive type`)
  }
  const primitive = typeDef.definedType.primitiveType
  if (primitive === VariableType.JSON_OBJ || primitive === VariableType.JSON_ARR) {
    throw new LHMisconfigurationError(
      `UserTaskDef field '${fieldName}' cannot be ${VariableType[primitive]}; use a primitive field type`
    )
  }
  return primitive
}

/** Convenience wrapper mirroring `lhStruct(...)` for task structs. */
export function userTaskSchema(name: string, fields: UserTaskFieldsInput, description?: string): UserTaskSchema {
  return new UserTaskSchema(name, fields, description)
}
