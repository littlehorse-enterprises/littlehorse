/**
 * The freshness check's quarantine file (proposals/sdk-js-parity/wfsdk.md,
 * Design 1): every public sdk-java wfsdk member that deliberately has NO
 * feature-matrix citation, each with a written reason.
 *
 * The rules, enforced by conformance/surface.test.ts:
 *  - An exemption is a PERMANENT claim ("this will never need its own JS
 *    coverage"). A feature we merely haven't ported yet is spelled
 *    `test.todo`, never an exemption — todos stay visible in the banner.
 *  - Every `key` must exist in golden/java-surface.json (dead entries fail).
 *  - Every `coveredBy` must itself be a cited symbol (excuses can't chain).
 *  - An exemption for a symbol that IS cited fails — the entry is stale.
 */

export type ExemptionKind =
  /** Same operation reachable through another (cited) entry point. */
  | 'COVERED_BY'
  /** A Java-language artifact with no JS counterpart by design. */
  | 'JAVA_IDIOM'
  /** Deliberately not part of the JS surface; reason says why. */
  | 'NOT_APPLICABLE'

export interface SurfaceExemption {
  /** `Class#method`, exactly as in golden/java-surface.json. */
  key: string
  kind: ExemptionKind
  /** Required for COVERED_BY: the cited symbol that carries the coverage. */
  coveredBy?: string
  reason: string
}

export const EXEMPTIONS: SurfaceExemption[] = [
  // ── Functional-interface plumbing ──────────────────────────────────────
  {
    key: 'IfElseBody#body',
    kind: 'JAVA_IDIOM',
    reason:
      'The SAM method of a functional interface; JS passes a plain function ' +
      '(the IfElseBody type in WorkflowThread.ts), so there is no method to test.',
  },
  {
    key: 'ThreadFunc#threadFunction',
    kind: 'JAVA_IDIOM',
    reason:
      'The SAM method of a functional interface; JS passes a plain function ' +
      '(the ThreadFunc type), so there is no method to test.',
  },

  // ── Compiler hooks Java exposes on public types ────────────────────────
  {
    key: 'SpawnedThreads#buildNode',
    kind: 'NOT_APPLICABLE',
    coveredBy: 'WorkflowThread#waitForThreads',
    reason:
      'A compile-time hook (returns the WaitForThreadsNode proto). The JS ' +
      'design builds that node inside WorkflowThread#waitForThreads/' +
      'waitForAnyOf/waitForFirstOf, where it is proven.',
  },

  // ── Trivial accessors of state proven through their setters ────────────
  {
    key: 'Workflow#getName',
    kind: 'COVERED_BY',
    coveredBy: 'Workflow#newWorkflow',
    reason: 'Accessor of the constructor-set name. Implemented in JS (Workflow.ts#getName).',
  },
  {
    key: 'Workflow#getDefaultTaskTimeout',
    kind: 'COVERED_BY',
    coveredBy: 'Workflow#setDefaultTaskTimeout',
    reason: 'Accessor of the cited setter’s state. Implemented in JS (Workflow.ts#getDefaultTaskTimeout).',
  },
  {
    key: 'Workflow#getTypeAdapterRegistry',
    kind: 'COVERED_BY',
    coveredBy: 'LHConfigBuilder#addTypeAdapter',
    reason:
      'In JS the adapter registry deliberately lives on LHConfig (one registry ' +
      'per config, shared by wfsdk and worker) rather than per-Workflow; the ' +
      'registry surface is proven in the config area.',
  },

  // ── WorkflowThread’s expression-factory forms ──────────────────────────
  // Java (and JS) expose every expression operation twice: as a method on the
  // value (`x.add(5)`, cited as LHExpression#add and exercised by the matrix)
  // and as a factory on the thread (`wf.add(x, 5)`). Both build the same
  // LHExpressionImpl and compile through the same toVariableAssignment path.
  // The factory spellings are exempted as duplicates of the proven operation;
  // upgrading them to their own probe pairs is a candidate for the probe
  // rollout (wfsdk.md, Design 2).
  ...(
    [
      ['add', 'LHExpression#add'],
      ['subtract', 'LHExpression#subtract'],
      ['multiply', 'LHExpression#multiply'],
      ['divide', 'LHExpression#divide'],
      ['pow', 'LHExpression#pow'],
      ['extend', 'LHExpression#extend'],
      ['removeIfPresent', 'LHExpression#removeIfPresent'],
      ['removeIndex', 'LHExpression#removeIndex'],
      ['removeKey', 'LHExpression#removeKey'],
    ] as const
  ).map(
    ([factory, coveredBy]): SurfaceExemption => ({
      key: `WorkflowThread#${factory}`,
      kind: 'COVERED_BY',
      coveredBy,
      reason: `Thread-level factory form of the same expression operation (wf.${factory}(lhs, rhs)).`,
    })
  ),
]

/**
 * Expansions for grouped citations. A test title may cite
 * `— Java: LHExpression#castToInt etc.` when it sweeps a family of methods;
 * a script cannot guess what "etc." means, so this table spells it out. The
 * surface test fails if a grouped citation has no entry here, and if any
 * expansion names a symbol that does not exist.
 */
export const CITATION_ALIASES: Record<string, string[]> = {
  // 'cast to INT / DOUBLE / STR / BOOL / BYTES / WF_RUN_ID' sweeps the typed
  // casts; the generic castTo is cited separately by its own test.
  'LHExpression#castToInt': [
    'LHExpression#castToBool',
    'LHExpression#castToBytes',
    'LHExpression#castToDouble',
    'LHExpression#castToStr',
    'LHExpression#castToWfRunId',
  ],
}
