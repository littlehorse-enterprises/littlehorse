# WfSpec Versioning

A `WfSpec` is a versioned resource. Each `WfSpec` is uniquely identified by its `name` (a String), its `majorVersion` (an auto-incremented number, managed by LittleHorse), and its `revision` (another auto-incremented number).

When you create a `WfSpec` with the same `name` as another previous `WfSpec`, LittleHorse will either increment the `revision` (if there are no "breaking changes") or increment the `majorVersion` and set `revision` to zero (if there _are_ "breaking changes"). A "breaking change" in this regard is defined as changing either:

* The set of required input variables to the `WfSpec`, or
* The set of indexed searchable variables in the `WfSpec`.

When you run a `WfSpec` (thus creating a `WfRun`), you may optionally specify the version of the `WfSpec` that you wish to run. If you specify a specific version, then LittleHorse will run the `WfSpec` specified by the `RunWf` request. If no version number is provided, then `LittleHorse` will automatically run the latest version of the `WfSpec` with the provided name. For instructions on how this works in practice, please check out our [Metadata Management docs](/docs/developer-guide/grpc/managing-metadata).

This versioning scheme allows you to improve the business logic of your `WfSpec` without changing the client code that invokes the `WfSpec`: all your clients need to do is specify the `name` of their `WfSpec`, and the latest logic will be run transparently. Alternatively, you can "pin" your clients to run a specific version of your `WfSpec`.

Once a `WfRun` is launched with the `WfSpec` specified by (`name` "foo", `version` 123), the `WfRun` will always be associated with that specific version. In other words, deploying a new version of a `WfSpec` does not affect already-running `WfRun`s.

Future versions of LittleHorse will add an optional "on-the-fly" upgrade mechanism.
