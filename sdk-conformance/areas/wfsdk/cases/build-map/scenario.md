# build-map

Build `probe-build-map` (R10) exercising `WorkflowThread#buildMap`.
Both variants declare a map variable `v` with STR keys and STR values.
The feature assigns `v` an inline map built with the map builder:
`v.assign(wf.buildMap().put("k", "val"))`, compiled as a `mapBuilder`
assignment.
The base is the nearest do-nothing neighbor; the feature adds exactly the
gesture above, and the fixture diff is its entire effect.
The reference body is in WfsdkArea.java (see rules.md, "Minting"); the
frozen fixtures are the contract. Rules: [../../rules.md](../../rules.md).
