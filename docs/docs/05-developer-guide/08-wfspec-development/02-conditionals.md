# Conditionals and Loops

In a LittleHorse `WfSpec`, the [`Edge`](../../08-api.md#edge) structure tells the workflow scheduler what `Node` to advance to next. The `Edge` has a `conditions` field, which allows you to specify different control flow paths based on variables in your `WfRun`. This is analogous to `if/else` in programming.

## Concepts

:::tip
This section covers the low-level details of how conditionals work in LittleHorse. If you just want to see some examples, skip ahead to [the next section](#the-workflowcondition).
:::

An [`EdgeCondition`](../../08-api.md#edgecondition) has three parts:

1. A "LHS" (left-hand side),
2. A `comparator`, and
3. A "RHS" (right-hand side).

The `comparator` is a boolean operator that operates on the LHS and the RHS and returns either `true` or `false`. The `EdgeCondition` evaluates to `

Just as `if/else` allows you to implement control flow in your programs, Conditional Branching allows you to add control flow to your LittleHorse Workflows.

Let's look at how an if statement works in Python:

```python
if foo < bar:
    do_something()
```

Look at the booean expression `foo < bar`. It consists of a left-hand-side (`foo`), comparator (`<`), and right-hand-side (`bar`).

In LittleHorse, we have Edge Conditions, which also have an LHS, Comparator, and RHS. The LHS and RHS are any `VariableAssignment`, meaning they can be a value taken from some `Variable` or a hard-coded literal value.

### Comparator Types

The supported `Comparator`'s are:

- `LESS_THAN`
- `GREATER_THAN`
- `LESS_THAN_EQ`
- `GREATER_THAN_EQ`
- `EQUALS`
- `NOT_EQUALS`
- `IN`
- `NOT_IN`

You can find a detailed description of them in the [protobuf documentation](../../08-api.md#comparator).


## The `WorkflowCondition`

Our SDK's all have a `WorkflowCondition` struct/object which makes it really easy to work with `EdgeCondition`s in a way that feels just like using if/else. In fact, you may not even need to know that the `EdgeCondition` exists.

In LittleHorse, you can create an expression using `WorkflowThread#condition` in any of our SDK's. The method or function takes three parameters:

1. The `LHS`
2. The Comparator
3. The `RHS`

The `LHS` and the `RHS` can be set in two ways:

1. A literal value.
2. A `WfRunVariable`, which means that the value of that `Variable` in the `WfRun` is used.

The following is equivalent to `foo < 3`:

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

```java

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableType;
// ...

WfRunVariable foo = wf.addVariable("foo", VariableType.INT);

WorkflowCondition condition = wf.condition(
    foo,
    Comparator.LESS_THAN,
    3
);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
foo := wf.AddVariable("foo", lhproto.VariableType_INT)

condition := wf.Condition(
    foo,
    lhproto.Comparator_LESS_THAN,
    3,
)
```

  </TabItem>
    <TabItem value="python" label="Python">

```python
def entrypoint(wf: WorkflowThread) -> None:
    foo = wf.add_variable("foo", VariableType.INT)
    condition = wf.condition(foo, Comparator.LESS_THAN, 3)
```

  </TabItem>
</Tabs>

### IN Conditional
For the `IN` conditional you have to provide either a variable or a literal value on the `LHS` that might be contained on a collection of values that is provided on the `RHS`.

<Tabs>
  <TabItem value="java" label="Java" default>

```java

import io.littlehorse.sdk.common.proto.Comparator;
// ...

WfRunVariable foo = wf.addVariable("foo", VariableType.INT);

WorkflowCondition condition = wf.condition(
    foo,
    Comparator.IN,
    [2, 3, 4]
);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
foo := wf.AddVariable("foo", lhproto.VariableType_INT)

condition := wf.Condition(
    foo,
    lhproto.IN,
    [3]int{1,2,3},
)
```

  </TabItem>
    <TabItem value="python" label="Python">

```python
def entrypoint(wf: WorkflowThread) -> None:
    foo = wf.add_variable("foo", VariableType.INT)
    condition = wf.condition(foo, Comparator.IN, [1, 2, 3])
```

  </TabItem>
</Tabs>


## If Statements

To do an `if` statement, you use `WorkflowThread::doIf()`. The method takes two arguments:

1. A `WorkflowCondition` (see above).
2. An `IfElseBody` implementation.

The `IfElseBody` is just a type: think of it as a functional interface that's the same as a `ThreadFunc` but it's used differently. Generally, an `IfElseBody` is provided by an anonymous function; however, in Python it is required to pass a proper function (not a `lambda`).

Here's an example of executing a `my-task` Task if `foo < 3`:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
WfRunVariable foo = wf.addVariable("foo", VariableType.INT);

wf.doIf(
    wf.condition(foo, Comparator.LESS_THAN, 3),
    ifBody -> {
        ifBody.execute("my-task");
    }
);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
foo := wf.AddVariable("foo", lhproto.VariableType_INT)

wf.DoIf(
    wf.Condition(foo, lhproto.Comparator_LESS_THAN, 3),
    func (ifBody *littlehorse.WorkflowThread) {
        ifBody.Execute("my-task")
    },
)
```

  </TabItem>
  <TabItem value="python" label="Python">

In python you have to use a first-class function, pay attention to `if_body` function.
This is also applicable for methods.

```python
def if_body(wf: WorkflowThread) -> None:
    wf.execute("my-task")

def entrypoint(wf: WorkflowThread) -> None:
    foo = wf.add_variable("foo", VariableType.INT)
    condition = wf.condition(foo, Comparator.LESS_THAN, 3)
    wf.do_if(condition, if_body)
```

  </TabItem>
</Tabs>

### Nested Conditions
Here's an example of executing a `my-task` Task if `foo < 3 and foo > 1`:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
WfRunVariable foo = wf.addVariable("foo", VariableType.INT);
wf.doIf(wf.condition(foo, Comparator.GREATER_THAN, 1),
        ifHandler -> {
            wf.doIf(wf.condition(foo, Comparator.LESS_THAN, 3), ifBody -> {
                ifBody.execute("my-task");
            });
});
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
wf.DoIf(
		wf.Condition(foo, lhproto.Comparator_LESS_THAN, 3),
		func (ifBody *littlehorse.WorkflowThread) {
			wf.DoIf(
					wf.Condition(foo, lhproto.Comparator.GREATER_THAN, 1),
					func (ifBody *littlehorse.WorkflowThread) {
						ifBody.Execute("my-task")
					}
			)
		}
	)
```

  </TabItem>
  <TabItem value="python" label="Python">

In python you have to use a first-class function, pay attention to `if_body` function.
This is also applicable for methods.

```python
from littlehorse.model import VariableType, Comparator
from littlehorse.workflow import WorkflowThread

foo = wf.add_variable("foo", VariableType.INT)

def if_body(wf: WorkflowThread) -> None:
    wf.execute("my-task")

def second_if(wf: WorkflowThread) -> None:
    wf.do_if(wf.condition(foo, Comparator.GREATER_THAN, 1), if_body)

def entrypoint(wf: WorkflowThread) -> None:
    condition = wf.condition(foo, Comparator.LESS_THAN, 3)
    wf.do_if(condition, second_if)
```

  </TabItem>
</Tabs>

## If Else Statements

To do an `if`/`else` statement, you can use `WorkflowThread::doIfElse()`, which is identical to `doIf()` but it takes an additional `IfElseBody` that is executed if the condition is false.

An example:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
WfRunVariable foo = wf.addVariable("foo", VariableType.INT);

wf.doIf(
    wf.condition(foo, Comparator.LESS_THAN, 3),
    ifBody -> {
        ifBody.execute("my-task");
    },
    elseBody -> {
        elseBody.execute("my-other-task");
        elseBody.execute("yet-another-task");
    }
);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
foo := wf.AddVariable("foo", lhproto.VariableType_INT)

wf.DoIfElse(
    wf.Condition(foo, lhproto.Comparator_LESS_THAN, 3),
    func (ifBody *littlehorse.WorkflowThread) {
        ifBody.Execute("my-task")
    },
    func (elseBody *littlehorse.WorkflowThread) {
        elseBody.Execute("another-task")
        elseBody.Execute("yet-another-task")
    },
)
```

  </TabItem>
    <TabItem value="python" label="Python">

In python you have to use a first-class function, pay attention to `if_body` and `else_body` functions.
This is also applicable for methods.

```python
def entrypoint(wf: WorkflowThread) -> None:
    def else_body(wf: WorkflowThread) -> None:
        wf.execute("another-task")
        wf.execute("yet-another-task")

    def if_body(wf: WorkflowThread) -> None:
        wf.execute("my-task")

    foo = wf.add_variable("foo", VariableType.INT)
    condition = wf.condition(foo, Comparator.LESS_THAN, 3)
    wf.do_if(condition, if_body, else_body)
```

  </TabItem>
</Tabs>

## While Loops

The `WorkflowThread` in LittleHorse also has a `doWhile()` function/method. To use it, you pass in a `WorkflowCondition` and a `WhileBody`, which is just a lambda function or interface defining workflow logic.

:::info
The semantics of `WorkflowThread#doWhile()` are the same as a `while` loop in programming, not a `do while`. That is because `while` is a reserved word in most languages, so we couldn't add a function called `while`.
:::

Here's an example that executes two tasks in a loop as long as `foo < 3`:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
WfRunVariable foo = wf.addVariable("foo", VariableType.INT);

wf.doWhile(
    wf.condition(foo, Comparator.LESS_THAN, 3),
    loopBody -> {
        loopBody.execute("my-task");
        loopBody.execute("another-task");
    }
);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
foo := wf.AddVariable("foo", lhproto.VariableType_INT)

wf.DoWhile(
    wf.Condition(foo, lhproto.Comparator_LESS_THAN, 3),
    func (loopBody *littlehorse.WorkflowThread) {
        loopBody.Execute("my-task")
        loopBody.Execute("another-task")
    },
)
```

  </TabItem>
    <TabItem value="python" label="Python">

In python you have to use a first-class function, pay attention to `while_body` function.
This is also applicable for methods.

```python
def entrypoint(wf: WorkflowThread) -> None:
    def while_body(wf: WorkflowThread) -> None:
        wf.execute("my-task")
        wf.execute("another-task")

    foo = wf.add_variable("foo", VariableType.INT)
    condition = wf.condition(foo, Comparator.LESS_THAN, 3)
    wf.do_while(condition, while_body)
```

  </TabItem>
</Tabs>
