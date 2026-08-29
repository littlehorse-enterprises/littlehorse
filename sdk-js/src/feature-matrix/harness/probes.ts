import { expect } from '@jest/globals'
import { Comparator } from '../../proto/type_definition'
import { VariableType } from '../../proto/common_enums'
import { VariableMutationType } from '../../proto/common_wfspec'
import { PutWfSpecRequest } from '../../proto/service'
import { Workflow, WorkflowThread, spawnedThreadsOf } from '../../wfsdk'
import { expectMatchesGolden, loadGolden } from './golden'

/**
 * Probe pairs: the per-feature evidence of the wfsdk matrix (see
 * proposals/sdk-js-parity/wfsdk.md, Design 2).
 *
 * Each probe is a minimal workflow exhibiting ONE feature, defined twice —
 * base (without it) and feature (with it) — and mirrored exactly by a Java
 * twin in golden/generator/.../ProbeGenerator.java, which real sdk-java
 * compiles into golden/probes/NAME.{base,feature}.json. A matrix entry proven
 * by a probe asserts three things, none of them an authored expectation:
 *
 *   1. our base compile is byte-identical to Java's base fixture;
 *   2. our feature compile is byte-identical to Java's feature fixture;
 *   3. the two fixtures differ — the toggle did something, so the probe
 *      cannot be vacuous (the failure mode that hid the spawnThread
 *      inputVars hole for weeks).
 *
 * Authoring rules: same workflow name in both variants; base is the feature's
 * nearest do-nothing neighbor; one feature per probe; input maps carry at most
 * one entry (Java's Map.of has no iteration order). Editing a probe means
 * editing BOTH twins and regenerating:
 *   ./gradlew :sdk-js-golden-generator:runProbes --args="$(pwd)/sdk-js/golden"
 */

type ProbeThread = (f: boolean) => (wf: WorkflowThread) => void

const PAIR_DEFS: Record<string, ProbeThread> = {
  // ---------------------------------------------------------------- declares
  'declare-str': f => wf => {
    if (f) wf.declareStr('v')
  },
  'declare-int': f => wf => {
    if (f) wf.declareInt('v')
  },
  'declare-double': f => wf => {
    if (f) wf.declareDouble('v')
  },
  'declare-bool': f => wf => {
    if (f) wf.declareBool('v')
  },
  'declare-bytes': f => wf => {
    if (f) wf.declareBytes('v')
  },
  'declare-timestamp': f => wf => {
    if (f) wf.declareTimestamp('v')
  },
  'declare-json-obj': f => wf => {
    if (f) wf.declareJsonObj('v')
  },
  'declare-json-arr': f => wf => {
    if (f) wf.declareJsonArr('v')
  },

  // ------------------------------------------------------- variable modifiers
  'var-with-default': f => wf => {
    const v = wf.declareInt('v')
    if (f) v.withDefault(42)
  },
  'var-required': f => wf => {
    const v = wf.declareStr('v')
    if (f) v.required()
  },
  'var-searchable': f => wf => {
    const v = wf.declareStr('v')
    if (f) v.searchable()
  },
  'var-searchable-on': f => wf => {
    const v = wf.declareJsonObj('v')
    if (f) v.searchableOn('$.customerId', VariableType.STR)
  },
  'var-masked': f => wf => {
    const v = wf.declareStr('v')
    if (f) v.masked()
  },
  'var-as-public': f => wf => {
    const v = wf.declareStr('v')
    if (f) v.asPublic()
  },
  'var-json-path': f => wf => {
    const v = wf.declareJsonObj('v')
    wf.execute('noop', f ? v.jsonPath('$.field') : v)
  },
  'var-assign': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(5)
  },

  // -------------------------------------------------------------------- tasks
  'execute-args': f => wf => {
    const name = wf.declareStr('name')
    if (f) {
      wf.execute('greet', name)
    } else {
      wf.execute('greet')
    }
  },
  'task-with-retries': f => wf => {
    const node = wf.execute('noop')
    if (f) node.withRetries(3)
  },

  // ------------------------------------------------------------- control flow
  // Shared by the condition() and doIf() entries: a condition only manifests
  // in output through a conditional consumer, so the nearest isolatable
  // feature is the conditioned branch itself.
  'do-if': f => wf => {
    const a = wf.declareInt('a')
    wf.execute('noop')
    if (f) {
      wf.doIf(wf.condition(a, Comparator.GREATER_THAN, 5), body => {
        body.execute('branch')
      })
    }
  },
  'do-else-if': f => wf => {
    const a = wf.declareInt('a')
    const ifStatement = wf.doIf(wf.condition(a, Comparator.GREATER_THAN, 10), body => {
      body.execute('big')
    })
    if (f) {
      ifStatement.doElseIf(wf.condition(a, Comparator.GREATER_THAN, 5), body => {
        body.execute('medium')
      })
    }
  },
  'do-else': f => wf => {
    const a = wf.declareInt('a')
    const ifStatement = wf.doIf(wf.condition(a, Comparator.GREATER_THAN, 10), body => {
      body.execute('big')
    })
    if (f) {
      ifStatement.doElse(body => {
        body.execute('small')
      })
    }
  },
  'do-if-else': f => wf => {
    const a = wf.declareInt('a')
    if (f) {
      wf.doIfElse(
        wf.condition(a, Comparator.GREATER_THAN, 5),
        body => body.execute('yes'),
        body => body.execute('no')
      )
    }
  },
  'do-while': f => wf => {
    const remaining = wf.declareInt('remaining')
    if (f) {
      wf.doWhile(wf.condition(remaining, Comparator.GREATER_THAN, 0), body => {
        body.execute('process-one')
      })
    }
  },
  fail: f => wf => {
    wf.execute('noop')
    if (f) wf.fail('business-problem', 'Something went wrong')
  },

  // ------------------------------------------------------------ sleep + waits
  'sleep-seconds': f => wf => {
    if (f) wf.sleepSeconds(30)
  },
  'sleep-until': f => wf => {
    const wakeAt = wf.declareTimestamp('wake-at')
    if (f) wf.sleepUntil(wakeAt)
  },
  'wait-for-condition': f => wf => {
    const ready = wf.declareBool('ready')
    if (f) wf.waitForCondition(wf.condition(ready, Comparator.EQUALS, true))
  },
  'throw-event': f => wf => {
    const payload = wf.declareStr('payload')
    if (f) wf.throwEvent('milestone', payload)
  },

  // -------------------------------------------------------------- expressions
  'expr-add': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(count.add(1))
  },
  'expr-subtract': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(count.subtract(1))
  },
  'expr-multiply': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(count.multiply(2))
  },
  'expr-divide': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(count.divide(2))
  },
  'expr-remove-index': f => wf => {
    const items = wf.declareJsonArr('items')
    wf.execute('noop')
    if (f) items.assign(items.removeIndex(0))
  },
  'expr-size': f => wf => {
    const items = wf.declareJsonArr('items')
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(items.size())
  },
  mutate: f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) wf.mutate(count, VariableMutationType.ADD, 5)
  },
  format: f => wf => {
    const name = wf.declareStr('name')
    const label = wf.declareStr('label')
    wf.execute('noop')
    if (f) label.assign(wf.format('Hello {0}', name))
  },

  // ---------------------------------------------------------- external events
  'wait-for-event': f => wf => {
    if (f) wf.waitForEvent('payment-received')
  },
  'external-event-timeout': f => wf => {
    const evt = wf.waitForEvent('payment-received')
    if (f) evt.timeout(3600)
  },
  'external-event-correlation': f => wf => {
    const orderId = wf.declareStr('order-id')
    const evt = wf.waitForEvent('payment-received')
    if (f) evt.withCorrelationId(orderId)
  },

  // ------------------------------------------------------------ child threads
  'spawn-thread-input-vars': f => wf => {
    const amount = wf.declareInt('amount')
    wf.spawnThread(
      child => {
        child.execute('child-task')
      },
      'child',
      f ? { budget: amount } : {}
    )
  },
  'spawn-thread-for-each': f => wf => {
    const items = wf.declareJsonArr('items')
    if (f) {
      wf.spawnThreadForEach(items, 'processor', child => {
        child.execute('process-one')
      })
    }
  },
  'wait-for-threads': f => wf => {
    const child = wf.spawnThread(
      c => {
        c.execute('child-task')
      },
      'child',
      {}
    )
    if (f) wf.waitForThreads(spawnedThreadsOf(child))
  },
  'spawned-threads-of': f => wf => {
    const first = wf.spawnThread(
      c => {
        c.execute('child-a-task')
      },
      'child-a',
      {}
    )
    const second = wf.spawnThread(
      c => {
        c.execute('child-b-task')
      },
      'child-b',
      {}
    )
    wf.waitForThreads(f ? spawnedThreadsOf(first, second) : spawnedThreadsOf(first))
  },

  // ---------------------------------------------------------- child workflows
  'run-wf-inputs': f => wf => {
    const orderId = wf.declareStr('order-id')
    wf.runWf('child-wf', f ? { 'order-id': orderId } : {})
  },
  'wait-for-child-wf': f => wf => {
    const child = wf.runWf('child-wf', {})
    if (f) wf.waitForChildWf(child)
  },

  // --------------------------------------------------------------- interrupts
  'interrupt-handler': f => wf => {
    wf.execute('main-step')
    if (f) {
      wf.registerInterruptHandler('cancel-requested', handler => {
        handler.execute('cancel')
      })
    }
  },

  // --------------------------------------------------------- failure handling
  'handle-error-any': f => wf => {
    const node = wf.execute('risky-task')
    if (f) {
      wf.handleError(node, null, handler => {
        handler.execute('cleanup')
      })
    }
  },
  'handle-exception-named': f => wf => {
    const node = wf.execute('flaky-task')
    if (f) {
      wf.handleException(node, 'out-of-stock', handler => {
        handler.execute('reorder')
      })
    }
  },
  'handle-any-failure': f => wf => {
    const node = wf.execute('fragile-task')
    if (f) {
      wf.handleAnyFailure(node, handler => {
        handler.execute('cleanup-any')
      })
    }
  },

  // --------------------------------------------------------------- user tasks
  'assign-user-task': f => wf => {
    if (f) wf.assignUserTask('approve-request', 'alice', 'approvers')
  },
  'user-task-notes': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) ut.withNotes('Please review')
  },
  'release-to-group-on-deadline': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) wf.releaseToGroupOnDeadline(ut, 300)
  },
  'schedule-reminder-task': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) wf.scheduleReminderTask(ut, 60, 'send-reminder')
  },
  'cancel-user-task-run-after': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) wf.cancelUserTaskRunAfter(ut, 86400)
  },
}

/** Capabilities that are the precondition of any output (no toggle exists). */
const SINGLE_DEFS: Record<string, () => Workflow> = {
  'workflow-minimal': () => Workflow.newWorkflow('probe-workflow-minimal', () => {}),
}

export const probePairs: Record<string, { base: () => Workflow; feature: () => Workflow }> = Object.fromEntries(
  Object.entries(PAIR_DEFS).map(([name, tf]) => [
    name,
    {
      base: () => Workflow.newWorkflow(`probe-${name}`, tf(false)),
      feature: () => Workflow.newWorkflow(`probe-${name}`, tf(true)),
    },
  ])
)

export const probeSingles = SINGLE_DEFS

/**
 * Which java-surface members each probe covers — machine-readable, like the
 * matrix citations. conformance/probes.test.ts diffs the union of these
 * against java-surface.json (minus the probe exemptions) and requires the
 * remainder to equal the checked-in backlog exactly, so probe coverage is
 * never left to memory. Keys must match the probe registry one-to-one.
 */
export const PROBE_COVERS: Record<string, string[]> = {
  'declare-str': ['WorkflowThread#declareStr'],
  'declare-int': ['WorkflowThread#declareInt'],
  'declare-double': ['WorkflowThread#declareDouble'],
  'declare-bool': ['WorkflowThread#declareBool'],
  'declare-bytes': ['WorkflowThread#declareBytes'],
  'declare-timestamp': ['WorkflowThread#declareTimestamp'],
  'declare-json-obj': ['WorkflowThread#declareJsonObj'],
  'declare-json-arr': ['WorkflowThread#declareJsonArr'],
  'var-with-default': ['WfRunVariable#withDefault'],
  'var-required': ['WfRunVariable#required'],
  'var-searchable': ['WfRunVariable#searchable'],
  'var-searchable-on': ['WfRunVariable#searchableOn'],
  'var-masked': ['WfRunVariable#masked'],
  'var-as-public': ['WfRunVariable#asPublic'],
  'var-json-path': ['WfRunVariable#jsonPath'],
  'var-assign': ['WfRunVariable#assign'],
  'execute-args': ['WorkflowThread#execute'],
  'task-with-retries': ['TaskNodeOutput#withRetries'],
  'do-if': ['WorkflowThread#doIf', 'WorkflowThread#condition'],
  'do-else-if': ['WorkflowIfStatement#doElseIf'],
  'do-else': ['WorkflowIfStatement#doElse'],
  'do-if-else': ['WorkflowThread#doIfElse'],
  'do-while': ['WorkflowThread#doWhile'],
  fail: ['WorkflowThread#fail'],
  'sleep-seconds': ['WorkflowThread#sleepSeconds'],
  'sleep-until': ['WorkflowThread#sleepUntil'],
  'wait-for-condition': ['WorkflowThread#waitForCondition'],
  'throw-event': ['WorkflowThread#throwEvent'],
  'expr-add': ['LHExpression#add'],
  'expr-subtract': ['LHExpression#subtract'],
  'expr-multiply': ['LHExpression#multiply'],
  'expr-divide': ['LHExpression#divide'],
  'expr-remove-index': ['LHExpression#removeIndex'],
  'expr-size': ['LHExpression#size'],
  mutate: ['WorkflowThread#mutate'],
  format: ['WorkflowThread#format'],
  'wait-for-event': ['WorkflowThread#waitForEvent'],
  'external-event-timeout': ['ExternalEventNodeOutput#timeout'],
  'external-event-correlation': ['ExternalEventNodeOutput#withCorrelationId'],
  'spawn-thread-input-vars': ['WorkflowThread#spawnThread'],
  'spawn-thread-for-each': ['WorkflowThread#spawnThreadForEach'],
  'wait-for-threads': ['WorkflowThread#waitForThreads'],
  'spawned-threads-of': ['SpawnedThreads#of'],
  'run-wf-inputs': ['WorkflowThread#runWf'],
  'wait-for-child-wf': ['WorkflowThread#waitForChildWf'],
  'interrupt-handler': ['WorkflowThread#registerInterruptHandler'],
  'handle-error-any': ['WorkflowThread#handleError'],
  'handle-exception-named': ['WorkflowThread#handleException'],
  'handle-any-failure': ['WorkflowThread#handleAnyFailure'],
  'assign-user-task': ['WorkflowThread#assignUserTask'],
  'user-task-notes': ['UserTaskOutput#withNotes'],
  'release-to-group-on-deadline': ['WorkflowThread#releaseToGroupOnDeadline'],
  'schedule-reminder-task': ['WorkflowThread#scheduleReminderTask'],
  'cancel-user-task-run-after': ['WorkflowThread#cancelUserTaskRunAfter'],
  'workflow-minimal': ['Workflow#newWorkflow', 'Workflow#compileWorkflow'],
}

/** True when the two named fixtures hold different protos. */
export function goldensDiffer(a: string, b: string): boolean {
  return !PutWfSpecRequest.equals(loadGolden(a), loadGolden(b))
}

/**
 * Proves a matrix entry by its probe pair: our compile matches Java's fixture
 * both with and without the feature, and the two fixtures differ.
 */
export function provenByProbe(name: string): void {
  const pair = probePairs[name]
  if (pair === undefined) {
    throw new Error(`No probe pair named '${name}' — add it to probes.ts AND ProbeGenerator.java, then regenerate.`)
  }
  expectMatchesGolden(pair.base().compileWorkflow(), `probes/${name}.base`)
  expectMatchesGolden(pair.feature().compileWorkflow(), `probes/${name}.feature`)
  expect(goldensDiffer(`probes/${name}.base`, `probes/${name}.feature`)).toBe(true)
}

/** Proves an entry whose capability cannot be toggled (see SINGLE_DEFS). */
export function provenBySingleProbe(name: string): void {
  const single = probeSingles[name]
  if (single === undefined) {
    throw new Error(`No single probe named '${name}' — add it to probes.ts AND ProbeGenerator.java, then regenerate.`)
  }
  expectMatchesGolden(single().compileWorkflow(), `probes/${name}`)
}
