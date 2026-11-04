# Conformance quickstart

A quick guide for familiarizing yourself with the conformance suite.
Background and the full contract live in [README.md](./README.md).

## Run the suite

Build every testee, then run everything:

```bash
node sdk-conformance/runner/build.mjs
```

```bash
node sdk-conformance/runner/suite.mjs
```

The build command runs each SDK's own build (gradle for java, npm for js),
as registered in `testees.json`.

## Break it on purpose

1. Open `sdk-js/src/wfsdk/WorkflowThread.ts` and find the
   `NODE_NAME_SUFFIX` table. Change `task: 'TASK'` to `task: 'TASKK'`.
2. Rebuild the testees with `node sdk-conformance/runner/build.mjs`
3. Run `node sdk-conformance/runner/suite.mjs`.

Every js case that compiles a task node now fails, each with a readable
diff showing the wrong node name, and the run exits nonzero. Java's
column stays green, because each SDK is graded independently against the
frozen canon. Revert the change, rebuild, rerun, and everything is green
again.

The same works in reverse. Break something in sdk-java and Java's own
column goes red, because the reference takes the same exam.

## Add a feature

When a new feature tracked by the `sdk-conformance/areas` is added, the
essence of that feature must be captured as a `case` in the suite. The
coverage ratchet forces this. A new public method in the reference SDK
with no case and no written exemption fails `freshness.mjs`.

1. Build the feature in sdk-java's wfsdk as usual.
2. Add a probe pair for it in `sdk-java/conformance/.../WfsdkArea.java`,
   one minimal workflow built once without the feature (base) and once
   with it (feature).
3. Add the case entry to `sdk-conformance/areas/wfsdk/manifest.json`
   (stable id, title, level, the `covers` capability keys) and a short
   `cases/<id>/scenario.md` saying what to build.
4. Regenerate the canon with
   `node sdk-conformance/runner/mint.mjs`
   The fixture diff in your PR is the review surface.
5. Implement the same case in the other testees (for js that is
   `sdk-js/conformance/src/wfsdkArea.ts`). Any SDK that cannot pass yet
   gets a `todo` line in its `ledgers/<sdk>.yaml`, in the same PR, so the
   suite lands green and the debt is visible.
6. Run `node sdk-conformance/runner/suite.mjs`.
