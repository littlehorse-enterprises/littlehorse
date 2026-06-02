# TypeScript WfSpec builder (minimal API)

## Motivation

[`sdk-js`](../sdk-js) already has a gRPC client, generated protos, and a task worker. There is no workflow-spec DSL yet—you either build `PutWfSpecRequest` by hand, deploy JSON with `lhctl`, or define the spec in another language. This doc sketches a **small first API** for defining specs in TypeScript the same way as in Java / Python / Go / .NET.

## Proposed Protocol Buffer changes

**None.** Compilation targets the existing public messages in [`schemas/`](../schemas/).

## Proposed SDK API changes

New workflow-builder types + implementation under `sdk-js/src/` (module path TBD) that follow the same graph-building rules as the other SDKs and emit the same protobuf you could build manually.

**Initial surface:** `Workflow.create`, `WorkflowThread` (`declare*`, `execute`, `mutate`, `format`, `complete`), `WfRunVariable.assign`, and **expression chaining on `LHExpression`** on variables / node outputs (same as Java—not `thread.add` / `thread.multiply`). Optional later: `registerWfSpec` helper like other SDKs.

Details and a worked example are in [Proposed TypeScript surface](#proposed-typescript-surface).

## Server architecture and operations

**None** — client-only.

## Backwards compatibility

**Additive** for `littlehorse-client`; existing exports unchanged.

---

## Scope (initial)

Variables, task nodes, **variable mutations** (`mutate` + `assign`, with RHS expressions on `LHExpression`). Control flow, external events, user tasks, etc. come later.

## Cross-SDK parity

Shapes follow **Java** especially: [`WfRunVariable`](../sdk-java/src/main/java/io/littlehorse/sdk/wfsdk/WfRunVariable.java) and [`NodeOutput`](../sdk-java/src/main/java/io/littlehorse/sdk/wfsdk/NodeOutput.java) extend [`LHExpression`](../sdk-java/src/main/java/io/littlehorse/sdk/wfsdk/LHExpression.java), so `add` / `multiply` / … are **instance methods on the LHS value**, not on `WorkflowThread`. Python does the same on `WfRunVariable` / `NodeOutput`.

| Concept                             | Java                                              | Python                       | Go                                 | C#                           | Proposed TS (camelCase)                           |
| ----------------------------------- | ------------------------------------------------- | ---------------------------- | ---------------------------------- | ---------------------------- | ------------------------------------------------- |
| Define workflow                     | `Workflow.newWorkflow(name, ThreadFunc)`          | `Workflow(name, entrypoint)` | `NewWorkflow(ThreadFunc, name)` ⚠️ | `new Workflow(name, Action)` | `Workflow.create(name, threadFn)`                 |
| Compile                             | `compileWorkflow()`                               | `compile()`                  | `Compile()`                        | `Compile()`                  | `compile()` → `PutWfSpecRequest`                  |
| Task node                           | `execute(...)`                                    | `execute(...)`               | `Execute(...)`                     | `Execute(...)`               | `execute(...)`                                    |
| Set variable                        | `var.assign(rhs)`                                 | `assign(rhs)`                | `Mutate(..., ASSIGN, ...)`         | `Mutate(..., ASSIGN, ...)`   | `assign(rhs)`                                     |
| Math / RHS expressions              | `var.add(x)`, `out.multiply(y)` on `LHExpression` | same idea                    | `t.Add(lhs, rhs)` on thread        | expression types             | `var.add(x)`, `out.multiply(y)` on `LHExpression` |
| Thread-level `mutate(lhs, op, rhs)` | yes                                               | yes                          | yes                                | yes                          | `mutate(...)` (same semantics as other SDKs)      |
| Declare var                         | `declareStr` / `addVariable`                      | `declare_str`, …             | `DeclareStr`, …                    | `DeclareStr`                 | `declareStr`, …                                   |
| Task retries / timeout              | `TaskNodeOutput.withRetries` / `timeout`          | kwargs on `execute`          | node overrides                     | chained setters              | chain on `TaskNodeOutput` (Java-style)            |

Go keeps `NewWorkflow(fn, name)` argument order; TS should stay **name-first** like Java/C#/Python.

## Implementation notes

- Generated types: [`sdk-js/src/proto/`](../sdk-js/src/proto) (`PutWfSpecRequest`, `ThreadSpec`, `VariableMutation`, `TaskNode`, …).
- One active thread builder, implicit edges, auto **EXIT** at end of thread callback (same pattern as .NET constructor-driven thread build).
- **Registration:** gRPC `putWfSpec` already exists on [`LHConfig#getClient()`](../sdk-js/src/LHConfig.ts).
- **Format strings:** Java/C# use `{0}`; Python uses `{}` for `LHFormatString` — pick one for TS and document it.
- **Deferred:** JSON helpers (`extend`, `removeKey`, …) beyond the core four ops on `LHExpression`, variable modifiers (`.searchable()`, `.masked()`, …), `doIf`, `spawnThread`, etc.

## Proposed TypeScript surface

```typescript
import type { PutWfSpecRequest } from "../sdk-js/src/proto/service";
import type { VariableType } from "../sdk-js/src/proto/common_enums";
import type {
  ExponentialBackoffRetryPolicy,
  VariableMutationType,
} from "../sdk-js/src/proto/common_wfspec";

export type WorkflowRhs = unknown;

/** Composable RHS / condition building — default methods on Java LHExpression. */
export interface LHExpression {
  add(other: WorkflowRhs): LHExpression;
  subtract(other: WorkflowRhs): LHExpression;
  multiply(other: WorkflowRhs): LHExpression;
  divide(other: WorkflowRhs): LHExpression;
}

export interface WfRunVariable extends LHExpression {
  readonly name: string;
  jsonPath(path: string): WfRunVariable;
  get(field: string): WfRunVariable;
  assign(rhs: WorkflowRhs): void;
}

export interface NodeOutput extends LHExpression {
  jsonPath(path: string): NodeOutput;
  get(field: string): NodeOutput;
}

export interface TaskNodeOutput extends NodeOutput {
  withRetries(retries: number): TaskNodeOutput;
  timeout(timeoutSeconds: number): TaskNodeOutput;
  withExponentialBackoff(policy: ExponentialBackoffRetryPolicy): TaskNodeOutput;
}

export interface LHFormatString {
  readonly _lhFormatString: true;
}

export interface WorkflowThread {
  declareStr(name: string, defaultValue?: WorkflowRhs): WfRunVariable;
  declareInt(name: string, defaultValue?: WorkflowRhs): WfRunVariable;
  declareDouble(name: string, defaultValue?: WorkflowRhs): WfRunVariable;
  declareBool(name: string, defaultValue?: WorkflowRhs): WfRunVariable;
  declareStruct(name: string, structDefName: string): WfRunVariable;
  addVariable(
    name: string,
    options: { type?: VariableType; structDefName?: string },
  ): WfRunVariable;

  execute(
    taskName: string | WfRunVariable | LHFormatString,
    ...args: WorkflowRhs[]
  ): TaskNodeOutput;

  mutate(
    lhs: WfRunVariable,
    operation: VariableMutationType,
    rhs: WorkflowRhs,
  ): void;

  format(template: string, ...args: WorkflowRhs[]): LHFormatString;

  complete(result?: WorkflowRhs): void;
}

export type ThreadFunc = (thread: WorkflowThread) => void;

export interface Workflow {
  readonly name: string;
  compile(): PutWfSpecRequest;
}

export declare const Workflow: {
  create(name: string, entrypoint: ThreadFunc): Workflow;
};

function exampleOrderFlow() {
  const wf = Workflow.create("example-order-flow", (thread) => {
    const orderId = thread.declareStr("order-id");
    const total = thread.declareDouble("total", 0);

    const fetch = thread.execute("fetch-order", orderId);
    total.assign(fetch.jsonPath("$.total"));

    const taxRate = thread.declareDouble("tax-rate", 0.08);
    const amountDue = total.add(total.multiply(taxRate));
    const charged = thread
      .execute("charge-card", amountDue, orderId)
      .withRetries(3);

    thread.complete(charged);
  });

  return wf.compile();
}

void exampleOrderFlow;
```
