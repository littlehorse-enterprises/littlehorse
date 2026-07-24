# Configurable gRPC Max Inbound Message Size

**Author:** Eduwer Camacaro

## Motivation

Each LittleHorse server instance deploys at least two gRPC servers:

1. **Public API:** clients (SDKs, `lhctl`, dashboard) talk to the server's public listeners. Operators can configure multiple listeners for public API access.
2. **Internal server-to-server communication:** LH Server instances forward requests to one
   another (Interactive Queries, waiting for commands/events, etc.) via the internal gRPC
   listener and internal clients.

gRPC enforces a **maximum inbound message size** (default 4 MiB). When a payload exceeds this
limit, the request fails with `RESOURCE_EXHAUSTED`. Some workloads legitimately need larger
payloads — for example large `ExternalEvent`s, `Struct`s, or `WfRun`s' input variables. 
Until now this limit was set by gRPC defaults, so operators had no way to raise (or lower) it to
fit their environment.

This proposal makes the gRPC max inbound message size **configurable via `LHS_`** config and
ensures the same value is applied consistently to both the public listeners **and** internal
server-to-server communication. Applying it to the internal path is important: if a public
listener accepts a large message but the internal channel that forwards it still uses the 4 MiB
default, cross-server RPCs would fail even though the client-facing limit was raised.

### Out of Scope

* Per-listener or per-tenant overrides. The value is cluster/instance-wide.
* Changing the Kafka producer max request size (`LHS_PRODUCER_MAX_REQUEST_SIZE`), which is a
  separate concern governing the size of records written to Kafka.

## Public Contract

### New Configuration Option

| Config Key | Default | Description |
|------------|---------|-------------|
| `LHS_GRPC_MAX_INBOUND_MESSAGE_SIZE` | `4194304` (4 MiB) | Maximum size, in bytes, of an inbound gRPC message the server will accept. Applied to both the public listeners and internal server-to-server communication. |

The default (`4194304`) matches gRPC's built-in default, so existing deployments observe **no
behavioral change** unless they explicitly set the value.

There are **no Protobuf API changes** and **no SDK API changes**. This is purely a server-side
operational configuration.

### Performance / Operational Considerations

* Raising this limit allows larger messages to be buffered in memory per in-flight request.
  Operators should size the value against available heap and expected concurrency to avoid
  memory pressure. This is the standard trade-off documented by gRPC.
* Lowering the value can be used defensively to reject oversized payloads earlier.
* Raising the value without raising the `LHS_PRODUCER_MAX_REQUEST_SIZE` can increase the changes of hitting RecordTooLarge exceptions.


