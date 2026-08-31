# sleep-seconds

Build a workflow named `probe-sleep-seconds` (R10) whose entrypoint thread:

- **base**: does nothing — entrypoint wired straight to exit.
- **feature**: makes one call pausing the thread for a literal 30 seconds
  (sdk-java: `wf.sleepSeconds(30)`).

The delta must be exactly one sleep node per R9: named per R1
(`1-sleep-SLEEP`), spliced by R3 between entrypoint and the automatic exit
(R4 — now `2-exit-EXIT`), carrying the literal per R8
(`rawSeconds.literalValue.int: "30"`). See
[../../rules.md](../../rules.md).
