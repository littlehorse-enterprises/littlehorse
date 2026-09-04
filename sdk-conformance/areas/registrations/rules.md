# The registrations rules

The recipe for the registrations area. It covers the paperwork a workflow
files besides its WfSpec, meaning the side-registration protos and the
required-names sets. As everywhere, **the fixtures are the authority**,
and this prose only restates them. A divergence here never errors. Events
just silently stop matching across SDKs.

## The answer document (G1)

A testee's answer (and every fixture) is one JSON document:

```json
{
  "externalEventDefs": [ <PutExternalEventDefRequest proto JSON> ],
  "workflowEventDefs": [ <PutWorkflowEventDefRequest proto JSON> ],
  "requiredTaskDefNames": ["..."],
  "requiredExternalEventDefNames": ["..."],
  "requiredChildWfSpecNames": ["..."],
  "requiredWorkflowEventDefNames": ["..."]
}
```

Ordering is normative. Def lists sort by `name` and name lists sort
lexically. The underlying collections are sets, and unordered answers
would make equality accidental. Proto entries follow R11 (defaults
emitted, semantic comparison).

## Registrations hydrate at compile time (G2)

The document is read after compiling the workflow, because registration is
a by-product of compilation. The reference sends these protos immediately
before the WfSpec, which is why they must agree across SDKs.

## The denominator (G3)

`surface.json` lists the capabilities whose observable output is this
document, meaning the four registration gestures and the six accessors.
It is reflection-verified at mint.

## Payload types across languages (G4)

Java declares payloads as classes (`registeredAs(String.class)`), and the
JS SDK declares them as zod schemas (`registeredAs(z.string())`). Both
must produce the same `contentType`. The canon pins Java's encoding.

## Cases are probe pairs (G5)

Same conventions as the wfsdk area. The workflow is named `probe-<id>` in
both variants (wfsdk R10), base is the minimal workflow without the
feature, and the base and feature documents must differ. Freshness
rejects a pair whose two fixtures are identical, because a pair that
toggles nothing proves nothing.

## Minting

This area's canon is minted by the reference testee's registrations
module,
[`RegistrationsAreaMint.java`](../../../sdk-java/conformance/src/main/java/io/littlehorse/sdk/conformance/RegistrationsAreaMint.java),
which mints from the case definitions in `RegistrationsArea.java`, the
same ones that answer the exam's `registrations` verb, so the answers and
the canon can never drift. Regenerate only ever inside a PR, where the
diff is the review surface:

```
node sdk-conformance/runner/build.mjs
node sdk-conformance/runner/mint.mjs
```
