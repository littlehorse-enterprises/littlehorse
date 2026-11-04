/**
 * registrations cases: the side-registration protos and required-names sets
 * a workflow produces besides its WfSpec, answered by the real builder.
 * Bodies mirror RegistrationsArea.java one to one; document shape: rules.md G1.
 */
import { z } from 'zod'
import { Workflow, type ThreadFunc } from '../../dist/wfsdk'
import { PutExternalEventDefRequest, PutWorkflowEventDefRequest } from '../../dist/proto/service'
import { CorrelatedEventConfig } from '../../dist/proto/external_event'

type PairBody = (f: boolean) => ThreadFunc

const PAIRS: Record<string, PairBody> = {
  'reg-external-event': f => wf => {
    const evt = wf.waitForEvent('payment-received')
    if (f) evt.registeredAs(z.string())
  },
  'reg-correlated-event-config': f => wf => {
    const evt = wf.waitForEvent('payment-received').registeredAs(z.string())
    if (f) {
      evt.withCorrelatedEventConfig(
        CorrelatedEventConfig.create({ ttlSeconds: '3600', deleteAfterFirstCorrelation: true })
      )
    }
  },
  'reg-workflow-event': f => wf => {
    const payload = wf.declareStr('payload')
    const evt = wf.throwEvent('milestone', payload)
    if (f) evt.registeredAs(z.string())
  },
  'reg-interrupt-event-type': f => wf => {
    wf.execute('main-step')
    const handler = wf.registerInterruptHandler('cancel-requested', h => {
      h.execute('cancel')
    })
    if (f) handler.withEventType(z.string())
  },
  'req-task-def-names': f => wf => {
    wf.declareStr('v')
    if (f) wf.execute('noop')
  },
  'req-external-event-def-names': f => wf => {
    wf.execute('noop')
    if (f) wf.waitForEvent('payment-received')
  },
  'req-child-wf-spec-names': f => wf => {
    wf.execute('noop')
    if (f) wf.runWf('child-wf', {})
  },
  'req-workflow-event-def-names': f => wf => {
    const payload = wf.declareStr('payload')
    wf.execute('noop')
    if (f) wf.throwEvent('milestone', payload)
  },
}

export function caseIds(): string[] {
  return Object.keys(PAIRS)
}

export function answer(caseId: string, variant: string): string {
  const body = PAIRS[caseId]
  if (!body) throw new Error(`unknown case: ${caseId}`)
  if (variant !== 'base' && variant !== 'feature') throw new Error(`variant must be base|feature: ${variant}`)
  const w = Workflow.newWorkflow(`probe-${caseId}`, body(variant === 'feature'))
  // registrations hydrate during compilation (G2)
  w.compileWorkflow()

  const byName = (a: { name: string }, b: { name: string }) => a.name.localeCompare(b.name)
  const doc = {
    externalEventDefs: [...w.getExternalEventDefsToRegister()]
      .sort(byName)
      .map(d => PutExternalEventDefRequest.toJson(d, { emitDefaultValues: true })),
    workflowEventDefs: [...w.getWorkflowEventDefsToRegister()]
      .sort(byName)
      .map(d => PutWorkflowEventDefRequest.toJson(d, { emitDefaultValues: true })),
    requiredTaskDefNames: [...w.getRequiredTaskDefNames()].sort(),
    requiredExternalEventDefNames: [...w.getRequiredExternalEventDefNames()].sort(),
    requiredChildWfSpecNames: [...w.getRequiredChildWfSpecNames()].sort(),
    requiredWorkflowEventDefNames: [...w.getRequiredWorkflowEventDefNames()].sort(),
  }
  return JSON.stringify(doc, null, 2)
}
