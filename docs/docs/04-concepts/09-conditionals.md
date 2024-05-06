# Conditional Branching

Just as `if/else` allows you to implement control flow in your programs, Conditional Branching allows you to add control flow to your LittleHorse Workflows.

Let's look at how an if statement works in Python:

```python
if foo < bar:
    do_something()
```

Look at the booean expression `foo < bar`. It consists of a left-hand-side (`foo`), comparator (`<`), and right-hand-side (`bar`).

In LittleHorse, we have Edge Conditions, which also have an LHS, Comparator, and RHS.

The LHS and RHS are any `VariableAssignment`, meaning they can be a value taken from some `Variable` or a hard-coded literal value.

## Comparator Types

Let's look at all of the Comparators supported by LittleHorse.

### `LESS_THAN`

This is equivalent to `<` and is valid for `STR`, `INT`, `DOUBLE`, and `BOOL` RHS and LHS.

Note that you may provide a `JSON_OBJ` variable with a `jsonPath` as one of your comparands so long as the result of evaluating the `jsonPath` is a primitive type.

For example, with the following `JSON_OBJ` variable (let's say the variable is named `myVar`):

```
{
    "foo": "Hello, there!",
    "bar": {
        "baz": 1234
    }
}
```

You could do the following in the Java SDK:

```
// myVar.bar.baz < 500
thread.condition(myVar.jsonPath("$.bar.baz), ComparatorPb.LESS_THAN, 500)
```

### `GREATER_THAN`

This is the same as `LESS_THAN`, except it corresponds to `>`.

### `LESS_THAN_EQ`

This is the same as `LESS_THAN` and `GREATER_THAN`, except it corresponds to `<=`.

### `GREATER_THAN_EQ`

Well, you know the drill... `>=`.

### `EQUALS`

This is valid for any variable type, and is similar to `.equals()` in Java.

One note: if the `RHS` is a different type from the `LHS`, then LittleHorse will try to cast the `RHS` to the same type as the `LHS` (see the `Variables` section for more info). If the cast fails, then the `ThreadRun` fails with a `VAR_SUB_ERROR`.

### `NOT_EQUALS`

This is the inverse of `EQUALS`.

### `IN`

This is valid for any type on the `LHS` and `JSON_OBJ` and `JSON_ARR` on the `RHS`. For the `JSON_OBJ` type, this returns true if the `LHS` has a key which is equal to the `RHS`. For the `JSON_ARR` type, it returns true if one of the elements of the `RHS` is equal to the `LHS`.

### `NOT_IN`

This is the inverse of `IN`.
