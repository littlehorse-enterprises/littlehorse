/**
 * Probe-coverage exemptions: java-surface members that will NEVER need a
 * probe pair, each with a written reason (see proposals/sdk-js-parity/
 * wfsdk.md, Design 2 — "the coverage guarantee").
 *
 * A probe proves a feature by the delta it makes to the compiled
 * PutWfSpecRequest. A member belongs here only when no such delta exists —
 * queries, registration RPCs, serialization wrappers, type plumbing. A member
 * that DOES change compiled output but has no probe yet belongs in
 * probeBacklog.ts (visible debt), never here.
 *
 * conformance/probes.test.ts fails on: a member neither covered, exempted,
 * nor backlogged; an exemption for a member that no longer exists; and an
 * exemption for a member a probe now covers (stale excuse).
 */

export type ProbeExemptionKind =
  /** No effect on the compiled proto — nothing for a fixture delta to show. */
  | 'NO_COMPILED_OUTPUT'
  /** Evidence is a validator throw, not a fixture delta. */
  | 'NEGATIVE_CONSTRAINT'

export interface ProbeExemption {
  key: string
  kind: ProbeExemptionKind
  reason: string
}

export const PROBE_EXEMPTIONS: ProbeExemption[] = [
  // ------------------------------------------------- functional-interface SAMs
  {
    key: 'IfElseBody#body',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'the SAM of a callback type; user code implements it, nothing compiles from the method itself',
  },
  {
    key: 'ThreadFunc#threadFunction',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'the SAM of a callback type; user code implements it, nothing compiles from the method itself',
  },
  {
    key: 'SpawnedThreads#buildNode',
    kind: 'NO_COMPILED_OUTPUT',
    reason:
      'compiler plumbing invoked internally by waitForThreads; its output is proven by the wait-for-threads probe',
  },

  // ------------------------------------- event-def registration (not the spec)
  {
    key: 'ExternalEventNodeOutput#registeredAs',
    kind: 'NO_COMPILED_OUTPUT',
    reason:
      'shapes the PutExternalEventDefRequest side-registration, not the WfSpec; proven by the registerWfSpec matrix test against the fake server',
  },
  {
    key: 'ExternalEventNodeOutput#withCorrelatedEventConfig',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'shapes the PutExternalEventDefRequest side-registration, not the WfSpec; proven by its matrix test',
  },
  {
    key: 'ThrowEventNodeOutput#registeredAs',
    kind: 'NO_COMPILED_OUTPUT',
    reason:
      'shapes the PutWorkflowEventDefRequest side-registration, not the WfSpec; proven by the registerWfSpec matrix test against the fake server',
  },

  // ------------------------------------------ Workflow queries / RPCs / output
  {
    key: 'Workflow#getName',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query over the builder; no compiled effect',
  },
  {
    key: 'Workflow#getDefaultTaskTimeout',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query over the builder; the setter that HAS compiled effect is backlogged',
  },
  {
    key: 'Workflow#getPlaceholderValues',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query over the builder; placeholder RESOLUTION is a visible matrix todo',
  },
  {
    key: 'Workflow#getTypeAdapterRegistry',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query over the config wiring; adapters affect values at runtime, not the compiled spec',
  },
  {
    key: 'Workflow#getRequiredTaskDefNames',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query derived from compilation; proven by its own matrix test',
  },
  {
    key: 'Workflow#getRequiredExternalEventDefNames',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query derived from compilation; proven by its own matrix test',
  },
  {
    key: 'Workflow#getRequiredChildWfSpecNames',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query derived from compilation; proven by its own matrix test',
  },
  {
    key: 'Workflow#getRequiredWorkflowEventDefNames',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query derived from compilation; proven by its own matrix test',
  },
  {
    key: 'Workflow#getExternalEventDefsToRegister',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query over side-registrations; proven by its own matrix test',
  },
  {
    key: 'Workflow#getWorkflowEventDefsToRegister',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'query over side-registrations; proven by its own matrix test',
  },
  {
    key: 'Workflow#registerWfSpec',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'an RPC, not a compilation; proven against the fake server (event defs first, then the spec)',
  },
  {
    key: 'Workflow#doesWfSpecExist',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'an RPC, not a compilation; proven against the fake server',
  },
  {
    key: 'Workflow#compileWfToJson',
    kind: 'NO_COMPILED_OUTPUT',
    reason:
      'serialization wrapper over compileWorkflow (which the workflow-minimal probe pins); proven by its matrix test',
  },
  {
    key: 'Workflow#compileAndSaveToDisk',
    kind: 'NO_COMPILED_OUTPUT',
    reason: 'file-IO wrapper over compileWfToJson; proven by its matrix test',
  },
]
