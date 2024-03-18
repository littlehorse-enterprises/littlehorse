# Conditionals and Loops

[Conditional Branching](../../04-concepts/09-conditionals.md) is a control flow mechanismm in LittleHorse that is very similar to `if/else` in programming. It allows you to execute different branches of a `WfSpec` (like a program) depending on the values of your `Variable`s.

The `WorkflowThread` structs and objects have a `doIf()` and `doIfElse()` function which enable this feature. These functions take in:

* A `WorkflowCondition` struct or object, which is similar to the expression that goes inside the `if (/* right here */)`
* A lambda function or `WorkflowThread` which executes the logic for the `if` branch.
* Another lambda function for the `else` branch (only for `doIfElse()`)

## The `WorkflowCondition`

In an `if` statement, the expression is what goes between the `()` parentheses. It is a boolean expression that evaluates to `true` or `false`.

In LittleHorse, you can create an expression using `WorkflowThread#condition` in any of our SDK's. The method or function takes three parameters:

1. The `LHS`
2. The Comparator
3. The `RHS`

Please review the Conditionals Concepts Documentation [here](/docs/concepts/conditionals) for a refresher on the various Comparator types. They are listed below:

- `LESS_THAN`
- `GREATER_THAN`
- `LESS_THAN_EQ`
- `GREATER_THAN_EQ`
- `EQUALS`
- `NOT_EQUALS`
- `IN`
- `NOT_IN`

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
foo := wf.AddVariable("foo", model.VariableType_INT)

condition := wf.Condition(
    foo,
    model.Comparator_LESS_THAN,
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
foo := wf.AddVariable("foo", model.VariableType_INT)

condition := wf.Condition(
    foo,
    model.IN,
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

The `IfElseBody` is just a type: think of it as a functional interface that's the same as a `ThreadFunc` but it's used differently. Generally, an `IfElseBody` is provided by an anonymous function (think a `lambda` function in python).

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
foo := wf.AddVariable("foo", model.VariableType_INT)

wf.DoIf(
    wf.Condition(foo, model.Comparator_LESS_THAN, 3),
    func (ifBody *wflib.WorkflowThread) {
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
foo := wf.AddVariable("foo", model.VariableType_INT)

wf.DoIfElse(
    wf.Condition(foo, model.Comparator_LESS_THAN, 3),
    func (ifBody *wflib.WorkflowThread) {
        ifBody.Execute("my-task")
    },
    func (elseBody *wflib.WorkflowThread) {
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
foo := wf.AddVariable("foo", model.VariableType_INT)

wf.DoWhile(
    wf.Condition(foo, model.Comparator_LESS_THAN, 3),
    func (loopBody *wflib.WorkflowThread) {
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
