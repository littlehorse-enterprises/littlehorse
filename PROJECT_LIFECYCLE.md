# LH Server Compatibility and Release Policy

LittleHorse is used in mission-critical systems at a multitude of companies. Such companies need a clear understanding of how stable the API's they are using will be.

This Proposal defines what changes will be legal in Major, Minor, and Patch releases in the LittleHorse project after the 1.0 release, including our release cadence, deprecation strategy, and feature support levels. The API we expose to our customers includes our Task Worker SDK's, the WfSpec SDK's, and also our GRPC clients.

## Our Compatibility Promise

[Semantic Versioning](https://semver.org) is an industry standard for versioning, in which:

* **Major Releases** contain breaking changes.
* **Minor Releases** contain new functionality with no breaking changes.
* **Patch Releases** contain only bug fixes and no new functionality.

Client Code is defined as code written by a user using our SDKs in Java, Go, Python, or C#. The "Server" is defined as the version of the physical LittleHorse Cluster that the Client Code talks to. Note that we release the SDKs and the Server together.

We will follow Semantic Versioning with an eye towards the following goals:

* Client Code can upgrade the SDK to new Patch or Minor versions without requiring changes to the code (with the exception of some edge cases in GoLang).
* Client Code is not guaranteed to work if the SDK Minor Version is newer than the Server Minor Version.
* Client Code will continue to work with a Server up to one Major Version newer than the SDK Version.
* Client Code is not guaranteed to work with a Server that is two or more Major Versions newer than the Client.

### Release Cadence

Our release plan will be as follows:
* We will have a minimum of two months and maximum of five months between minor releases. As we gain experience with this release schedule, we may refine this number.
* We will maintain one long-term-support (LTS) branch at a time, which will receive patches. LTS branches will be supported for a minimum of one year and maximum of two years before a new minor release is chosen as an LTS branch.
* In the event of a Major Release, the last minor release before the Major Release will automatically become the LTS release.

### Implementing Semantic Versioning

Implementing the above is a detailed and nuanced effort, and we will provide details in the rest of this document. However, the implementation:

* **Patch Releases** contain no changes to the protobuf schemas but may contain non-breaking additions to the SDK code.
* **Minor Releases** contain backwards-compatible additions to the protobuf schemas and SDK's.
* **Major Releases** will contain breaking changes or removals to protobuf schemas and SDK code.

It is worth noting that allowing non-breaking additions to SDK code in a patch release is a slight deviation from true Semantic Versioning. However, this is reasonable given the fact that we currently release four SDK's together with the Server. With a time-based release plan, it might not be possible to fully implement a new client-side feature in all four SDK's before the deadline for a Minor release. Allowing missing features (already implemented in the protobuf schemas and the Server) to be added to an SDK in a Patch release prevents users from having to wait up to 5 months for all of our SDK's to reach parity. Furthermore, this relaxation of the SemVer rules will come in handy if we add new SDK's in the future, as it will allow us to build out new features quickly during Patch releases.

It is also important to note that Minor releases may require small updates to GoLang code which directly uses our protobuf messages in case that we move a field into a `oneof`, make a field `optional`, or remove the `optional` descriptor from a field. This is an unfortunate result of how GoLang protobuf structures are compiled.

## Defining Our API

Our API consists of two important components, in descending priority order:

1. The Protobuf API: everything defined in the [`schemas/littlehorse`](https://github.com/littlehorse-enterprises/littlehorse/tree/master/schemas/littlehorse) directory.
2. Our `WfSpec` and Task Worker SDK's in Java, Go, Python, and C#. This is all of the hand-written code (not auto-generated from our protobuf and grpc schemas) shipped in our SDK's.

Avoiding breaking changes in protobuf is absolutely critical because it is part of our public API and also because the Server itself stores data in the protobuf format on disk.

Note that we exclude the dashboard from decisions around semantic versioning in the LittleHorse project because:

* Most users will not write code that depends on the dashboard itself, so changing its behavior will be much less likely to break customer automations.
* We have a "monorepo," and coordinating development across two very distinct projects is too difficult.

### Allowed Protobuf Changes

The following protobuf changes will be allowed and considered non-breaking:

* Adding fields, whether `optional` or regular.
* Moving a field into a `oneof`.
* Adding or removing the `optional` modifier from a field if the field is not depended on by clients and doing so does not break our end-to-end tests.

The following changes should only be done in Major releases:

* Making a _commonly-used_ field `optional`, in a way that a client might be broken by expecting it to always be set when it is not actually set.
* Removing an `optional` field from the schemas.

> __NOTE__: For a detailed breakdown of protobuf compatibility and evolution rules, check out this helpful [tech talk on YouTube](https://www.youtube.com/watch?v=Z0NM62ZeNN4) as well as the [official protobuf documentation](https://protobuf.dev/programming-guides/proto3/#updating).

### Experimental Features

The LittleHorse Philosophy is to build new features quickly and get feedback from users. Sometimes, responding to that initial feedback requires a non-compatible change to the initial feature. This necessitates the concept of an "Experimental" feature, which is not considered part of the stable API. An _Experimental_ feature is subject to breaking changes or even removal in Minor releases.

By default, all new features will be marked as _Experimental_ for the first Minor release after they are released, and will graduate to _Generally Available_ in the subsequent Minor (or Major) release. However, we may indicate that certain features remain _Experimental_ for more than one Minor release as necessary and depending on the complexity of the specific feature.

As we gain more experience with this release strategy, we will introduce additional levels of feature stability beyond just "Experimental" and "Generally Available" if and only if required.

### Testing Server Compatibility

We already have an extensive suite of automated end-to-end tests which run on every commit. In order to codify compatibility, we will:

* Publish an artifact that contains the _client-side_ code for the end-to-end test suite (for all non-experimental features) with each Minor Release. This will be called a "Regression Suite."
* Run every Regression Suite from the current and previous Major Version against the newest Server release candidate.

In that way, we will have an automated way of enforcing that we can upgrade a Server without impacting Client Code within one Major Version's difference of the Server.

Because this is intended to test Server compatibility, testing using our Java artifacts is sufficient. 

### Deprecation Policy

Eventually, we will have to make breaking changes to prevent our API from looking like a kitchen sink. However, we will only perform a deprecation if:

* Supporting the old feature causes significant complexity from the maintainer's side.
* There is a new alternative and supporting both code paths indefinitely impacts the cleanliness of the _new_ API.

Even in Major releases, we will do our best to avoid gratuitous renamings or refactors or removals that do not have significant reason, even if the "new way" is indeed better.

Any Minor release can include a deprecation notice, but features cannot be removed until a Major release. Note that Server-side deprecations and removals are more strict than Client-side deprecations, as a Server must support Clients from the previous Major version.

### Protobuf Changes in Java, C#, Go, and Python

In Python, Java, and C# we are able to:
* Add the `optional` descriptor to a field, or
* Move a field (regular or `optional`) into a `oneof`,

without affecting user code. This means that the above changes can occur without users having to update their code. Unfortunately, this is untrue in GoLang, as minor code changes will be required. We will call out these changes in our release notes. This will be a slight deviation from pure "Semantic Versioning."

## For the Lawyers

This document reflects our goals as an open-source project. It does not reflect a legally-binding commitment on behalf of any contributors to the open-source `littlehorse` project, nor on behalf of LittleHorse Enterprises LLC, to maintain compatibility in our products in any way.
