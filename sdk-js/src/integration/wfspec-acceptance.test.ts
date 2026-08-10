import { beforeAll, describe, expect, test } from '@jest/globals'
import { z } from 'zod'
import type { LHPublicClient } from '../client'
import { StructDefCompatibilityType } from '../proto/service'
import { VariableType } from '../proto/common_enums'
import { TypeDefinition } from '../proto/type_definition'
import { referenceWorkflows } from '../feature-matrix/referenceWorkflows'
import { Workflow } from '../wfsdk'
import { registerRequiredTaskDefs, requireServer, tenantClient } from './harness'

/**
 * Does the real server accept what the JS wfsdk compiles?
 *
 * The golden tests prove our compiled proto is byte-identical to the Java
 * SDK's. That is necessary but not sufficient: it says nothing about whether
 * the server considers the spec *valid*. If both SDKs emitted something the
 * server rejects, every golden test would still pass. This closes that gap.
 *
 * Making these pass documented the real prerequisites of a WfSpec, none of
 * which an offline test could surface:
 *  - a TaskDef's input vars must match the arity and types of the execute()
 *    call referencing it;
 *  - ExternalEventDefs, WorkflowEventDefs, UserTaskDefs, and child WfSpecs
 *    must already exist;
 *  - a pinned StructDef version must exist AND contain every field the
 *    workflow's struct builder sets.
 */

let client: LHPublicClient

/**
 * Input vars for TaskDefs that take arguments; unlisted tasks take none.
 * Mirrors the execute() calls in referenceWorkflows.ts — the server rejects
 * any mismatch.
 */
const TASK_INPUT_VARS: Record<string, Record<string, z.ZodTypeAny>> = {
  greet: { name: z.string() },
  fulfill: { 'order-id': z.string(), payment: z.object({}) },
}

/** The version the `structs` reference workflow pins. */
const CUSTOMER_STRUCT_PINNED_VERSION = 3

function primitive(type: VariableType): TypeDefinition {
  return TypeDefinition.create({ definedType: { oneofKind: 'primitiveType', primitiveType: type } })
}

beforeAll(async () => {
  await requireServer()
  client = await tenantClient()

  await client.putUserTaskDef({ name: 'approve-request', fields: [] })

  // A nested object field must reference a separately registered StructDef.
  // The server rejects an inlineStructDef nested inside a StructDef with
  // "Forbidden JSON type: JSON_OBJ ... use native equivalents such as
  // StructDefs for nested object types" — a constraint no offline test could
  // have surfaced.
  await client.putStructDef({
    name: 'address-struct',
    allowedUpdates: StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES,
    structDef: {
      fields: {
        city: { fieldType: primitive(VariableType.STR), isNullable: false },
        zip: { fieldType: primitive(VariableType.STR), isNullable: false },
      },
    },
  })

  // customer-struct must contain every field the workflow's struct builder
  // sets. Each compatible change bumps the version, so registering repeatedly
  // reaches the pinned version deterministically.
  for (let version = 0; version <= CUSTOMER_STRUCT_PINNED_VERSION; version++) {
    const fields: Record<string, { fieldType: TypeDefinition; isNullable: boolean }> = {
      name: { fieldType: primitive(VariableType.STR), isNullable: false },
      age: { fieldType: primitive(VariableType.INT), isNullable: false },
      address: {
        fieldType: TypeDefinition.create({
          definedType: { oneofKind: 'structDefId', structDefId: { name: 'address-struct', version: 0 } },
        }),
        isNullable: false,
      },
    }
    for (let extra = 0; extra < version; extra++) {
      fields[`filler${extra}`] = { fieldType: primitive(VariableType.STR), isNullable: true }
    }
    await client.putStructDef({
      name: 'customer-struct',
      structDef: { fields },
      allowedUpdates: StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES,
    })
  }

  // Events referenced by waitForEvent / throwEvent / registerInterruptHandler.
  for (const name of ['payment-received', 'cancel-requested']) {
    await client.putExternalEventDef({ name, contentType: {} })
  }
  await client.putWorkflowEventDef({ name: 'milestone-reached', contentType: {} })

  // child-workflow spawns "shipping-wf", which must already exist. Built with
  // the SDK itself so the setup can't drift from what the SDK emits.
  const shippingWf = Workflow.newWorkflow('shipping-wf', thread => {
    thread.declareStr('order-id')
  })
  await client.putWfSpec(shippingWf.compileWorkflow())
}, 120000)

describe('real server accepts wfsdk output', () => {
  test.each(Object.keys(referenceWorkflows))('server accepts reference workflow "%s"', async name => {
    const workflow = referenceWorkflows[name]()

    // Exercises getRequiredTaskDefNames() for real: the server rejects a
    // WfSpec referencing an unknown TaskDef.
    await registerRequiredTaskDefs(client, workflow, TASK_INPUT_VARS)

    const result = await client.putWfSpec(workflow.compileWorkflow())

    expect(result.id?.name).toBe(workflow.getName())
    expect(result.id?.majorVersion).toBeGreaterThanOrEqual(0)
  })

  test('a registered spec round-trips back from the server', async () => {
    const workflow = referenceWorkflows['conditionals']()
    await registerRequiredTaskDefs(client, workflow, TASK_INPUT_VARS)
    const spec = await client.putWfSpec(workflow.compileWorkflow())

    const fetched = await client.getWfSpec(spec.id!)
    expect(fetched.id).toEqual(spec.id)
    expect(Object.keys(fetched.threadSpecs)).toContain(fetched.entrypointThreadName)
    // The server preserved the graph we compiled, node names and all.
    const entrypoint = fetched.threadSpecs[fetched.entrypointThreadName]
    expect(Object.keys(entrypoint.nodes)).toEqual(
      expect.arrayContaining(Object.keys(workflow.compileWorkflow().threadSpecs['entrypoint'].nodes))
    )
  })

  test('the server rejects a WfSpec referencing an unknown TaskDef', async () => {
    // Guards the harness itself: if the server accepted anything at all, every
    // acceptance result above would be meaningless.
    const bogus = Workflow.newWorkflow('bogus-unknown-taskdef', thread => {
      thread.execute('definitely-not-registered-task-xyz')
    })
    await expect(client.putWfSpec(bogus.compileWorkflow())).rejects.toThrow(/nonexistent|does not exist|invalid/i)
  })
})
