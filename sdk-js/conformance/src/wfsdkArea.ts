/**
 * wfsdk cases, answered by the real builder (src/wfsdk/) — no knowledge of
 * the expected protos, only the calls each scenario.md describes. Bodies
 * typecheck against the builder's published types, so API drift fails the
 * build, not the grade.
 */
import { Workflow } from '../../dist/wfsdk'
import { PutWfSpecRequest } from '../../dist/proto/service'

type Variant = 'base' | 'feature'

interface Case {
  variants: Variant[]
  build: (variant: Variant) => Workflow
}

const CASES: Record<string, Case> = {
  'workflow-minimal': {
    variants: ['feature'],
    build: () => Workflow.newWorkflow('probe-workflow-minimal', () => {}),
  },
  'sleep-seconds': {
    variants: ['base', 'feature'],
    build: variant =>
      Workflow.newWorkflow('probe-sleep-seconds', wf => {
        if (variant === 'feature') wf.sleepSeconds(30)
      }),
  },
}

export function caseIds(): string[] {
  return Object.keys(CASES)
}

export function compile(caseId: string, variant: string): string {
  const c = CASES[caseId]
  if (!c) throw new Error(`unknown case: ${caseId}`)
  if (!(c.variants as string[]).includes(variant)) throw new Error(`case ${caseId} has no variant "${variant}"`)
  // R11: proto3 JSON with defaults emitted; comparison is semantic.
  const json = PutWfSpecRequest.toJson(c.build(variant as Variant).compileWorkflow(), { emitDefaultValues: true })
  return JSON.stringify(json, null, 2)
}
