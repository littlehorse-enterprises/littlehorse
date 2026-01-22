# Conditional Refactors

This proposal aims to:

* Improve the user experience of our `WfSpec` DSL, particularly around complex nested conditionals.
* Clean up technical debt that complicates the protobuf API.

## Desired SDK Behavior (Java)

Users should be able to write code like the following.

### `BOOL` Logic

We should allow simple comparators that evaluate to a `BOOL`:

```java
WfRunVariable myInt = wf.declareInt("my-int").required();
WfRunVariable myBool = wf.declareBool("my-bool");

myBool.assign(myInt.isLessThan(10));
```

We should allow boolean logic:

```java
WfRunVariable myInt = wf.declareInt("my-int").required();
WfRunVariable myBool = wf.declareBool("my-bool").required();

// (myInt < 10 || myBool)
LHExpression someBool = myInt.isLessThan(10).or(myBool);
someBool = myBool.or(myInt.isLessThan(10)); // Same as line above

// Support AND
myBool.and(myInt.isLessThan(10));

// Support NOT
myBool.assign(myBool.negate());
```

(in Python we'd have to do `my_int.is_less_than(10).do_or(my_bool))` to avoid using the reserved word `or`).

### Conditionals

We should allow the `EdgeCondition` to take in any expression evaluating to a boolean. We will deprecate the `WorkflowCondition` class.

```java
WfRunVariable isVip = wf.declareBool("is-vip");
WfRunVariable price = wf.declareDouble("price");

wf.doIf(isVip, handler -> {
    handler.execute("send-special-welcome");
});

wf.doIf(isVip.or(price.isGreaterThan(10000)), handler -> {
    handler.execute("use-expedited-shipping");
})
```

## Implementation Strategy

We'll be greatly changing the class structure of the SDK's, but the vast majority of client code that works in 0.16 will continue to work in 1.x.

### SDK Changes

First, we will deprecate the `WorkflowCondition` class for removal in the 2.0 release (see the deprecation strategy below).

Next, we will edit the following methods as follows:
1. Move them from `WfRunvariable` to `LHExpression`.
2. Change them to return another `LHExpression` rather than a `WorkflowCondition`.

Methods to edit:

* `.isLessThan()`
* `.isGreaterThan()`
* `.isEqualTo()`
* `.isNotEqualTo()`
* `.doesContain()`
* `.doesNotContain()`
* `.isIn()`
* `.isNotIn()`.

### Protobuf

We'll rename the `EdgeCondition` message to `LegacyEdgeCondition`. Then we will edit the `Edge` to look like this:

```protobuf
// The Edge is the line in the workflow that connects one Node to another.
message Edge {
  // The name of the Node that the Edge points to.
  string sink_node_name = 1;

  // The Condition on which this Edge will be traversed. When choosing an Edge
  // to travel after the completion of a NodeRun, the Edges are evaluated in
  // order. The first one to pass is taken.
  //
  // If none of the conditions are set, then this `Edge` automatically passes.
  //
  // A `oneof` is used to support the legacy edge condition.
  oneof edge_condition {
    // Support for `WfSpec`s created before 1.0
    LegacyEdgeCondition legacy_condition = 2;

    // Default condition
    VariableAssignment condition = 4;
  }

  // Ordered list of Variable Mutations to execute when traversing this Edge.
  repeated VariableMutation variable_mutations = 3;
}
```

We'll rename the `enum VariableMutationType` to `enum Operation`. Then we'll add the following components to the `Operation`:

```proto
enum Operation {
  // ... (everything in VariableMutationType right now)

  // Equivalent to `<`. Only valid for primitive types (no JSON_OBJ or JSON_ARR).
  LESS_THAN = 8;

  // Equivalent to `>`. Only valid for primitive types (no JSON_OBJ or JSON_ARR).
  GREATER_THAN = 9;

  // Equivalent to `<=`. Only valid for primitive types (no JSON_OBJ or JSON_ARR).
  LESS_THAN_EQ = 10;

  // Equivalent to `>=`. Only valid for primitive types (no JSON_OBJ or JSON_ARR).
  GREATER_THAN_EQ = 11;

  // This is valid for any variable type, and is similar to .equals() in Java.
  //
  // One note: if the RHS is a different type from the LHS, then LittleHorse will
  // try to cast the RHS to the same type as the LHS. If the cast fails, then the
  // ThreadRun fails with a VAR_SUB_ERROR.
  EQUALS = 12;

  // This is the inverse of `EQUALS`
  NOT_EQUALS = 13;

  // Only valid if the RHS is a JSON_OBJ or JSON_ARR. Valid for any type on the LHS.
  //
  // For the JSON_OBJ type, this returns true if the LHS is equal to a *KEY* in the
  // RHS. For the JSON_ARR type, it returns true if one of the elements of the RHS
  // is equal to the LHS.
  IN = 14;

  // The inverse of IN.
  NOT_IN = 15;
}
```

## Deprecation Strategy

The API's above are clear to the user and are backwards-compatible. For example, the following code is valid both before and after the change:

```java
WfRunVariable foo = wf.declareInt("foo");
wf.doIf(foo.isLessThan(10), handler -> {
    handler.execute("hello");
});
```

What is going to be no longer valid is the following code:

We will release these changes in the 1.0 release. Our compatibility promise will be that:

* All 1.x and later clients will use the new API's.
* The 1.x servers will understand the old (0.16.x) API's.
* The 2.x servers will not understand the old API's. This will allow us to remove support for the old API in the 2.0 release.
* At least one 1.x release will contain functionality to migrate `WfSpec`s from the old style to the new style without user intervention.
