# TypeScript WfSpec builder (minimal API sketch)

Design artifact for a **TypeScript workflow-spec DSL** in `littlehorse-client` (`sdk-js`), aligned with Java / Python / Go / C#. Companion API shapes: [`017-typescript-wfspec-minimal-api.proposal.ts`](./017-typescript-wfspec-minimal-api.proposal.ts).

## Scope (initial)

Per team discussion: **variables**, **task nodes**, and **variable mutations** first—then if/while, events, user tasks, etc.

## Cross-SDK parity

| Concept | Java | Python | Go | C# | Proposed TS (camelCase) |
|--------|------|--------|----|----|-------------------------|
| Define workflow | `Workflow.newWorkflow(name, ThreadFunc)` | `Workflow(name, entrypoint)` | `NewWorkflow(ThreadFunc, name)` ⚠️ arg order | `new Workflow(name, Action)` | `Workflow.create(name, threadFn)` |
| Compile | `compileWorkflow()` → `PutWfSpecRequest` | `compile()` | `Compile()` | `Compile()` | `compile()` → `PutWfSpecRequest` |
| Task node | `execute(task, args…)` | `execute(...)` | `Execute(...)` | `Execute(...)` | `execute(...)` |
| Mutate | `mutate(var, type, rhs)` | `mutate(...)` | `Mutate(...)` | `Mutate(...)` | `mutate(...)` |
| Assign sugar | `var.assign(rhs)` | `assign(rhs)` | _(use `Mutate` + ASSIGN)_ | _(use `Mutate` + ASSIGN)_ | `assign(rhs)` |
| Declare var | `declareStr(name)` etc. | `declare_str(name, default=…)` | `DeclareStr` / `AddVariableWithDefault` | `DeclareStr` | `declareStr(name, default?)` |
| Task options | `execute(...).withRetries(n)` etc. | kwargs on `execute` | overrides on node | chain on `TaskNodeOutput` | chain on `TaskNodeOutput` (Java-style) |

Go keeps `(threadFunc, name)`; TS should use the more common **name-first** order like Java/C#/Python.

## Implementation notes

- Reuse generated types under [`sdk-js/src/proto/`](../sdk-js/src/proto): `PutWfSpecRequest`, `ThreadSpec`, `VariableMutation`, `VariableMutationType`, `TaskNode`, etc.
- Mirror the **graph-building model** from other SDKs: one active `WorkflowThread`, implicit edges from the previous node, auto-append **exit** when the thread function returns (same as C# constructor pattern).
- **RHS** of `execute` / `mutate` / `assign`: literals, `WfRunVariable`, `NodeOutput`, and **`LHExpression`** from `add` / `multiply` / … (same layering as Java/Python/Go).
- **Registration:** `putWfSpec` already exists on the gRPC client from [`LHConfig#getClient()`](../sdk-js/src/LHConfig.ts); a future `registerWfSpec()` helper is mostly orchestration + external-event side effects (same as other SDKs).
- **Format strings:** Java/C# use `{0}`-style indices; Python’s `LHFormatString` uses `{}` — choose one for TS and document it so examples stay consistent.
- **Deferred parity:** JSON helpers (`extend`, `removeKey`, …), variable modifiers (`.searchable()`, `.masked()`, …), `doIf` / `spawnThread`, etc. follow after the minimal milestone.

The `.proposal.ts` file is **not** part of the `sdk-js` build (`tsconfig.json` only includes `sdk-js/src/`); it exists for review until the API is implemented under `sdk-js/src/`.
