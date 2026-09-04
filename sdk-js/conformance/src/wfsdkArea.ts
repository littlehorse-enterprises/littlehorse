/**
 * wfsdk cases, answered by the real builder (src/wfsdk/) — no knowledge of
 * the expected protos, only the calls each scenario.md describes. Bodies
 * typecheck against the builder's published types, so API drift fails the
 * build, not the grade. Bodies mirror WfsdkArea.java one to one.
 */
import { Workflow, spawnedThreadsOf, type ThreadFunc, type WfRunVariable } from '../../dist/wfsdk'
import { PutWfSpecRequest, AllowedUpdateType } from '../../dist/proto/service'
import { Comparator } from '../../dist/proto/type_definition'
import { VariableType, LHErrorType } from '../../dist/proto/common_enums'
import { VariableMutationType, ExponentialBackoffRetryPolicy } from '../../dist/proto/common_wfspec'
import { WfRunVariableAccessLevel, ThreadRetentionPolicy, WorkflowRetentionPolicy } from '../../dist/proto/wf_spec'

type Variant = 'base' | 'feature'
type PairBody = (f: boolean) => ThreadFunc

const backoff = () => ExponentialBackoffRetryPolicy.create({ baseIntervalMs: 1000, multiplier: 2, maxDelayMs: '60000' })
const threadRetention = () =>
  ThreadRetentionPolicy.create({
    threadGcPolicy: { oneofKind: 'secondsAfterThreadTermination', secondsAfterThreadTermination: '600' },
  })

const SINGLES: Record<string, () => Workflow> = {
  'workflow-minimal': () => Workflow.newWorkflow('probe-workflow-minimal', () => {}),
}

const PAIRS: Record<string, PairBody> = {
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
  'declare-array': f => wf => {
    if (f) wf.declareArray('v', VariableType.STR)
  },
  'declare-map': f => wf => {
    if (f) wf.declareMap('v', VariableType.STR, VariableType.STR)
  },
  'declare-struct': f => wf => {
    if (f) wf.declareStruct('v', 'customer')
  },
  'add-variable': f => wf => {
    // addVariable is private in JS; declareStr is its public wrapper and
    // compiles to the identical VariableDef.
    if (f) wf.declareStr('v')
  },

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
  'var-with-access-level': f => wf => {
    const v = wf.declareStr('v')
    if (f) v.withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR)
  },
  'var-as-inherited': f => wf => {
    const v = wf.declareStr('v')
    if (f) v.asInherited()
  },
  'var-json-path': f => wf => {
    const v = wf.declareJsonObj('v')
    wf.execute('noop', f ? v.jsonPath('$.field') : v)
  },
  'var-get-field': f => wf => {
    const v = wf.declareJsonObj('v')
    wf.execute('noop', f ? v.get('field') : v)
  },
  'var-assign': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(5)
  },

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
  'task-timeout': f => wf => {
    const node = wf.execute('noop')
    if (f) node.timeout(30)
  },
  'task-exponential-backoff': f => wf => {
    const node = wf.execute('noop')
    if (f) node.withExponentialBackoff(backoff())
  },
  'node-output-json-path': f => wf => {
    const v = wf.declareJsonObj('v')
    const node = wf.execute('noop')
    if (f) v.assign(node.jsonPath('$.total'))
  },
  'node-output-get': f => wf => {
    const v = wf.declareJsonObj('v')
    const node = wf.execute('noop')
    if (f) v.assign(node.get('total'))
  },

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
  'wt-complete': f => wf => {
    wf.execute('noop')
    if (f) wf.complete('done')
  },
  'wt-retention-policy': f => wf => {
    wf.execute('noop')
    if (f) wf.withRetentionPolicy(threadRetention())
  },

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
  'expr-pow': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(count.pow(2))
  },
  'expr-extend': f => wf => {
    const items = wf.declareJsonArr('items')
    wf.execute('noop')
    if (f) items.assign(items.extend('x'))
  },
  'expr-remove-index': f => wf => {
    const items = wf.declareJsonArr('items')
    wf.execute('noop')
    if (f) items.assign(items.removeIndex(0))
  },
  'expr-remove-if-present': f => wf => {
    const items = wf.declareJsonArr('items')
    wf.execute('noop')
    if (f) items.assign(items.removeIfPresent('x'))
  },
  'expr-remove-key': f => wf => {
    const obj = wf.declareJsonObj('obj')
    wf.execute('noop')
    if (f) obj.assign(obj.removeKey('k'))
  },
  'expr-size': f => wf => {
    const items = wf.declareJsonArr('items')
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(items.size())
  },

  'wt-add': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(wf.add(count, 1))
  },
  'wt-subtract': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(wf.subtract(count, 1))
  },
  'wt-multiply': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(wf.multiply(count, 2))
  },
  'wt-divide': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(wf.divide(count, 2))
  },
  'wt-pow': f => wf => {
    const count = wf.declareInt('count')
    wf.execute('noop')
    if (f) count.assign(wf.pow(count, 2))
  },
  'wt-extend': f => wf => {
    const items = wf.declareJsonArr('items')
    wf.execute('noop')
    if (f) items.assign(wf.extend(items, 'x'))
  },
  'wt-remove-if-present': f => wf => {
    const items = wf.declareJsonArr('items')
    wf.execute('noop')
    if (f) items.assign(wf.removeIfPresent(items, 'x'))
  },
  'wt-remove-index': f => wf => {
    const items = wf.declareJsonArr('items')
    wf.execute('noop')
    if (f) items.assign(wf.removeIndex(items, 0))
  },
  'wt-remove-key': f => wf => {
    const obj = wf.declareJsonObj('obj')
    wf.execute('noop')
    if (f) obj.assign(wf.removeKey(obj, 'k'))
  },

  'expr-is-equal-to': f => wf => {
    const a = wf.declareInt('a')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isEqualTo(5))
  },
  'expr-is-not-equal-to': f => wf => {
    const a = wf.declareInt('a')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isNotEqualTo(5))
  },
  'expr-is-greater-than': f => wf => {
    const a = wf.declareInt('a')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isGreaterThan(5))
  },
  'expr-is-less-than': f => wf => {
    const a = wf.declareInt('a')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isLessThan(5))
  },
  'var-is-greater-than-eq': f => wf => {
    const a = wf.declareInt('a')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isGreaterThanEq(5))
  },
  'var-is-less-than-eq': f => wf => {
    const a = wf.declareInt('a')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isLessThanEq(5))
  },
  'expr-is-in': f => wf => {
    const a = wf.declareStr('a')
    const allowed = wf.declareJsonArr('allowed')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isIn(allowed))
  },
  'expr-is-not-in': f => wf => {
    const a = wf.declareStr('a')
    const blocked = wf.declareJsonArr('blocked')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isNotIn(blocked))
  },
  'expr-does-contain': f => wf => {
    const items = wf.declareJsonArr('items')
    wf.execute('noop')
    if (f) wf.waitForCondition(items.doesContain('x'))
  },
  'expr-does-not-contain': f => wf => {
    const items = wf.declareJsonArr('items')
    wf.execute('noop')
    if (f) wf.waitForCondition(items.doesNotContain('x'))
  },
  'expr-and': f => wf => {
    const a = wf.declareInt('a')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isGreaterThan(0).and(a.isLessThan(10)))
  },
  'expr-or': f => wf => {
    const a = wf.declareInt('a')
    wf.execute('noop')
    if (f) wf.waitForCondition(a.isLessThan(0).or(a.isGreaterThan(10)))
  },

  'expr-cast-to': f => wf => {
    const v = wf.declareInt('v')
    const s = wf.declareStr('s')
    wf.execute('noop')
    if (f) s.assign(v.castTo(VariableType.STR))
  },
  'expr-cast-to-int': f => wf => {
    const s = wf.declareStr('s')
    const v = wf.declareInt('v')
    wf.execute('noop')
    if (f) v.assign(s.castToInt())
  },
  'expr-cast-to-double': f => wf => {
    const s = wf.declareStr('s')
    const v = wf.declareDouble('v')
    wf.execute('noop')
    if (f) v.assign(s.castToDouble())
  },
  'expr-cast-to-str': f => wf => {
    const n = wf.declareInt('n')
    const s = wf.declareStr('s')
    wf.execute('noop')
    if (f) s.assign(n.castToStr())
  },
  'expr-cast-to-bool': f => wf => {
    const s = wf.declareStr('s')
    const b = wf.declareBool('b')
    wf.execute('noop')
    if (f) b.assign(s.castToBool())
  },
  'expr-cast-to-bytes': f => wf => {
    const s = wf.declareStr('s')
    const b = wf.declareBytes('b')
    wf.execute('noop')
    if (f) b.assign(s.castToBytes())
  },
  'expr-cast-to-wf-run-id': f => wf => {
    const s = wf.declareStr('s')
    const w = wf.declareStr('w')
    wf.execute('noop')
    if (f) w.assign(s.castToWfRunId())
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

  'build-struct': f => wf => {
    const v = wf.declareStruct('v', 'customer')
    wf.execute('noop')
    if (f) v.assign(wf.buildStruct('customer').put('name', 'n'))
  },
  'build-inline-struct': f => wf => {
    const v = wf.declareStruct('v', 'customer')
    wf.execute('noop')
    if (f) v.assign(wf.buildStruct('customer').put('nested', wf.buildInlineStruct().put('k', 'x')))
  },

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
  'wait-for-any-of': f => wf => {
    const child = wf.spawnThread(
      c => {
        c.execute('child-task')
      },
      'child',
      {}
    )
    if (f) wf.waitForAnyOf(spawnedThreadsOf(child))
  },
  'wait-for-first-of': f => wf => {
    const child = wf.spawnThread(
      c => {
        c.execute('child-task')
      },
      'child',
      {}
    )
    if (f) wf.waitForFirstOf(spawnedThreadsOf(child))
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
  'spawned-thread-number': f => wf => {
    const num = wf.declareInt('num')
    const child = wf.spawnThread(
      c => {
        c.execute('child-task')
      },
      'child',
      {}
    )
    if (f) num.assign(child.getThreadNumberVariable())
  },
  'wait-threads-handle-exception-on-child': f => wf => {
    const child = wf.spawnThread(
      c => {
        c.execute('child-task')
      },
      'child',
      {}
    )
    const wait = wf.waitForThreads(spawnedThreadsOf(child))
    if (f) {
      wait.handleExceptionOnChild('out-of-stock', handler => {
        handler.execute('reorder')
      })
    }
  },
  'wait-threads-handle-error-on-child': f => wf => {
    const child = wf.spawnThread(
      c => {
        c.execute('child-task')
      },
      'child',
      {}
    )
    const wait = wf.waitForThreads(spawnedThreadsOf(child))
    if (f) {
      wait.handleErrorOnChild(LHErrorType.CHILD_FAILURE, handler => {
        handler.execute('cleanup')
      })
    }
  },
  'wait-threads-handle-any-failure-on-child': f => wf => {
    const child = wf.spawnThread(
      c => {
        c.execute('child-task')
      },
      'child',
      {}
    )
    const wait = wf.waitForThreads(spawnedThreadsOf(child))
    if (f) {
      wait.handleAnyFailureOnChild(handler => {
        handler.execute('cleanup-any')
      })
    }
  },

  'run-wf-inputs': f => wf => {
    const orderId = wf.declareStr('order-id')
    wf.runWf('child-wf', f ? { 'order-id': orderId } : {})
  },
  'wait-for-child-wf': f => wf => {
    const child = wf.runWf('child-wf', {})
    if (f) wf.waitForChildWf(child)
  },

  'interrupt-handler': f => wf => {
    wf.execute('main-step')
    if (f) {
      wf.registerInterruptHandler('cancel-requested', handler => {
        handler.execute('cancel')
      })
    }
  },

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

  'assign-user-task': f => wf => {
    if (f) wf.assignUserTask('approve-request', 'alice', 'approvers')
  },
  'user-task-notes': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) ut.withNotes('Please review')
  },
  'user-task-on-cancellation-exception': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) ut.withOnCancellationException('cancelled')
  },
  'release-to-group-on-deadline': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) wf.releaseToGroupOnDeadline(ut, 300)
  },
  'reassign-user-task': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) wf.reassignUserTask(ut, 'bob', 'approvers', 300)
  },
  'schedule-reminder-task': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) wf.scheduleReminderTask(ut, 60, 'send-reminder')
  },
  'schedule-reminder-on-assignment': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) wf.scheduleReminderTaskOnAssignment(ut, 60, 'send-reminder')
  },
  'cancel-user-task-run-after': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) wf.cancelUserTaskRunAfter(ut, 86400)
  },
  'cancel-user-task-after-assignment': f => wf => {
    const ut = wf.assignUserTask('approve-request', 'alice', 'approvers')
    if (f) wf.cancelUserTaskRunAfterAssignment(ut, 86400)
  },
}

const WF_PAIRS: Record<string, (f: boolean) => Workflow> = {
  'wf-update-type': f => {
    const w = Workflow.newWorkflow('probe-wf-update-type', wf => {
      wf.execute('noop')
    })
    if (f) w.withUpdateType(AllowedUpdateType.NO_UPDATES)
    return w
  },
  'wf-set-parent': f => {
    const w = Workflow.newWorkflow('probe-wf-set-parent', wf => {
      wf.execute('noop')
    })
    if (f) w.setParent('parent-wf')
    return w
  },
  'wf-retention-policy': f => {
    const w = Workflow.newWorkflow('probe-wf-retention-policy', wf => {
      wf.execute('noop')
    })
    if (f) {
      w.withRetentionPolicy(
        WorkflowRetentionPolicy.create({
          wfGcPolicy: { oneofKind: 'secondsAfterWfTermination', secondsAfterWfTermination: '3600' },
        })
      )
    }
    return w
  },
  'wf-default-thread-retention': f => {
    const w = Workflow.newWorkflow('probe-wf-default-thread-retention', wf => {
      wf.execute('noop')
    })
    if (f) w.withDefaultThreadRetentionPolicy(threadRetention())
    return w
  },
  'wf-default-task-timeout': f => {
    const w = Workflow.newWorkflow('probe-wf-default-task-timeout', wf => {
      wf.execute('noop')
    })
    if (f) w.setDefaultTaskTimeout(45)
    return w
  },
  'wf-default-task-retries': f => {
    const w = Workflow.newWorkflow('probe-wf-default-task-retries', wf => {
      wf.execute('noop')
    })
    if (f) w.setDefaultTaskRetries(2)
    return w
  },
  'wf-default-task-backoff': f => {
    const w = Workflow.newWorkflow('probe-wf-default-task-backoff', wf => {
      wf.execute('noop')
    })
    if (f) w.setDefaultTaskExponentialBackoffPolicy(backoff())
    return w
  },
}

export function caseIds(): string[] {
  return [...Object.keys(SINGLES), ...Object.keys(PAIRS), ...Object.keys(WF_PAIRS)]
}

function buildWorkflow(caseId: string, variant: string): Workflow {
  if (SINGLES[caseId]) {
    if (variant !== 'feature') throw new Error(`case ${caseId} is single-variant; only feature exists`)
    return SINGLES[caseId]()
  }
  if (variant !== 'base' && variant !== 'feature') throw new Error(`variant must be base|feature: ${variant}`)
  const withFeature = variant === 'feature'
  if (PAIRS[caseId]) return Workflow.newWorkflow(`probe-${caseId}`, PAIRS[caseId](withFeature))
  if (WF_PAIRS[caseId]) return WF_PAIRS[caseId](withFeature)
  throw new Error(`unknown case: ${caseId}`)
}

export function compile(caseId: string, variant: string): string {
  // R11: proto3 JSON with defaults emitted; comparison is semantic.
  const json = PutWfSpecRequest.toJson(buildWorkflow(caseId, variant).compileWorkflow(), {
    emitDefaultValues: true,
  })
  return JSON.stringify(json, null, 2)
}
