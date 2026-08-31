# sleep-seconds

Build a workflow named `probe-sleep-seconds` whose entrypoint thread does
nothing else.

- **base**: the empty thread — entrypoint wired straight to exit.
- **feature**: the same thread with one call that pauses it for a literal
  30 seconds (sdk-java: `wf.sleepSeconds(30)`).

The delta must be exactly one SLEEP node carrying the literal as
`sleep.rawSeconds.literalValue.int`, spliced between entrypoint and exit.
