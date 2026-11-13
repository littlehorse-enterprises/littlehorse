# littlehorse-derive

Derive macros used through the `littlehorse` Rust SDK. Consumers normally depend
on `littlehorse` and import its re-exported `LittleHorseValue` derive macro.

The macro derives native value conversion and struct schema definitions. Structs
use `#[lh(name = "schema-name")]`; fields support `rename`, `masked`, and
`description`. The minimum Rust version is 1.89, shared with the SDK.

Apache-2.0. Publishing this package precedes publishing its matching SDK version.
