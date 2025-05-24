# Moving Towards Strong Typing

- [Moving Towards Strong Typing](#moving-towards-strong-typing)
  - [Motivation](#motivation)
    - [Why We Need Typing](#why-we-need-typing)
    - [Defining Strong Typing](#defining-strong-typing)
  - [Proposed Changes](#proposed-changes)
    - [Deprecating Untyped `ExternalEventDef`s](#deprecating-untyped-externaleventdefs)
    - [Deprecating `VariableAssignment.NodeOutputReference`](#deprecating-variableassignmentnodeoutputreference)
      - [Replacing The Functionality](#replacing-the-functionality)
      - [User Impact](#user-impact)
    - [Deprecating `JSON_OBJ` and `JSON_ARR`](#deprecating-json_obj-and-json_arr)
    - [Metrics](#metrics)
  - [Future Work](#future-work)

We decided to move LittleHorse towards a more "strong typing" model rather than the current Wild West YOLO typing model that we have, in which type errors at `WfRun` runtime are quite common.

## Motivation

Unfortunately, we have too many users whose applications depend on existing untyped features, and we do not wish to break current users. For example:
* Users are using `ExternalEventDef`'s without the `content_type` field, so we cannot remove the functionality of "untyped `ExternalEventDef`'s". 
* Some users may be using the `VariableAssignment.NodeOutputReference` capability to directly pass the output of one `NodeRun` into another.
* A multitude of users are using `JSON_OBJ` and `JSON_ARR` and `.jsonPath()`.

This proposal outlines a plan to move LittleHorse _completely_ to Strong Typing so that, by version 2.0, we will be able to validate 

### Why We Need Typing

Right now there are many times where we don't know the types of what we are dealing with:

* When mutating variables after any `Node`.
* When accepting an `INPUT` variable to capture the `ExternalEvent` that triggered an Interrupt.
* When accepting an `INPUT` variable to capture the `Failure` content that triggered an exception handler.
* Anytime a `.jsonPath()` is used.

Those cases yield confusing _`WfRun` runtime_ errors, which have been a sore spot for our users.

All of those cases can be fixed only if we deprecate json variables and enforce strong typing as described above. Furthermore, strong typing as described above can create confusion with the Output Topic as it results in un-schema'ed variables (especially `JSON_OBJ` variables), `ExternalEvent`s, and `WorkflowEvent`s being thrown into the Output Topic.

### Defining Strong Typing

Java and GoLang have different definitions of "strong typing." In LittleHorse, we'll be more similar to Java. What do I mean by this?

Let's look at the `message TypeDefinition`:

```
// Defines the type of a value in LittleHorse. Can be used for Task Parameters,
// Task return types, External Event types, ThreadSpec variables, etc.
message TypeDefinition {
  // The basic type of the value. Will become a `oneof` once StructDef's and Struct's
  // are implemented according to issue #880.
  VariableType type = 1;

  // For compatibility purposes.
  reserved 2, 3;

  // Set to true if values of this type contain sensitive information and must be masked.
  bool masked = 4;
}
```

There is unfortunately no way to signify whether this is _always present_ or _sometimes present, sometimes null._ That's just like an Object Reference in Java: it can be present, or it can be `null`. However, in GoLang, you can specify whether you want something to be _always present_ or _sometimes present_, by choosing a pointer or a raw value. (Yes, there are other differences between pointer and raw value...).

In LittleHorse, we'll be satisfied with the Java style guarantees, where _every value_ can be a value or empty (a `VariableValue` with the `oneof value` not set to anything). If the `VariableValue` is not empty, we _must_ know the type (and, if necessary, the `InlineStructDef` associated with it).

The only exception to this is that in a `ThreadVarDef` (that defines a `Variable` in a `ThreadSpec`/`WfSpec`), you can have a `required` field which means the `Variable` must be passed as input and must not be `NULL` (an empty `VariableValue`).

## Proposed Changes

We will:

1. Provide utilities that facilitate and encourage the registration of `ExternalEventDef` and `WorkflowEventDef` resources _with proper typing information, and then deprecate un-typed `ExternalEventDef` and `WorkflowEventDef`'s in LittleHorse 2.0.
2. Deprecate the `VariableAssignment.NodeOutputReference` in 1.0 and remove it in 2.0.
3. Deprecate `JSON_OBJ` and `JSON_ARR` when `StructDef` is ready (before 1.0), and remove it in 2.0.

### Deprecating Untyped `ExternalEventDef`s

We will first make it possible to register `ExternalEventDef`'s using our `Workflow` DSL. This utility will _only_ support strongly-typed `ExternalEventDef`'s, and will not support `JSON_OBJ` `ExternalEventDef`'s. It will look as follows:

```java
wf.waitForEvent("my-event").registeredAs(String.class);
```

Valid classes in Java will be:
* `String.class`
* `Integer.class` or `Long.class`
* `Boolean.class`
* `Double.class` or `Float.class`
* Any class with the `@LHStructDef` annotation.

Other SDK's will provide equivalent functionality.

We will then make the Server validate all variable mutations on `ExternalEventNode`s and also validate the declarations of the 

### Deprecating `VariableAssignment.NodeOutputReference`

The `VariableAssignment.NodeOutputReference` allows a `VariableAssignment` to take in the name of a `Node`, and resolve to the output of the most recent `NodeRun` for that `Node`.

For example, it is used in the following code:

```java
NodeOutput firstOutput= wf.execute("some-task");

wf.execute("another-task", firstOutput);
```

The current implementation has some problems:

* **Performance**: it involves sequentially iterating through all previous `NodeRun`'s in the 
* **Archival**: we want to add the ability to archive/delete old `NodeRun`s automatically during a `WfRun` to save disk space. Imagine a `WfRun` that executes a loop every 30 seconds indefinitely—we have no need to keep the history forever; it could be useful to delete old `NodeRun`s 24 hours after they execute. Implementing such a retention policy could be complicated with the `NodeOutputReference`.
* **Type Inference**: this behavior has issues with strong typing.

We will:
* Refactor all of our SDK's to implement the same code in a less harmful manner before the 1.0 version.
* Continue supporting `VariableAssignment.NodeOutputReference` in the 1.x versions.
* Fully remove the `VariableAssignment.NodeOutputReference` from the protobuf in the 2.0 version.

#### Replacing The Functionality

We will replace the functionality as follows:

1. Deprecate the `compileWfToJson()` method before 1.0 and remove it in 1.0, to be replaced with a method that requires a `LittleHorseBlockingStub`.
2. Instead of using a `VariableAssignment.NodeOutputReference`, the SDK will transparently:
   1. Fetch whatever information is needed about the previous `NodeOutput` from the LH Server, eg. the `TaksDef` or `ExternalEventDef` which has typing information.
   2. Transparently create an internal variable with the appropriate `TypeDefinition`.
   3. Add a `VariableMutation` on the provided `NodeOutput` that saves the output.
   4. Pass that internal `Variable` in the resulting `VariableAssignment`.
3. The SDK's will throw an `IllegalStateException` (or the language-equivalent) in case an `ExternalEventDef` or `TaskDef` is encountered that lacks typing information.

#### User Impact

Users will simply have to recompile and re-register their `WfSpec`'s with a `1.x` client before upgrading their server to `2.0`. If they have long-running `WfRun`'s (eg. the `WfRun` takes years to complete), the users will need to use the soon-to-be-implemented `rpc MigrateWfSpec` which moves active `WfRun`'s from one version of the `WfSpec` to a newer version of the `WfSpec`.

This means that we will not be able to release LittleHorse 2.0 before implementing `rpc MigrateWfSpec`; otherwise, we risk leaving certain users with no upgrade path.

### Deprecating `JSON_OBJ` and `JSON_ARR`

The majority of our users use `JSON_OBJ`. I'm confident that the `StructDef` and `Struct` user experience will be strictly superior to the `JSON_OBJ` experience. However, we have a lot of users currently using `JSON_OBJ`, and we haven't yet released a version of LittleHorse with a replacement.

This proposal proposes to:

1. Release `Struct` and `StructDef`, with a feature-set that is a super-set to the `JSON_OBJ` feature set, prior to 1.0.
2. Add an `@Deprecated` (or equivalent) annotation to `.declareJsonObj()`, `.declareJsonArr()`, and `.jsonPath()` before 1.0.
3. Remove all references to `JSON_OBJ` and related functionality from our documentation before 1.0 and replace with _equivalent and better_ functionality.
4. Remove `declareJsonObj()`, `declareJsonArr()`, and `.jsonPath()` from the SDK's in LittleHorse 1.0.
5. Ensure that the Server continues to support all `JSON_OBJ` functionality throughout `1.x`.
6. Remove all `JSON_OBJ` referernces from our protobuf and server-side implementation in 2.0.

### Metrics

We will add metrics to the Server which warns users when deprecated `WfSpec` protobuf is found. We will also explore whether it is possible to show these warnings in the LH Dashboard.

## Future Work

Future work includes:
* A proposal to define how casting works.
* Implementing as much type-checking in the `rpc RegisterWfSpec` as possible. There are more opportunities now that the `ExternalEventDef` has type information.
* Adding a new API over the `UserTaskOutput` (and refactoring the output structure of the `UserTaskNodeRun`) to deprecate and eventually remove the use of `.jsonPath()` to access User Task form fields.
* A proposal it possible to have `required=true` on an input variable to a `TaskDef`, just like a `ThreadVarDef` has a required variable. Note that this proposal would have to define whether it is legal to pass a classic value (which can be `NULL`) into a `TaskNode` with a required variable.

There is an upcoming Proposal which defines our Compatibility Policy and Release Schedule.
