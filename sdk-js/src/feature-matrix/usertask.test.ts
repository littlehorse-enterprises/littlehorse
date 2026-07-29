import { z } from 'zod'
import { LHConfig } from '../LHConfig'
import { VariableType } from '../proto/common_enums'
import { LHMisconfigurationError } from '../common'
import { userTaskSchema, UserTaskSchema } from '../usertask'
import { FakeLHServer } from './fakeServer'

/**
 * Feature matrix: user task schemas.
 *
 * See sdk-js/PARITY_PLAN.md. Java derives UserTaskDef schemas by reflecting
 * over annotated classes (usertask/UserTaskSchema, @UserTaskField); the JS
 * equivalent describes fields with zod — as the worker already does for task
 * inputs and structs — while producing the same PutUserTaskDefRequest proto.
 */

describe('usertask', () => {
  test('define a user task schema from a typed object definition — Java: UserTaskSchema', () => {
    const schema = userTaskSchema('approve-request', {
      approved: z.boolean(),
      notes: z.string(),
    })

    expect(schema).toBeInstanceOf(UserTaskSchema)
    expect(schema.name).toBe('approve-request')
    expect(schema.compile().fields.map(f => f.name)).toEqual(['approved', 'notes'])

    // A form with no fields is a misconfiguration, not an empty form.
    expect(() => userTaskSchema('empty', {})).toThrow(LHMisconfigurationError)

    // Fields must be renderable primitives; a nested object would silently
    // become JSON, which the form cannot present.
    expect(() => userTaskSchema('bad', { blob: z.object({ a: z.string() }) }).compile()).toThrow(
      LHMisconfigurationError
    )
  })

  test('compile the schema to a PutUserTaskDefRequest — Java: UserTaskSchema#compile', () => {
    const request = userTaskSchema(
      'approve-request',
      { approved: z.boolean() },
      'Approve or reject the pending request'
    ).compile()

    expect(request.name).toBe('approve-request')
    expect(request.description).toBe('Approve or reject the pending request')
    expect(request.fields).toHaveLength(1)

    // compile() is idempotent — the same proto every time, as Java caches it.
    const schema = userTaskSchema('cached', { ok: z.boolean() })
    expect(schema.compile()).toBe(schema.compile())
  })

  test('map field types, display names, descriptions, and required flags — Java: @UserTaskField', () => {
    const request = userTaskSchema('rich-form', {
      approved: z.boolean(),
      score: z.number().int(),
      ratio: z.number(),
      reviewer: { schema: z.string(), displayName: 'Reviewing user', description: 'Who signed off' },
      notes: { schema: z.string(), required: false },
      attachment: z.string().optional(),
    }).compile()

    const byName = Object.fromEntries(request.fields.map(f => [f.name, f]))

    expect(byName.approved.type).toBe(VariableType.BOOL)
    expect(byName.score.type).toBe(VariableType.INT)
    expect(byName.ratio.type).toBe(VariableType.DOUBLE)
    expect(byName.reviewer.type).toBe(VariableType.STR)

    // Display name defaults to the field name; description is optional.
    expect(byName.approved.displayName).toBe('approved')
    expect(byName.reviewer.displayName).toBe('Reviewing user')
    expect(byName.reviewer.description).toBe('Who signed off')
    expect(byName.approved.description).toBeUndefined()

    // Required by default, explicitly overridable, and inferred as optional
    // when the zod schema itself is optional.
    expect(byName.approved.required).toBe(true)
    expect(byName.notes.required).toBe(false)
    expect(byName.attachment.required).toBe(false)
  })

  test('register the UserTaskDef with the server — Java: UserTaskSchema + client', async () => {
    const server = new FakeLHServer()
    await server.start()
    try {
      const config = LHConfig.fromMap({ LHC_API_HOST: '127.0.0.1', LHC_API_PORT: String(server.port) })
      const schema = userTaskSchema('approve-request', {
        approved: z.boolean(),
        notes: { schema: z.string(), required: false },
      })

      const result = await schema.register(config)
      expect(result.name).toBe('approve-request')

      // The server received exactly what compile() produced.
      expect(server.putUserTaskDefRequests).toHaveLength(1)
      expect(server.putUserTaskDefRequests[0]).toEqual(schema.compile())
    } finally {
      await server.stop()
    }
  }, 20000)
})
