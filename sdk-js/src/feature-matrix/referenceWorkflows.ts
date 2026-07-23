import { Comparator } from '../proto/type_definition'
import { VariableMutationType } from '../proto/common_wfspec'
import { VariableType } from '../proto/common_enums'
import { Workflow, spawnedThreadsOf } from '../wfsdk'

/**
 * TypeScript equivalents of the Java reference workflows in
 * sdk-js/golden/generator/.../GoldenGenerator.java. Each must compile to the
 * exact same PutWfSpecRequest as its Java twin (asserted by
 * wfsdk-golden.test.ts and the per-feature matrix tests). When editing one,
 * edit BOTH sides and regenerate the goldens:
 *   ./gradlew :sdk-js-golden-generator:run --args="$(pwd)/sdk-js/golden"
 */
export const referenceWorkflows: Record<string, () => Workflow> = {
  basic: () =>
    Workflow.newWorkflow('golden-basic', thread => {
      const name = thread.declareStr('name').required()
      thread.execute('greet', name)
    }),

  variables: () =>
    Workflow.newWorkflow('golden-variables', thread => {
      thread.declareStr('my-str').required()
      thread.declareInt('my-int').withDefault(42)
      thread.declareDouble('my-double')
      thread.declareBool('my-bool')
      thread.declareBytes('my-bytes')
      thread.declareTimestamp('my-timestamp')
      thread.declareJsonObj('my-json-obj').searchableOn('$.customerId', VariableType.STR)
      thread.declareJsonArr('my-json-arr')
      thread.declareStr('my-searchable').searchable()
      thread.declareStr('my-masked').masked()
      thread.declareStr('my-public').asPublic()
      thread.execute('noop')
    }),

  conditionals: () =>
    Workflow.newWorkflow('golden-conditionals', thread => {
      const amount = thread.declareInt('amount')
      const customer = thread.declareJsonObj('customer')

      thread
        .doIf(thread.condition(amount, Comparator.GREATER_THAN, 100), body => {
          body.execute('large-order')
        })
        .doElseIf(thread.condition(amount, Comparator.GREATER_THAN, 10), body => {
          body.execute('medium-order')
        })
        .doElse(body => {
          body.execute('small-order')
        })

      thread.doIfElse(
        customer.jsonPath('$.isVip').isEqualTo(true),
        body => body.execute('vip-flow'),
        body => body.execute('regular-flow')
      )
    }),

  expressions: () =>
    Workflow.newWorkflow('golden-expressions', thread => {
      const count = thread.declareInt('count')
      const total = thread.declareDouble('total')
      const items = thread.declareJsonArr('items')
      const label = thread.declareStr('label')

      count.assign(count.add(1))
      total.assign(count.multiply(2).divide(4).subtract(1))
      items.assign(items.removeIndex(0))
      label.assign(thread.format('count is {0} of {1}', count, total))
      count.assign(items.size())
      total.assign(count.castToDouble())
      thread.mutate(count, VariableMutationType.ADD, 5)
      thread.execute('noop')
    }),

  'while-loop': () =>
    Workflow.newWorkflow('golden-while-loop', thread => {
      const remaining = thread.declareInt('remaining')
      thread.doWhile(thread.condition(remaining, Comparator.GREATER_THAN, 0), body => {
        body.execute('process-one')
        body.mutate(remaining, VariableMutationType.SUBTRACT, 1)
      })
    }),

  'external-events': () =>
    Workflow.newWorkflow('golden-external-events', thread => {
      const orderId = thread.declareStr('order-id')
      const payment = thread.declareJsonObj('payment')

      payment.assign(thread.waitForEvent('payment-received').timeout(3600).withCorrelationId(orderId))
      thread.execute('fulfill', orderId, payment)
    }),

  'child-threads': () =>
    Workflow.newWorkflow('golden-child-threads', thread => {
      const orders = thread.declareJsonArr('orders')

      const notifier = thread.spawnThread(
        child => {
          child.execute('notify')
        },
        'notifier',
        {}
      )
      const auditor = thread.spawnThread(
        child => {
          child.execute('audit')
        },
        'auditor',
        {}
      )
      thread.waitForThreads(spawnedThreadsOf(notifier, auditor))

      const processors = thread.spawnThreadForEach(orders, 'processor', child => {
        child.execute('process-order')
      })
      thread.waitForThreads(processors)
    }),

  'failure-handling': () =>
    Workflow.newWorkflow('golden-failure-handling', thread => {
      const risky = thread.execute('risky-task')
      thread.handleError(risky, null, handler => {
        handler.execute('cleanup-error')
      })

      const flaky = thread.execute('flaky-task').withRetries(3)
      thread.handleException(flaky, 'out-of-stock', handler => {
        handler.execute('reorder')
      })

      const fragile = thread.execute('fragile-task')
      thread.handleAnyFailure(fragile, handler => {
        handler.execute('cleanup-any')
        handler.fail('unrecoverable', 'Could not recover from failure')
      })
    }),

  'user-tasks': () =>
    Workflow.newWorkflow('golden-user-tasks', thread => {
      const approver = thread.declareStr('approver')

      const approval = thread
        .assignUserTask('approve-request', approver, 'approvers')
        .withNotes(thread.format('Approval needed from {0}', approver))
      thread.releaseToGroupOnDeadline(approval, 300)
      thread.scheduleReminderTask(approval, 60, 'send-reminder')
      thread.cancelUserTaskRunAfter(approval, 86400)
      thread.execute('finalize')
    }),

  'sleep-and-events': () =>
    Workflow.newWorkflow('golden-sleep-and-events', thread => {
      const ready = thread.declareBool('ready')
      const wakeAt = thread.declareTimestamp('wake-at')

      thread.sleepSeconds(30)
      thread.sleepUntil(wakeAt)
      thread.waitForCondition(thread.condition(ready, Comparator.EQUALS, true))
      thread.throwEvent('milestone-reached', ready)
      thread.complete()
    }),

  'child-workflow': () =>
    Workflow.newWorkflow('golden-child-workflow', thread => {
      const orderId = thread.declareStr('order-id')
      const shipping = thread.runWf('shipping-wf', { 'order-id': orderId })
      thread.waitForChildWf(shipping)
    }),

  interrupts: () =>
    Workflow.newWorkflow('golden-interrupts', thread => {
      thread.registerInterruptHandler('cancel-requested', handler => {
        handler.execute('cancel-order')
      })
      thread.execute('long-running-step')
    }),
}
