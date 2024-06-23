---
sidebar_label: Sleeping
---

# Sleeping

You can make a `ThreadRun` go to sleep for a period of time by using a `SLEEP` Node. This `Node` type has no output, and simply holds a `ThreadRun` in place until the time expires.

You may specify the time by using a `VariableAssignment` that resolves to one of the three things:

- `sleepSeconds`: An `INT` Value determining how many seconds to sleep for.
- `isoDate`: A `STR` value representing a timestamp in ISO format.
- `timestamp`: A `INT` value representing the number of milliseconds since the epoch.
  - (Don't worry, in LittleHorse the `INT` is a 64-bit number.)

Note that when a `ThreadRun` reaches a `SLEEP` Node, the status of the `ThreadRun` remains `RUNNING`. Additionally, the `ThreadRun` is immediately interruptible (unless, of course, it has a non-interruptible Child Thread).
