# The Registrations Rules

The recipe for the registrations area: the paperwork a workflow files
BESIDES its WfSpec — the side-registration protos and the required-names
sets. As everywhere, **the fixtures are the authority**; this prose
restates them. A divergence here never errors: events silently stop
matching across SDKs.

## G1 — the answer document

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

Ordering is normative: def lists sorted by `name`, name lists sorted
lexically — the underlying collections are sets, and unordered answers
would make equality accidental. Proto entries follow R11 (defaults
emitted, semantic comparison).

## G2 — registrations hydrate at compile time

The document is read AFTER compiling the workflow: registration is a
by-product of compilation (the reference sends these protos immediately
before the WfSpec, which is why they must agree across SDKs).

## G3 — the denominator

`surface.json` lists the capabilities whose observable output is this
document: the four registration gestures and the six accessors. Reflection-
verified at mint.

## G4 — payload types across languages

Java declares payloads as classes (`registeredAs(String.class)`); the JS
SDK declares them as zod schemas (`registeredAs(z.string())`). Both must
produce the same `contentType`. The canon pins Java's encoding.

## G5 — cases are probe pairs

Inherited unchanged from the wfsdk rules: same workflow name in both
variants, base is the nearest do-nothing neighbor, base and feature
documents must differ.

## Minting

This area's canon is minted by the reference testee's registrations
module,
[`RegistrationsAreaMint.java`](../../../sdk-java/conformance/src/main/java/io/littlehorse/sdk/conformance/RegistrationsAreaMint.java),
which mints from the case definitions in `RegistrationsArea.java` — the
same ones that answer the exam's `registrations` verb — so the answers and
the canon can never drift. Regenerate — only ever inside a PR — with:

```
./gradlew :sdk-java-conformance:installDist
sdk-java/conformance/build/install/sdk-java-conformance/bin/sdk-java-conformance mint sdk-conformance
```
