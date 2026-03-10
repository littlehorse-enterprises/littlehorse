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

We initially planned to rename `VariableMutationType` to `Operation` and merge comparator operations into it. However, this was not possible for compatibility reasons.

Instead, we kept `VariableMutationType` and `Comparator` as separate enums and connected them via a `oneof` inside the `VariableAssignment.Expression` message. We also added `AND` and `OR` to `VariableMutationType` for boolean logic:

```proto
enum VariableMutationType {
  ASSIGN = 0;
  ADD = 1;
  EXTEND = 2;
  SUBTRACT = 3;
  MULTIPLY = 4;
  DIVIDE = 5;
  REMOVE_IF_PRESENT = 6;
  REMOVE_INDEX = 7;
  REMOVE_KEY = 8;
  // Logical AND operation. Combines two boolean values; result is true if both LHS and RHS are true.
  AND = 9;
  // Logical OR operation. Combines two boolean values; result is true if either LHS or RHS is true.
  OR = 10;
}

// Operator for comparing two values to create a boolean expression.
enum Comparator {
  LESS_THAN = 0;
  GREATER_THAN = 1;
  LESS_THAN_EQ = 2;
  GREATER_THAN_EQ = 3;
  EQUALS = 4;
  NOT_EQUALS = 5;
  IN = 6;
  NOT_IN = 7;
}
```

The `VariableAssignment.Expression` message uses a `oneof` to select which kind of operation to perform:

```proto
message Expression {
  // The left-hand-side of the expression.
  VariableAssignment lhs = 1;

  // The operator in the expression.
  oneof operation {
    VariableMutationType mutation_type = 2;
    Comparator comparator = 4;
  }

  // The right-hand-side of the expression.
  VariableAssignment rhs = 3;
}
```

This approach preserves full backwards compatibility while still enabling comparators and boolean logic to be used inside expressions.

## Deprecation Strategy

The API's above are clear to the user and are backwards-compatible. For example, the following code is valid both before and after the change:

```java
WfRunVariable foo = wf.declareInt("foo");
wf.doIf(foo.isLessThan(10), handler -> {
    handler.execute("hello");
});
```

What is going to be no longer valid is the following outdated code:

```java
wf.doIf(wf.condition(foo, Comparator.LESS_THAN, bar), handler -> {/* ... */});
```

We will release these changes in the 1.0 release. Our compatibility promise will be that:

* All 1.x and later clients will use the new API's.
* The 1.x servers will understand the old (0.16.x) API's.
* The 2.x servers will not understand the old API's. This will allow us to remove support for the old API in the 2.0 release.
* At least one 1.x release will contain functionality to migrate `WfSpec`s from the old style to the new style without user intervention.
