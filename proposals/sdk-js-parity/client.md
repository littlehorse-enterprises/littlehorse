# Proposal: sdk-js Parity — the Client Layer

- Status: **Draft** (largely implemented; planned checks marked)
- Scope: `sdk-js/src/LHConfig.ts`, `sdk-js/src/client.ts`, `sdk-js/src/grpcRetry.ts`,
  `sdk-js/src/common/`, `sdk-js/src/usertask/`
- Parent: [README.md](./README.md)

This file covers everything between a user's code and the wire: the generated
RPC methods, the hand-written configuration layer around them, the shared
value-encoding code, and User Task form definitions. It is the easiest layer to
prove, for a reason worth understanding.

## Contents

- [The RPC surface: parity by construction](#the-rpc-surface-parity-by-construction)
- [The hand-written layer](#the-hand-written-layer)
- [Shared value encoding (serde)](#shared-value-encoding-serde)
- [User Task forms](#user-task-forms)
- [Coverage today, and the planned checks](#coverage-today-and-the-planned-checks)

## The RPC surface: parity by construction

Neither SDK hand-writes `runWf`, `getWfRun`, `putWfSpec`, or any of the other
RPC methods. Both **generate** their clients from the same `service.proto` —
Java through its gRPC code generator, JS through `@protobuf-ts`. One shared
contract, two generated outputs.

That dissolves the usual parity question for this surface. The method list
cannot drift between the SDKs, because neither SDK owns it — the proto file
does. The only way to fall behind is **stale generated code**, so the only
checks needed are freshness checks:

- **A drift gate in CI** (planned): regenerate the JS protos and fail on any
  difference from what is committed.
- **A meta-test** (planned, three lines): iterate the generated method list and
  assert every declared RPC is callable on our client. The technique already
  exists in `client.ts`, which iterates `LittleHorse.methods` for request
  defaulting.
- **A tripwire that already runs**: the golden loader parses fixtures with
  `ignoreUnknownFields: false`, so if sdk-java ever emits a proto field our
  generated code does not know, the suite fails loudly — stale codegen cannot
  hide behind passing tests.

## The hand-written layer

What *is* authored, and therefore gets the full two-question treatment, is the
thin layer around the stubs:

**`LHConfig`** — configuration loading (a source-composing builder over env
vars, properties files, and in-memory maps, merged in call order like Java's
`LHConfigBuilder`), TLS and mutual-TLS credentials, gRPC keepalive options,
tenant and bearer-token metadata, and worker settings.

**`client.ts`** — wraps the generated client so unary calls return plain
Promises, and normalizes requests through each message's own `create()`. That
normalization exists because of a real divergence the integration tier found:
`@protobuf-ts` requires every repeated and map field to be present, and
omitting one fails deep inside serialization with an opaque error — while
Java's builders let users leave fields out. Accepting partial messages restores
what Java users take for granted.

**`grpcRetry.ts`** — automatic retry of `RESOURCE_EXHAUSTED` errors, honoring
the server-provided `RetryInfo` delay when present. Errors with no server-sent
detail (for example, a response over the configured message-size limit,
generated locally by grpc-js) are never retried.

**OAuth** (`common/oauth.ts`) — client-credentials flow: fetch a token with
HTTP Basic, cache it, refresh inside a configurable window before expiry, and
collapse concurrent refreshes into a single request. Proven against a real
Keycloak instance, not a mock — see [integration.md](./integration.md) for why
the issuer must be pinned.

**Type adapters** (`common/typeAdapters.ts`) — user-registered encoders for
custom types the SDK does not know (a `Money`, a `Decimal`), mirroring Java's
`LHTypeAdapter`. One deliberate difference, recorded so nobody "fixes" it:
adapters apply automatically when **writing**, but reading stays with the
built-ins unless a caller asks for an adapter by name. An encoded value carries
no marker saying which adapter produced it, and guessing wrong would silently
return the wrong type.

## Shared value encoding (serde)

`common/serde.ts` is the **single** JS ↔ `VariableValue` conversion in the
SDK; the wfsdk and the worker both call it, deliberately, so the two can never
disagree about encoding. Its oracle is a fixture of 24 representative values
**as sdk-java encodes them** (`golden/fixtures/serde.json`), asserted
byte-for-byte — because two SDKs can each "work" while disagreeing on bytes,
and that disagreement silently corrupts data written by one and read by the
other.

Building against that fixture caught two divergences no JS-only test could
see:

- **Java drops `null` object fields, recursively — but keeps nulls inside
  arrays.** Plain `JSON.stringify` does neither, so a custom serializer exists.
- **Guessing a value's type from its shape is unsafe in JS.** An early version
  detected `WfRunId` structurally and would have encoded the very common plain
  object `{id: 'abc'}` as a workflow-run ID — silently discarding every other
  field. Storing a `WF_RUN_ID` now requires passing an explicitly built value.

One known gap, still open: the decoder has no case for the native typed
`ARRAY`/`MAP` value kinds — such values round-trip through the server
correctly but come back as `undefined`. Found by running the examples; tracked
in [roadmap.md](./roadmap.md). It is also this proposal family's favorite
cautionary tale: the serde enumeration was written by hand once, missed two
kinds, and every consumer inherited the blind spot.

## User Task forms

`usertask/` defines User Task forms (Java: an annotated class) using zod
schemas plus display metadata, compiling to the same `PutUserTaskDefRequest`.
Zod is the deliberate, consistent stand-in everywhere Java uses classes and
annotations to describe types at runtime — task inputs, struct definitions,
event payloads, and these forms. Fields must be primitives the form can
render; struct and collection schemas are rejected rather than silently
becoming JSON.

## Coverage today, and the planned checks

Today: 22 config/client entries, 7 serde entries, and 4 usertask entries in
the matrix, each citing its Java source. The planned mechanical checks, in
order of cheapness:

1. **The config-key comparison** — Java's recognized environment keys versus
   ours, asserted as *containment plus an explicit allowlist* (not equality:
   sdk-js legitimately adds a key Java lacks, and every such divergence should
   be written down with a reason). This permanently mechanizes a real bug the
   audit found: sdk-js invented `LHC_`-prefixed spellings for worker settings
   that Java reads from the `LHW_` namespace. Since those spellings never
   shipped, the fix is a straight rename — and this check makes the mistake
   unrepeatable. The same pass decides the in-flight-tasks default (currently
   8, matching .NET; Java uses 2 and, as the stated gold standard, should
   win).
2. **The RPC meta-test and CI drift gate** described above.
3. **Extending the freshness check** ([wfsdk.md](./wfsdk.md), Design 1) to
   Java's `LHConfig` and related classes — the same reflection script with more
   classes listed, and a much smaller exemption list than the wfsdk needs.

Judgment residue for this layer, kept in writing: the config-key allowlist.
