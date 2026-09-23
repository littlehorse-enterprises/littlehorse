# map-put

Build `probe-map-put` (R10) exercising `WfRunVariable#put`.
Both variants declare a map variable `v` with STR keys and STR values.
The feature calls `v.put("k", "val")`: one entry written through the map
variable, compiled as a dynamic-selector mutation on `v`.
The base is the nearest do-nothing neighbor; the feature adds exactly the
gesture above, and the fixture diff is its entire effect.
The reference body is in WfsdkArea.java (see rules.md, "Minting"); the
frozen fixtures are the contract. Rules: [../../rules.md](../../rules.md).
