# LittleHorse Rust SDK

[![CI](https://github.com/littlehorse-enterprises/littlehorse/actions/workflows/rust.yml/badge.svg)](https://github.com/littlehorse-enterprises/littlehorse/actions/workflows/rust.yml)

Async Rust SDK for LittleHorse, with v1.3.1 server compatibility, using Tokio, tonic, and prost. Includes
all public RPCs from the shared schemas, a workflow DSL, native value and schema adapters, task workers,
cluster assignment/reconnection, durable checkpoints, TLS/mTLS, and OAuth refresh.
All **147 upstream conformance cases** are implemented: 119 workflow cases, 20
serialization cases, and 8 registration cases. Base and feature variants are
independently constructed through Rust APIs and graded by the upstream runner.

## Use from Git

```toml
[dependencies]
littlehorse = { git = "https://github.com/littlehorse-enterprises/littlehorse" }
tokio = { version = "1", features = ["full"] }
```

Rust **1.89 or later** is required. The build includes protobuf schemas and a platform-specific
protoc binary; a system protoc installation is unnecessary.

From the repository root, start a local server and the
[basic DSL example](../examples/rust/basic/README.md):

```sh
docker run --rm -d --name littlehorse -p 2023:2023 \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:1.3.1
lhctl whoami
cargo run --manifest-path examples/rust/basic/Cargo.toml
```

Keep the worker running and use another terminal to run `lhctl run rust-basic name Ada`.
The worker prints `Hello, Ada!`. Press Ctrl-C to stop the worker, then
`docker stop littlehorse` to stop the local server.

For the standard KYC workflow used by the other SDKs, see the
[Rust quickstart](../examples/rust/quickstart/README.md). From the repository root,
run `cargo run --manifest-path examples/rust/quickstart/Cargo.toml`, then use
`lhctl` to start a workflow and send its identity-verification callback.

For advanced, explicit node-and-edge construction, run
`cargo run --manifest-path sdk-rust/Cargo.toml --example manual-graph` from the
repository root. This greeting demo was previously named `quickstart`.
See the [Rust example index](../examples/rust/README.md) for 21 runnable examples,
including control flow, events, child workflows, human tasks, retries, checkpoints,
and native structs.

## Workflows

```rust
use littlehorse::Workflow;
use littlehorse::proto::VariableMutationType;

let workflow = Workflow::define("count-to-three", |wf| {
    let count = wf.declare_int("count").with_default(0);
    wf.while_loop(count.less_than(3), |_| {
        count.mutate(VariableMutationType::Add, 1);
    });
    wf.return_value(&count);
})?;
let request = workflow.compile()?;
# Ok::<(), littlehorse::WorkflowError>(())
```

`ThreadBuilder` supports sequential tasks, dynamic task names, conditional chains,
loops, variable mutations, child threads and fan-out, all/any/first joins, child
workflows, external/correlated events, emitted workflow events, sleeps, condition
waits, interrupts, error/exception handlers, user tasks, and timer actions.

As in the other SDKs, options chain on the node you just added:
`wf.execute("charge", &order).with_retries(3).timeout(10)`,
`wf.wait_for_event("paid").timeout(60).with_correlation_id(&order_id)`, and
`wf.assign_user_task_to_user("review", "alice").with_notes("Approve?")`. Conditionals
chain the same way: `wf.do_if(a, |_| ...).do_else_if(b, |_| ...).do_else(|_| ...)`.
`TaskOptions` and `EventOptions` remain for building node options up front, and
`Variable` holds required/searchable/masked/default/access/index options.
Declare variables with `declare_str`, `declare_int`, `declare_bool`, `declare_double`,
`declare_json_obj`, `declare_json_arr`, or `declare_struct::<T>`, and chain options:
`wf.declare_str("name").with_default("world").searchable()`. Variables share their
definition, so options can also be set after declaring, as in the other SDKs.
`with_default` accepts native values; a value that cannot be converted is returned
as an error from `Workflow::define`.

Like the other SDKs, variables and node outputs work directly as task inputs and in
expressions: `wf.execute("greet", &name)`, `wf.execute("notify", (&full_name, &email))`,
`donuts.less_than(10)`. `name.assign(value)` and `name.mutate(op, value)` apply at the
current point of the thread being built, including inside branches and child threads.
Body closures can return `()` or `Result<(), WorkflowError>`; builder problems are
collected and returned from `Workflow::define`.
`Expression` supports arithmetic, comparisons, boolean operations, collection
operations, formatting, casts, JSON paths, typed paths, struct builders, and maps.
`ThreadBuilder::append` and `node_mut` expose advanced protobuf settings.

`Workflow::compile` validates graph structure. `to_json`/`save` use standard
protobuf JSON; `json::workflow_from_json` loads saved definitions. `dependencies`
returns referenced metadata names. `register` deploys the workflow; `schema::Registrations`
can register its structs, tasks, event types, and human task forms first.

## Native values and schemas

```rust
use littlehorse::{LittleHorseValue, StructSchema};

#[derive(Debug, PartialEq, LittleHorseValue)]
#[lh(name = "customer")]
struct Customer {
    #[lh(rename = "full-name")]
    name: String,
    #[lh(masked, description = "Customer balance")]
    balance: i64,
    labels: Vec<String>,
    nickname: Option<String>,
}
let customer = Customer {
    name: "Ada".into(), balance: 42, labels: vec!["priority".into()], nickname: None,
};
let value = customer.to_lh()?;
assert_eq!(Customer::from_lh(&value)?, customer);
let registration = Customer::registration();
# Ok::<(), littlehorse::types::TypeError>(())
```

`LittleHorseValue` is also a custom adapter trait. Implementations cover primitive
values, bytes (`types::Bytes`), timestamps, workflow IDs, native arrays (`Vec<T>`),
native maps (`HashMap<K,V>`), nullable values, and named/inline structs (`types::Inline<T>`).
Derived schemas include nested dependencies. `Worker::register_typed` registers
those dependencies before its task definition. `to_value`/`from_value` provide
serde conversion for primitives and JSON objects/arrays.

## Clients and authentication

```rust,no_run
use littlehorse::{ClientConfig, proto::RunWfRequest};
# async fn example() -> Result<(), littlehorse::client::ClientError> {
let mut client = ClientConfig::from_env()?.connect().await?;
let run = client.run_wf(RunWfRequest {
    wf_spec_name: "count-to-three".into(), ..Default::default()
}).await?.into_inner();
# Ok(())
# }
```

`ClientConfig::from_env`, `from_properties`, and `from_file` configure connections.
Files accept `key=value` or `key:value` entries; `LHC_` environment settings override
file entries. Supported settings:

| Settings | Meaning |
| --- | --- |
| `LHC_API_HOST`, `LHC_API_PORT` | Defaults: localhost, 2023 |
| `LHC_API_PROTOCOL` | PLAINTEXT or TLS |
| `LHC_TENANT_ID`, `LHC_BEARER_TOKEN` | Tenant and static bearer credentials |
| `LHC_CA_CERT`, `LHC_CLIENT_CERT`, `LHC_CLIENT_KEY` | PEM file paths for TLS/mTLS |
| `LHC_OAUTH_CLIENT_ID`, `LHC_OAUTH_CLIENT_SECRET`, `LHC_OAUTH_ACCESS_TOKEN_URL` | OAuth client credentials |
| `LHC_OAUTH_SCOPE` | Optional token scope |
| `LHC_GRPC_KEEPALIVE_TIME_MS`, `LHC_GRPC_KEEPALIVE_TIMEOUT_MS` | Connection keepalive |
| `LHC_INFLIGHT_TASKS` | Global execution ceiling for `run_cluster`, also bounded by `Worker::concurrency` |

OAuth tokens are refreshed on demand before expiry, with concurrent refreshes
serialized. Debug output redacts credentials. `ClientConfig` additionally accepts
explicit endpoints, tonic TLS settings, and connection/request deadlines.

Generated clients expose every management/search/scheduling/admin/migration RPC.
`pagination::Paginator` supports bounded asynchronous cursor pagination.

## Workers

Use named inputs with `context.input_named::<String>("email")?` instead of positional indexes.
For ordinary async functions, the task attribute generates a worker and registration helper:

```rust,no_run
use littlehorse::{Client, worker::TaskFailure};

#[littlehorse::task("send-email")]
async fn send_email(email: String, subject: String, body: String) -> Result<String, TaskFailure> {
    Ok(format!("{email}: {subject}: {body}"))
}

async fn register(client: &mut Client) -> Result<(), littlehorse::TaskRegistrationError> {
    send_email::register(client).await?;
    let _worker = send_email::worker();
    Ok(())
}
# fn main() {}
```

`#[task]` must be applied to a function at module level, not inside another function.

Parameter names become TaskDef input names; the workflow DSL remains unchanged:
`wf.execute("send-email", (&email, &subject, &body))`.


`Worker::new(task_name, async_handler)` supplies `TaskContext` for positional/native
inputs, task/attempt/source IDs, stable idempotency keys, user-task ownership,
logging, and checkpoints. `Worker::typed<I,O>` adapts typed one-argument handlers.
Use `register`/`register_typed` before starting polling.

`run_cluster(config, shutdown)` periodically registers with the bootstrap server,
reconciles assigned hosts, reconnects streams, and shares an execution limit across
hosts. `run(client, shutdown)` polls an explicit single endpoint. `health()` exposes
registration, cluster health, assignment, connections, and errors through a watch channel.
Configure `concurrency`, `heartbeat_interval`, `reconnect_delay`, and `drain_timeout`.
`worker::start(config, workers)` runs workers until Ctrl-C and then drains them, like
`start` in the other SDKs.

A watch value of true, or closing the sender, stops polling and drains accepted
work. The drain deadline cancels remaining handlers and returns an error. Handler
panics, input/output conversion errors, technical failures, and business exceptions
are reported distinctly. Transient reporting failures retry the report, never the
user's function. Checkpoint fencing cancels the handler and suppresses stale reports.

`checkpoint`/`checkpoint_typed` replay persisted results before executing new steps.
Keep checkpoint calls in the same order across retries. Side effects must be
idempotent: a process can crash after the effect but before checkpoint persistence.
Async cancellation cannot undo external effects or stop blocking user code.

## Tests

```sh
cargo fmt --all -- --check
cargo clippy --workspace --all-targets --all-features --locked -- -D warnings
cargo test --workspace --all-targets --all-features --locked
cargo test --workspace --doc --all-features --locked
cargo doc --workspace --no-deps --locked
# With a running v1.3.1 server:
cargo test --test live_server --test live_features --locked -- --ignored
```

GitHub CI runs the full Java workflow fixture comparison, native serialization,
OAuth/TLS/mTLS fixtures, worker lifecycle tests, documentation, and server-backed
behavioral tests against v1.3.1 and a server built from the current source.
CI also grades Java, JavaScript, and Rust together and compares 50 seeded workflows. Tests needing the live server
are ignored in ordinary local test runs and explicitly enabled in the integration job.

For master-native human tasks, use `UserTaskSchema::struct_backed` with an exact
registered `StructDefId`, and complete the task using `CompleteUserTaskRunRequest.output`.
Legacy primitive fields remain supported for v1.3.1 compatibility. Registration
helpers resolve and pin human-task definition versions before submission, so
output inference is safe on master. Lookup retries are limited to
`NOT_FOUND` during the first five seconds of metadata visibility; other errors
propagate immediately. Offline compilation does not
perform network lookups. When submitting raw `PutWfSpecRequest` messages, callers
can use `schema::pin_user_task_versions` explicitly.

Build the conformance testee from the repository root with
`cargo build --manifest-path sdk-rust/Cargo.toml --features conformance --bin lh-conformance`.
The shared grader uses `sdk-conformance/testees.json` to run Rust alongside the other SDKs.
`LITTLEHORSE_SCHEMA_DIR` and `LITTLEHORSE_CONFORMANCE_DIR` optionally select external
schemas and fixtures. Source builds use `../schemas/littlehorse` and
`../sdk-conformance/areas`; crate archives include the public schemas.
