import { describe, expect, it } from '@jest/globals'
import { z } from 'zod'
import { AllowedUpdateType } from '../proto/service'
import { VariableType } from '../proto/common_enums'
import { WfRunVariableAccessLevel, WorkflowRetentionPolicy } from '../proto/wf_spec'
import { userTaskSchema } from '../usertask'
import { Workflow } from './Workflow'

/**
 * The options style must compile to byte-identical protos as the fluent
 * style: every option key delegates to its chain method internally, and
 * these tests pin that equivalence for each key.
 */
describe('options objects compile identically to fluent chains', () => {
  it('variable declaration options', () => {
    const fluent = Workflow.newWorkflow('wf', thread => {
      thread
        .declareStr('who')
        .required()
        .masked()
        .withDefault('anon')
        .withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR)
      thread.declareJsonObj('doc').searchable().searchableOn('$.city', VariableType.STR).withDefault({ city: 'x' })
    })
    const options = Workflow.newWorkflow('wf', thread => {
      thread.declareStr('who', {
        required: true,
        masked: true,
        defaultValue: 'anon',
        accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
      })
      thread.declareJsonObj('doc', {
        searchable: true,
        searchableOn: [{ fieldPath: '$.city', fieldType: VariableType.STR }],
        defaultValue: { city: 'x' },
      })
    })
    expect(options.compileWorkflow()).toEqual(fluent.compileWorkflow())
  })

  it('declareStruct version as an option', () => {
    const positional = Workflow.newWorkflow('wf', thread => {
      thread.declareStruct('c', 'customer', 2)
    })
    const options = Workflow.newWorkflow('wf', thread => {
      thread.declareStruct('c', 'customer', { version: 2 })
    })
    expect(options.compileWorkflow()).toEqual(positional.compileWorkflow())
  })

  it('execute options', () => {
    const backoff = { baseIntervalMs: 100, maxDelayMs: 5000, multiplier: 2 }
    const fluent = Workflow.newWorkflow('wf', thread => {
      const x = thread.declareInt('x')
      thread.execute('work', x).withRetries(3).withExponentialBackoff(backoff).timeout(30)
    })
    const options = Workflow.newWorkflow('wf', thread => {
      const x = thread.declareInt('x')
      thread.execute('work', [x], { retries: 3, exponentialBackoff: backoff, timeoutSeconds: 30 })
    })
    expect(options.compileWorkflow()).toEqual(fluent.compileWorkflow())
  })

  it('a lone array argument is still one variadic task input', () => {
    const wf = Workflow.newWorkflow('wf', thread => {
      thread.execute('work', [1, 2, 3])
    })
    const spec = wf.compileWorkflow()
    const nodes = spec.threadSpecs['entrypoint'].nodes
    const taskNode = Object.values(nodes).find(n => n.node.oneofKind === 'task')!.node
    expect(taskNode.oneofKind === 'task' && taskNode.task.variables).toHaveLength(1)
  })

  it('waitForEvent options', () => {
    const fluent = Workflow.newWorkflow('wf', thread => {
      const email = thread.declareStr('email')
      thread.waitForEvent('paid').timeout(300).withCorrelationId(email, true).registeredAs(z.boolean())
    })
    const options = Workflow.newWorkflow('wf', thread => {
      const email = thread.declareStr('email')
      thread.waitForEvent('paid', {
        timeoutSeconds: 300,
        correlationId: email,
        maskCorrelationId: true,
        payloadSchema: z.boolean(),
      })
    })
    expect(options.compileWorkflow()).toEqual(fluent.compileWorkflow())
    expect(options.getExternalEventDefsToRegister()).toEqual(fluent.getExternalEventDefsToRegister())
  })

  it('throwEvent options', () => {
    const fluent = Workflow.newWorkflow('wf', thread => {
      thread.throwEvent('milestone', 'done').registeredAs(z.string())
    })
    const options = Workflow.newWorkflow('wf', thread => {
      thread.throwEvent('milestone', 'done', { payloadSchema: z.string() })
    })
    expect(options.compileWorkflow()).toEqual(fluent.compileWorkflow())
    expect(options.getWorkflowEventDefsToRegister()).toEqual(fluent.getWorkflowEventDefsToRegister())
  })

  it('registerInterruptHandler options', () => {
    const fluent = Workflow.newWorkflow('wf', thread => {
      thread.declareStr('x')
      thread.registerInterruptHandler('stop', handler => handler.execute('cleanup')).withEventType(z.string())
    })
    const options = Workflow.newWorkflow('wf', thread => {
      thread.declareStr('x')
      thread.registerInterruptHandler('stop', handler => handler.execute('cleanup'), { payloadSchema: z.string() })
    })
    expect(options.compileWorkflow()).toEqual(fluent.compileWorkflow())
    expect(options.getExternalEventDefsToRegister()).toEqual(fluent.getExternalEventDefsToRegister())
  })

  it('newWorkflow options', () => {
    const retention = WorkflowRetentionPolicy.create()
    const fn = (thread: Parameters<Parameters<typeof Workflow.newWorkflow>[1]>[0]) => {
      thread.execute('work')
    }
    const fluent = Workflow.newWorkflow('wf', fn)
    fluent.withUpdateType(AllowedUpdateType.NO_UPDATES)
    fluent.withRetentionPolicy(retention)
    fluent.setDefaultTaskTimeout(60)
    fluent.setDefaultTaskRetries(2)
    fluent.setParent('parent-wf')
    const options = Workflow.newWorkflow('wf', fn, {
      updateType: AllowedUpdateType.NO_UPDATES,
      retentionPolicy: retention,
      defaultTaskTimeout: 60,
      defaultTaskRetries: 2,
      parentWfSpecName: 'parent-wf',
    })
    expect(options.compileWorkflow()).toEqual(fluent.compileWorkflow())
  })

  it('assignUserTask options', () => {
    const fluent = Workflow.newWorkflow('wf', thread => {
      const group = thread.declareStr('group')
      const ut = thread.assignUserTask('approve', null, group)
      ut.withNotes('please review').withOnCancellationException('cancelled')
    })
    const options = Workflow.newWorkflow('wf', thread => {
      const group = thread.declareStr('group')
      thread.assignUserTask('approve', {
        userGroup: group,
        notes: 'please review',
        onCancellationException: 'cancelled',
      })
    })
    expect(options.compileWorkflow()).toEqual(fluent.compileWorkflow())
  })

  it('userTaskSchema description option', () => {
    const positional = userTaskSchema('approve', { ok: z.boolean() }, 'an approval')
    const options = userTaskSchema('approve', { ok: z.boolean() }, { description: 'an approval' })
    expect(options.compile()).toEqual(positional.compile())
  })
})
