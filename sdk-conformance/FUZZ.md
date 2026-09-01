# Random Dual-Compile (fuzz)

A report-only cross-check that composes builder calls **randomly** and
requires every SDK to compile the same random workflow to the same proto.
It has no canon: with a shared seed, each testee generates the identical
call sequence independently, and the runner compares the SDKs' outputs to
each other. Divergence means one builder's composition behavior drifted in
a way no hand-written case anticipated.

Not a corpus area — no fixtures, no manifest, no ledgers. Run with:

```
node sdk-conformance/runner/fuzz.mjs            # seeds 1..20, 12 ops each
node sdk-conformance/runner/fuzz.mjs 50 16      # 50 seeds, 16 ops
node sdk-conformance/runner/fuzz.mjs --register # also register each agreed
                                                # workflow with the server at
                                                # LHC_API_HOST/PORT and require
                                                # acceptance (canon validation)
```

`--register` extends the testee verb with a trailing `--register` flag: the
testee must register the compiled workflow (creating the fixed `task-<n>`,
`branch-<n>`, and `evt-<n>` defs the op table references if absent) and exit
nonzero if the server rejects it. Optional — only the js testee implements
it today.

## The generator contract (normative)

Testees implement one verb:

```
testee fuzz --seed <uint32> --ops <n>
    Generate the op sequence below, apply it to a workflow named
    fuzz-<seed>, and print the compiled PutWfSpecRequest as proto JSON.
```

PRNG: **mulberry32(seed)** — for each draw, with 32-bit unsigned state `a`:

```
a = (a + 0x6D2B79F5) | 0
t = a ^ (a >>> 15); t = imul(t, 1 | a)
t = (t + imul(t ^ (t >>> 7), 61 | t)) ^ t
draw = ((t ^ (t >>> 14)) >>> 0)          // uint32
nextInt(bound) = draw % bound
```

Op sequence: for `i` in `0..ops-1`, draw `k = nextInt(8)`, then:

| k | op (exactly these draws, in this order) |
| --- | --- |
| 0 | `declareInt("v<i>")` |
| 1 | `declareStr("v<i>")` |
| 2 | `declareBool("v<i>")` |
| 3 | `execute("task-<nextInt(5)>")` |
| 4 | `sleepSeconds(1 + nextInt(60))` |
| 5 | `waitForEvent("evt-<nextInt(5)>")` |
| 6 | if any int variable exists: `mutate(intVars[nextInt(len)], ADD, nextInt(10))`; else `declareInt("v<i>")` **with no draws** |
| 7 | if any int variable exists: `doIf(condition(intVars[nextInt(len)], GREATER_THAN, nextInt(10)), body → execute("branch-<nextInt(5)>"))`; else `execute("task-<nextInt(5)>")` |

`intVars` is the list of int variables in declaration order. Draw order is
normative — a conditional branch that skips a draw must skip it in every
SDK.
