# Feature Stability

The LittleHorse Council cares deeply about making LittleHorse a user-friendly, intuitive, performant, reliable, and secure platform for our users. We also want our users to be able to depend on the public API's we provide, such that upgrading from one version of LittleHorse to the next is non-disruptive.

However, we also note that LittleHorse is a young project, and we are still learning about the usage patterns as early adopters give us feedback on how they use the platform. Therefore, we want to be able to put in front of users certain features at various levels of maturity while also being fully clear and transparent about how stable each API is.

This document defines the policy for LittleHorse API Stability, which is based on [Semantic Versioning](https://semver.org).

## Semantic Versioning Policy

LittleHorse follows the rules of [Semantic Versioning](https://semver.org). According to Semanatic Versioning, any pre-`1.0.0` project may define their own compatibility policy, but rules for any release after `1.0.0` are well-documented and well-defined.

As a refresher, the rules for a project **after the `1.0.0` release** are:

* A patch release `Z` (`x.y.Z`) is for backwards-compatible bug fixes only, with no API changes.
* A minor release `Y` (`x.Y.z`) introduces new features in a backwards-compatible manner.
* A major release `X` (`X.y.z`) must be incremented if any backwards-incompatible changes are made.

_After we release `1.0.0`,_ we will provide 12 months of patch support for every minor version. This is equivalent to other major projects such as Kubernetes and Apache Kafka.

### Policy Prior to `1.0.0`

Semantic Versioning allows projects in the pre-`1.0.0` phase to define their own compatibility rules. For LittleHorse, they are as follows:

* A patch release `Z` (`0.y.Z`) is for backwards-compatible improvements and bug-fixes.
* A minor release `Y` (`0.Y.z`) allows for breaking changes to `BETA` or `EXPERIMENTAL` features.
* No breaking changes to `STABLE` features are allowed without a deprecation notice of 6 months and a viable migration path.

## Maturity Levels

We classify each feature in LittleHorse according to the following maturity levels.

### `EXPERIMENTAL`

An `EXPERIMENTAL` feature is, quite simply, experimental. It is under active development with no promises made about API stability, feature completion, or lack of bugs. We also reserve the right to remove an `EXPERIMENTAL` feature without replacement in a future release.

An `EXPERIMENTAL` feature should not be used in a production system, but the LittleHorse Council appreciates and requests feedback from adventurous testers.

### `BETA`

A `BETA` feature has proven its value and passed through the initial development phase. We do not plan to make any breaking changes to the API's presented by this feature. The `BETA` feature has unit tests, and if possible, automated end-to-end tests in our pipeline. The `BETA` feature is supported by all `STABLE` sdk's (as of this writing: Java).

However, the `BETA` feature is still new and as such there might be _unforeseen_ breaking changes as we learn more about how the feature is used and what implications it has for the rest of the system. Additionally, the feature may lack support in some `BETA` sdk's.

It is generally ok to use a `BETA` feature in production; if you do, please give us heads up on our [Community Slack](https://launchpass.com/littlehorsecommunity) and we will do our best to ensure that you have a good experience with the feature. In the unlikely and unfortunate event that we do need to make a breaking change, we will work with you to provide a minimally painful migration experience.

### `STABLE`

A `STABLE` feature has been proven in battle and the LittleHorse Council and LittleHorse Community believe that the API is not going to change. To qualify for the `STABLE` label, a feature must have been `BETA` for at least six months without a breaking change. All behaviors promised in our documentation must have a corresponding test (unit or end-to-end), and the feature must be supported by all `BETA` or `STABLE` sdk's (as of this writing: Java, Go, Python).

## Road to `1.0.0`

Our commitments from `1.0.0` onward are significant, so we want to ensure that we can meet them.

### Features to Prototype

Before we release `1.0.0`, we need to be fully confident that our internal architecture can support all of the new features on our roadmap. Therefore, we want to implement prototypes of the following features to ensure that there are no red flags that cause headaches when we attempt to extend our feature-set. We do not anticipate any troubles, but we take guarantees seriously, so we opt to be more conservative.

Before reaching `1.0.0`, we want to reach `EXPERIMENTAL` support for the following features:

* Schemas on `TaskDef` outputs.
* Schemas on `ExternalEventDef` content.
* Allow a `ThreadRun` to return an output on completion.
* Allow creating and waiting for child `WfRun` as a `Node` in a `WfSpec`.
* Counted `Tag`s, which enable queries such as "how many `WfRun`s are `RUNNING` right now?"
* Intelligent read/write locking for `WfRunVariable`'s at runtime.
* Support compound conditionals without nesting `NOP` nodes.
* Pushing notification events to a Kafka topic for user consumption on a per-Tenant basis.
* Quotas on a per-`Principal` and per-`Tenant` basis.

### Areas to Harden

There are currently some known weaknesses in the LH Server that we wish to harden before releasing `1.0.0`. They are as follows:

* Implement phased deletion of `NodeRun`s for very large `WfRun`'s (10k+ `NodeRun`'s).
* Smartly and transparently change the Index type on a field at runtime.
* Phased bulk operations, such as deleting `WfRun`'s in bulk.
* Bound the memory usage of `TaskQueueManager`.
* Bring mTLS support out of `EXPERIMENTAL` (either dropped or promoted to `BETA`).
* Harden `waitForCommand` internal RPC support during a Kafka Streams rebalance.

### Areas to Stabilize

There are currently some features in the `EXPERIMENTAL` or `BETA` phases which we want to bring to `STABLE` before releasing `1.0.0`:

* `WfSpec` version migration (currently `EXPERIMENTAL`)
* OAuth support (`BETA`)
* Multi-Tenancy (`EXPERIMENTAL`)
* ACL's (`EXPERIMENTAL`)
* Compatibility rules for new `WfSpec` versions (`EXPERIMENTAL`)
* `Variable` search (`EXPERIMENTAL`)

### Timeline

The earliest we can release `1.0.0` is in July of 2024.

We plan to move OAuth 2.0, Multi-Tenancy, and ACL's to `BETA` by EOY 2023. Our policy then requires a 6-month stabilization period before moving those features into `STABLE`, which is a requirement for the `1.0.0` release.

A more realistic expectation for the timeline is October 2024, because software always takes more time than you think.
