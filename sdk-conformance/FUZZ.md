# Random dual-compile (fuzz)

A report-only cross-check that composes builder calls randomly and
requires every SDK to compile the same random workflow to the same proto.
It has no canon. With a shared seed, each testee generates the identical
call sequence independently, and the runner compares the SDKs' outputs to
each other. Divergence means one builder's composition behavior drifted in
a way no hand-written case anticipated.

Two numbers control a run, seeds and ops.

- A seed is the starting value for the random number generator. The
  generator is deterministic, so the same seed always produces the same
  sequence of draws. Give every SDK seed 7 and each one independently
  builds the exact same "random" workflow, named `fuzz-7`. One seed means
  one workflow, so running seeds 1..20 tests 20 distinct workflows.
- Ops is how many random build steps make up each workflow. Each op is
  one draw from the table below, such as declaring a variable, running a
  task, sleeping, or waiting for an event. More ops means longer, more
  tangled workflows, which stresses how builder calls compose.

So `fuzz.mjs 50 16` builds 50 different workflows of 16 random steps
each, and requires every SDK to compile all 50 identically.

It is not a corpus area. There are no fixtures, no manifest, and no
ledgers. Run it with:

```
node sdk-conformance/runner/fuzz.mjs            # seeds 1..20, 12 ops each
node sdk-conformance/runner/fuzz.mjs 50 16      # 50 seeds, 16 ops
node sdk-conformance/runner/fuzz.mjs --register # also register each generated
                                                # workflow with the server at
                                                # LHC_API_HOST/PORT and require
                                                # acceptance (canon validation)
```

`--register` extends the testee verb with a trailing `--register` flag.
The testee must register the compiled workflow (creating the fixed
`task-<n>`, `branch-<n>`, and `evt-<n>` defs the op table references if
absent) and exit nonzero if the server rejects it. The flag is optional,
and only the js testee implements it today.

## The generator contract (normative)

Testees implement one verb:

```
testee fuzz --seed <uint32> --ops <n>
    Generate the op sequence below, apply it to a workflow named
    fuzz-<seed>, and print the compiled PutWfSpecRequest as proto JSON.
```

The PRNG is mulberry32(seed). Each draw updates 32-bit unsigned state `a`:

```
a = (a + 0x6D2B79F5) | 0
t = a ^ (a >>> 15); t = imul(t, 1 | a)
t = (t + imul(t ^ (t >>> 7), 61 | t)) ^ t
draw = ((t ^ (t >>> 14)) >>> 0)          // uint32
nextInt(bound) = draw % bound
```

For each `i` in `0..ops-1`, draw `k = nextInt(8)` and apply the op:

| k | op (exactly these draws, in this order) |
| --- | --- |
| 0 | `declareInt("v<i>")` |
| 1 | `declareStr("v<i>")` |
| 2 | `declareBool("v<i>")` |
| 3 | `execute("task-<nextInt(5)>")` |
| 4 | `sleepSeconds(1 + nextInt(60))` |
| 5 | `waitForEvent("evt-<nextInt(5)>")` |
| 6 | if any int variable exists, `mutate(intVars[nextInt(len)], ADD, nextInt(10))`, else `declareInt("v<i>")` with no draws |
| 7 | if any int variable exists, `doIf(condition(intVars[nextInt(len)], GREATER_THAN, nextInt(10)))` with a body that runs `execute("branch-<nextInt(5)>")`, else `execute("task-<nextInt(5)>")` |

`intVars` is the list of int variables in declaration order. Draw order is
normative. A conditional branch that skips a draw must skip it in every
SDK.
