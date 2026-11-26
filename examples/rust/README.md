# LittleHorse Rust examples

These **21 runnable examples** cover the same topics as the Python examples,
using Rust's workflow DSL, typed workers, and derive macros. Each example links
to its Go or Python reference. The advanced SDK-local manual graph demo is
additional and is not included in that count.

## Setup and conventions

Requires Rust 1.89 or later and `lhctl`. Start LittleHorse using the
[shared setup instructions](../README.md), then verify `lhctl whoami`.
Examples target v1.3.1 and the server built from current source.

Run commands from the repository root. Examples read
`~/.config/littlehorse.config` when it exists; `LHC_` environment settings
override it. Defaults connect to localhost:2023. See the
[SDK configuration reference](../../sdk-rust/README.md#clients-and-authentication).

Each example is a standalone program: it registers its metadata and workflows,
then runs its task workers until Ctrl-C. Examples with no task workers exit after
registration; their workflows run on the server. Start workflow runs and send
callbacks from another terminal.
Metadata names use a Rust prefix to keep these examples separate, except KYC,
which deliberately shares the cross-language quickstart names.

## Example index

| Example | Demonstrates |
| --- | --- |
| [basic](basic/README.md) | Start here: greeting with the DSL and a typed worker |
| [quickstart](quickstart/README.md) | KYC identity verification, matching all other SDK quickstarts |
| [casting](casting/README.md) | Cast string inputs to a double and then an integer before typed task execution. |
| [checkpoint-tasks](checkpoint-tasks/README.md) | Persist a checkpoint, fail the task, and replay its result on the retry. |
| [child-thread](child-thread/README.md) | Start a child thread, pass it an input, and join it before returning shared state. |
| [conditionals](conditionals/README.md) | Choose a donut or salad with a workflow if/else. |
| [conditionals-while](conditionals-while/README.md) | Eat donuts in a workflow loop until the counter reaches zero. |
| [correlated-event](correlated-event/README.md) | Correlate an external callback using a business key instead of a workflow ID. |
| [error-handling](error-handling/README.md) | Handle technical task errors and named business exceptions separately. |
| [event-registration](event-registration/README.md) | Register inbound event types and an outbound workflow event before deploying a summary workflow. |
| [external-event](external-event/README.md) | Wait for an external event addressed to a specific workflow run. |
| [hierarchical-workflow](hierarchical-workflow/README.md) | Expose a public parent variable and read it from a separately started child workflow. |
| [interrupt-handler](interrupt-handler/README.md) | Handle an event in an interrupt thread while the main thread is waiting. |
| [json](json/README.md) | Extract JSON paths and send their values to greeting and car-description tasks. |
| [mutation](mutation/README.md) | Assign a task result back to a workflow variable and pass it to another task. |
| [parallel-approval](parallel-approval/README.md) | Fan out one human approval thread per reviewer, then join all threads. |
| [retries](retries/README.md) | Retry a technical task failure using the per-run attempt number. |
| [run-child-workflow](run-child-workflow/README.md) | Register a separate child WfSpec, invoke it, and return its result. |
| [struct-def](struct-def/README.md) | Derive nested native StructDefs, look up a vehicle owner, and pass the typed result to a second worker. |
| [user-tasks](user-tasks/README.md) | Assign a human approval to alice and return the submitted boolean. |
| [wait-for-condition](wait-for-condition/README.md) | Wait for a shared counter to reach zero; interrupt events decrement it. |

[manual-graph](../../sdk-rust/examples/manual-graph.rs) is an advanced alternative:
`cargo run --manifest-path sdk-rust/Cargo.toml --example manual-graph`.
It constructs explicit protobuf nodes and edges, reads only environment settings,
starts one greeting run automatically, and polls a single endpoint.

## Build

All 21 examples belong to one Cargo workspace with one lockfile. Like the other
SDKs' examples, they are compiled and linted in CI but not run; SDK behavior is
covered by the tests in [sdk-rust](../../sdk-rust/README.md#tests).

```sh
cargo fmt --manifest-path examples/rust/Cargo.toml --all -- --check
cargo clippy --manifest-path examples/rust/Cargo.toml --workspace --all-targets --locked -- -D warnings
```
