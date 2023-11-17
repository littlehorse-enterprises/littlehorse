# Feature Stability

The LittleHorse Council cares deeply about making LittleHorse a user-friendly, intuitive, performant, reliable, and secure platform for our users. We also want our users to be able to depend on the public API's we provide, such that upgrading from one version of LittleHorse to the next is non-disruptive.

However, we also note that LittleHorse is a young project, and we are still learning about the usage patterns as early adopters give us feedback on how they use the platform. Therefore, we want to be able to put in front of users certain features at various levels of maturity while also being fully clear and transparent about how stable each API is.

This document defines the policy for LittleHorse API Stability, which is based on [Semantic Versioning](https://semver.org).

## Policy After `1.0.0`

After releasing `1.0.0`, we will strictly follow the SemVer rules. As a refresher, any project **in the post-`1.0.0` phase** must adhere to the following rules:

* A patch release increments `Z` (`x.y.Z`) and is for bug fixes only, with no API changes.
* A minor release increments `Y` (`x.Y.z`) and introduces new features in a backwards-compatible manner.
* A major release increments `X` (`X.y.z`) and must be used if any backwards-incompatible changes are made.

Our Public API will be determined by the content of our [documentation](https://littlehorse.dev/docs/overview) prefixed by the `/v1_0` for version `1.0.x`, or `/v2_4` for version `2.4.x`, for example. Before releasing `1.0.0`, our documentation will not be versioned.

_After we release `1.0.0`,_ we will provide 12 months of patch support for every minor version. This is equivalent to other major projects such as Kubernetes and Apache Kafka.

## Policy Prior to `1.0.0`

Semantic Versioning says that for a project in the pre-`1.0.0` phase, "anything may change at anytime." It is intended for projects in the early development stages. At LittleHorse, we are no longer in initial development, but we have many new features that we are still trying out and learning about as we get feedback from our users.

We wish to clearly communicate expectations about what might change and what is stable, and we also wish to be able to quickly add new features and get feedback from our users. As such, we will adhere to the following guidelines prior to `1.0.0`:

* Features will be classified by their maturity level: `EXPERIMENTAL`, `BETA`, or `STABLE`.
* A patch release `Z` (`0.y.Z`) is for backwards-compatible improvements or bug-fixes without any breaking changes.
* A minor release `Y` (`0.Y.z`) allows for breaking changes to `BETA` or `EXPERIMENTAL` features.
* No breaking changes to `STABLE` features are allowed without a deprecation notice of 12 months and a viable migration path.

### Maturity Levels

We will use the [Stability Status](./STABILITY_STATUS.md) document to keep track of the maturity level of every feature.

#### `EXPERIMENTAL`

An `EXPERIMENTAL` feature is, quite simply, experimental. It is under active development with no promises made about API stability, feature completion, or lack of bugs. We also reserve the right to remove an `EXPERIMENTAL` feature without replacement in a future release.

An `EXPERIMENTAL` feature should not be used in a production system.

#### `BETA`

A `BETA` feature has proven its value and passed through the initial development phase. We do not plan to make any breaking changes to the API's presented by this feature. The `BETA` feature has unit tests, and if possible, automated end-to-end tests in our pipeline. The `BETA` feature is supported by all `STABLE` sdk's (as of this writing: Java).

However, the `BETA` feature is still new and as such there might be _unforeseen_ breaking changes as we learn more about how the feature is used and what implications it has for the rest of the system. Additionally, the feature may lack support in some `BETA` sdk's.

It is generally okay to use a `BETA` feature in production; if you do, please give us heads up on our [Community Slack](https://launchpass.com/littlehorsecommunity) and we will do our best to ensure that you have a good experience with the feature. In the unlikely event that we do need to make a breaking change, we will work with you to provide a minimally painful migration experience.

**We take breaking changes to `BETA` features very seriously and use a case-by-case approach with input from users in the community.**

#### `STABLE`

A `STABLE` feature has been stable for some time and the LittleHorse Council believes that the API is not going to undergo breaking changes anytime soon. All behaviors promised in our documentation must have a corresponding test (unit or end-to-end), and the feature must be supported by all `BETA` or `STABLE` sdk's (as of this writing: Java, Go, Python).

A `STABLE` feature can be used in production without any worry.

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

We hope to release `1.0.0` sometime between March 2024 and November 2024.
