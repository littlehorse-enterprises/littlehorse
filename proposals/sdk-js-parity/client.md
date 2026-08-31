# Proposal: sdk-js Parity — the Client Layer

- Status: **Draft** (largely implemented; planned checks marked)
- Scope: `sdk-js/src/LHConfig.ts`, `sdk-js/src/client.ts`, `sdk-js/src/grpcRetry.ts`,
  `sdk-js/src/common/`, `sdk-js/src/usertask/`
- Parent: [README.md](./README.md)

The client layer is the part of the SDK that **talks to the server**: it makes
the calls, carries the settings and credentials those calls need, and
translates values between JavaScript and the network. This file assumes no
knowledge of how SDKs are built — the context is explained as it appears.

## Contents

- [Context: how an SDK talks to a server](#context-how-an-sdk-talks-to-a-server)
- [What the client layer contains](#what-the-client-layer-contains)
- [The generated part: it cannot drift, only go stale](#the-generated-part-it-cannot-drift-only-go-stale)
- [The hand-written part](#the-hand-written-part)
- [The shared translator: serde](#the-shared-translator-serde)
- [User Task forms](#user-task-forms)
- [How this layer is tested today](#how-this-layer-is-tested-today)
- [The plan](#the-plan)

## Context: how an SDK talks to a server

**RPC** ("remote procedure call") is the trick of making a network call look
like an ordinary function call. You write:

```ts
const wfRun = await client.getWfRun(id)
```

…and machinery underneath turns that into a request to the server, waits, and
hands the reply back as a return value. The function is local; the work
happens remotely.

**gRPC** is the specific RPC technology LittleHorse uses. Its key idea: the
entire API is described in a **contract file** (`service.proto`) — every
method name, every request and response shape. Nobody hand-writes the calling
code; a compiler reads the contract and **generates** it, in any language. The
generated calling object is called a **stub** (Java's term) or a **client**
(the JS ecosystem's term) — same thing: an object with all the server's
methods, where each method body just packages your arguments, sends them, and
unpacks the reply. The server's shape, without the server. The messages
themselves travel as **protobuf** — a compact binary format, also defined by
the contract — which is why "do both SDKs produce the same bytes?" is a
meaningful question throughout this proposal family.

One consequence matters for everything below: each SDK carries a **generated
snapshot** of the contract, produced at some point in time. Java's snapshot is
rebuilt automatically on every build; ours is checked into the repo
(`src/proto/`) and regenerated manually. Snapshots of the same contract cannot
*disagree* — but one can be *older* than the other.

## What the client layer contains

| Piece                    | What it is, plainly                                                           |
| ------------------------ | ----------------------------------------------------------------------------- |
| the generated stub       | every RPC method (`runWf`, `getWfRun`, …) — machine-made from `service.proto` |
| `client.ts`              | a thin wrapper making stub calls nicer to use (details below)                 |
| `LHConfig.ts`            | settings: which server, which tenant, TLS certificates, login credentials    |
| `grpcRetry.ts`           | automatic retry when the server says "busy, try again"                        |
| `common/oauth.ts`        | machine login — turns a client id + secret into a token, and keeps it fresh   |
| `common/serde.ts`        | the value translator: JavaScript values ↔ wire format (shared with all layers) |
| `common/typeAdapters.ts` | user-registered translators for custom types the SDK doesn't know             |
| `usertask/`              | defines the forms humans fill in for User Tasks                               |

Only the first row is generated; everything else is hand-written and therefore
needs the full two-question treatment ([README.md](./README.md), the
doctrine).

## The generated part: it cannot drift, only go stale

Here is the free lunch of this layer. Java's stub and our client are both
generated **from the same contract file**. Their method lists physically
cannot differ — neither SDK owns the list; the contract does. So for the RPC
surface, "parity" needs no enumeration and no comparison against Java. The
only possible failure is **staleness**: the contract gains a field or a
method, Java's auto-rebuilt snapshot picks it up, and our checked-in snapshot
lags behind.

What staleness looks like, concretely: suppose the contract gains a field,
and Java starts writing it into a **golden fixture** — one of the checked-in
reference files our tests compare against, produced by running real sdk-java
(the wfsdk's whole oracle; see [wfsdk.md](./wfsdk.md)). The fixture now says
`"retryPriority": 5`, and our older generated code has never heard of that
name. Many parsers would silently *drop* the unknown field — and then our tests
would keep passing while we were blind to it. So the golden loader parses
strictly:

```ts
// harness/golden.ts (the helper that reads fixture files) — if a fixture
// mentions a field our generated code doesn't know, this THROWS, and the
// suite goes red with "unknown field".
PutWfSpecRequest.fromJsonString(json, { ignoreUnknownFields: false })
```

Stale codegen becomes a crash instead of a silent shrug. Two more guards
complete the story:

- **A regenerate-and-compare gate in CI** — the automatic checks every
  proposed change must pass before merging — (planned): rerun the proto code
  generator and fail if the output differs from what is checked in. Unlike the
  tripwire — which only fires when a fixture happens to carry the new field —
  this catches contract drift unconditionally.
- **A three-line meta-test** (planned): loop over the generated method list
  and assert every declared RPC is callable on our client. The loop already
  exists in `client.ts` for another purpose; the test is the same loop with an
  assertion added.

## The hand-written part

**`LHConfig` — the settings.** Where the SDK learns which server to call
(host and port), which **tenant** to act in (a tenant is an isolated workspace
on a shared server, keeping different teams' workflows apart), whether to
encrypt the connection (**TLS**) and with which certificates, how to log in,
and connection keep-alive tuning. Settings come from environment variables,
properties files, or in-memory maps, merged by a builder in call order (later
sources win) — mirroring Java's config builder.

**`client.ts` — the wrapper.** Two jobs. First, it turns stub calls into
plain Promises, so every RPC is a one-line `await`. Second, it fixes a real
usability gap found by the integration tier (the suites that run against a
real server — [integration.md](./integration.md)): our generated code demanded
that every list- and map-typed field be present on every request, and omitting
one crashed deep inside serialization — the packing of a request into bytes —
with an unhelpful error, while Java's builders happily default missing
fields. The wrapper now accepts partial
requests and fills in the blanks, restoring what Java users take for granted.

**`grpcRetry.ts` — polite retry.** When the server answers "resource
exhausted" (busy), the wrapper waits and retries — honoring the wait time the
server itself suggests when it provides one. Errors generated locally (for
example, a reply bigger than the configured size limit) are never retried,
because retrying cannot fix them.

**`common/oauth.ts` — machine login.** Services don't type passwords; they
present a **client id and secret** to an identity provider (the login
service — Keycloak, in our tests) and receive a short-lived **token**,
which rides along on every RPC. Our implementation
fetches the token, caches it, refreshes it shortly *before* it expires (so it
never dies mid-call), and collapses concurrent refreshes into a single
request. It is proven against a **real** identity provider (Keycloak), not a
mock — see [integration.md](./integration.md) for why that distinction
matters.

**`common/typeAdapters.ts` — custom types.** If a user stores a type the SDK
doesn't know (a `Money`, a `Decimal`), an adapter lets them define its
encoding. One deliberate asymmetry, recorded so nobody "fixes" it: adapters
apply automatically when **writing**, but reading stays with the built-ins
unless the caller asks for an adapter by name. A stored value carries no label
saying which adapter produced it — auto-applying on read would mean guessing,
and a wrong guess silently returns the wrong type.

## The shared translator: serde

"Serde" = **ser**ialize / **de**serialize: converting a JavaScript value to
the wire format (the protobuf encoding from the contract, above) and back. There is exactly **one** serde implementation in the
SDK (`common/serde.ts`), used by the wfsdk and the worker alike —
deliberately, so the two can never disagree about encoding.

Why bytes matter here: two SDKs can each "work" on their own while encoding
the same value differently — and then data written by one is quietly corrupted
when read by the other. So the oracle is a fixture of 24 representative values
**as sdk-java encodes them** (`golden/fixtures/serde.json`), which our
encoding must match byte-for-byte. Building against it caught two divergences
no JS-only test could have seen:

- **Java drops `null` object fields — recursively — but keeps nulls inside
  arrays.** Plain `JSON.stringify` does neither, so a custom serializer
  exists.
- **Guessing a value's type from its shape is unsafe.** An early version
  recognized workflow-run IDs structurally, and would have encoded the very
  common plain object `{id: 'abc'}` as one — silently discarding every other
  field. Storing a run ID now requires saying so explicitly.

One known gap, still open: the *decoder* has no case for the native typed
`ARRAY`/`MAP` value kinds, so such values round-trip through the server
correctly but come back as `undefined`. Found by running the sample programs in `examples/js/`; tracked
in [roadmap.md](./roadmap.md). It is also this family's favorite cautionary
tale: the serde checklist was written by hand once, missed two kinds, and
every consumer inherited the blind spot — the exact failure mode the derived
checklists elsewhere in this system exist to prevent.

## User Task forms

User Tasks are workflow steps a *human* completes — an approval, a form. The
server needs to know the form's fields and types. Java describes them with an
annotated class; JS has no annotations, so we describe them with **zod** — a
small library for describing data shapes in code ("this field is a string,
that one an integer"), and the SDK's standard stand-in wherever Java would use
a class to describe a type — plus display labels. Fields must be primitives
(plain single values: text, numbers, booleans) that a form can render; struct
and collection fields are rejected outright rather than silently becoming
JSON blobs.

## How this layer is tested today

- **33 matrix entries** (22 config/client, 7 serde, 4 usertask). The matrix
  is this family's parity suite — one test per Java capability, each naming
  that capability in its title, so running the suite is reading the status
  ([README.md](./README.md)).
- **Behavior evidence:** unit tests against scripted responses for the
  wrapper, retry, and OAuth logic; the serde fixture for encoding; and the
  real-infrastructure suites — an actual TLS handshake and an actual identity
  provider — for the parts where simulation proves too little
  ([integration.md](./integration.md)).
- **Scope boundary, stated plainly:** the freshness check shipped for the
  wfsdk ([wfsdk.md](./wfsdk.md), Design 1) covers the wfsdk's 21 types only.
  The client-layer classes currently sit under **no** coverage-completeness
  check — nothing fails today if Java's `LHConfig` grows a method we never
  noticed. Closing that is the plan below.

## The plan

In order of cheapness:

1. **The config-key comparison.** Both SDKs read settings from named
   environment variables, and both lists are extractable constants — so a test
   can compare them. This mechanizes a real bug the audit found ([audit.md](./audit.md) #5): Java reads
   worker settings from `LHW_`-prefixed names (`LHW_TASK_WORKER_ID`, …), but
   sdk-js invented `LHC_`-prefixed spellings — so a Java user migrating a
   working config to JS would have those settings **silently ignored**. The
   wrong spellings never shipped, so the fix is a straight rename; the check
   then makes the mistake unrepeatable. One subtlety: assert *containment plus
   an allowlist*, not equality — sdk-js legitimately has one key Java lacks,
   and every deliberate divergence should be a written entry with a reason,
   not a cause to weaken the check. (Same pass: decide the default for in-flight tasks — how many tasks a
   worker executes at once. Ours is 8, Java's is 2, and Java should win as
   the stated gold standard.)
2. **The regenerate-and-compare CI gate and the meta-test** described above —
   the staleness guards for the generated part.
3. **Extend the freshness check to this layer** — add Java's `LHConfig`,
   `LHConfigBuilder`, and the auth classes to the shipped machinery
   (`SurfaceGenerator.java`'s class list, the scope of the citation parser —
   the tool that reads the `— Java: …` tags out of our test titles — and a
   small exemption list). One prerequisite the wfsdk didn't have: several
   config-area test titles cite prose rather than symbols (`— Java:
   common/auth`, `LHConfigBuilder source ordering`, slash-composites like
   `getWorkerThreads/getInflightTasks`). Those titles need normalizing to
   `Class#method` form — or alias entries — before a set difference can run
   clean.

Judgment residue for this layer, kept in writing: the config-key allowlist.
