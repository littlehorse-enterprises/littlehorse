# User Tasks

[User Tasks](/docs/04-concepts/05-user-tasks.md) allow you to manage tasks involving humans alongside standard computer tasks in your LittleHorse Workflow.

You can use the Workflow SDK's to schedule User Tasks, and also create lifecycle hooks such as:
* Automatic Reassignment
* Reminder Tasks
* Automatic Cancellation.

See the [Metadata Management docs](/docs/developer-guide/grpc/managing-metadata) for information about how to create a `UserTaskDef`.

## Assigning User Tasks

You can assign a User Task in two ways:
* To a specific User, specified by an arbitrary user id (and optionally a user group).
* To a group of users.

### Assigning to Users

To assign a user task to a specific user who is not a part of a group, you can use `WorkflowThread#assignUserTask()`.

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

```java
String hardCodedUserId = "some-user-id-or-email";
WfRunVariable userIdVariable = wf.addVariable("user-id", VariableType.STR);

// Hard-coded using a string, notice that the second parameter (group id) is null
wf.assignUserTask("some-user-task-def", hardCodedUserId, null);

// Using a variable to set User Id
wf.assignUserTask("another-user-task-def", userIdVariable, null);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
hardCodedUserId := "some-user-id-or-email"
userIdVariable := wf.AddVariable("user-id", lhproto.VariableType_STR)

// Hard-coded using a string
wf.AssignUserTask("some-user-task-def", &hardCodedUserId, nil)

// Using a variable to set User Id
wf.AssignUserTask("some-user-task-def", userIdVariable, nil)
```
  </TabItem>
  <TabItem value="python" label="Python">

```python
hard_coded_user_id = "some-user-id-or-email"
user_id_variable = wf.add_variable("user-id", VariableType.STR)

# assign using a hard coded string
wf.assign_user_task("some-user-task", user_id=hard_coded_user_id)

# assign using a variable
wf.assign_user_task("some-user-task", user_id=user_id_variable)
```

  </TabItem>
</Tabs>


You can also assign a user task to a user as part of a group:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
WfRunVariable userIdVariable = wf.addVariable("user-id", VariableType.STR);
WfRunVariable userGroupVariable = wf.addVariable("user-group", VariableType.STR);
String hardCodedUserGroup = "finance";

// Hard-coded using a string
wf.asignUserTask("some-user-task-def", userIdVariable, hardCodedUserGroup);

// Using a variable to set User Group
wf.assignUserTask("another-user-task-def", userIdVariable, userGroupVariable);
```

  </TabItem>
  <TabItem value="go" label="Go">

Golang user task docs coming soon. But if you want, you can try it out anyways :wink:.

  </TabItem>
  <TabItem value="python" label="Python">

```python
user_id = wf.add_variable("user-id", VariableType.STR)
user_group = wf.add_variable("user_group", VariableType.STR)
hard_coded_user_group = "finance";

# assign using a hard coded string
wf.assign_user_task("some-user-task", user_id=user_id, user_group=hard_coded_user_group)

# assign using a variable
wf.assign_user_task("some-user-task", user_id=user_id, user_group=user_group)
```

  </TabItem>
</Tabs>

### Assigning to Groups

You can assign a task to a user group using `WorkflowThread#assignUserTask()`.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
String hardCodedUserGroup = "sales";
WfRunVariable userGroupVariable = wf.addVariable("user-group", VariableType.STR);

// Hard-coded using a string
String userId = null;
wf.assignUserTask("some-user-task-def", userId, hardCodedUserGroup);

// Using a variable to set User Group
wf.assignUserTask("another-user-task-def", userId, userGroupVariable);
```

  </TabItem>
  <TabItem value="go" label="Go">

GoLang docs for user tasks coming soon. But if you want, you can try it out anyways :wink:.

  </TabItem>
  <TabItem value="python" label="Python">

```python
user_group_str = "sales
user_group_var = wf.add_variable("user_group", VariableType.STR)

# assign using a hard coded string
wf.assign_user_task("some-user-task", user_group=user_group_str)

# assign using a variable
wf.assign_user_task("some-user-task", user_group=user_group_var)
```

  </TabItem>
</Tabs>

## Using User Task Outputs

The `assignUserTask` method return a `UserTaskOutput`, which is a special type of `NodeOutput`. It can be used to:

* Mutate variables
* Schedule reminder tasks
* Schedule user task reassignment.

### Using the Form Output

You can use a `UserTaskOutput` to mutate variables. Note that a `UserTaskDef` has a series of fields, where each field has a `name` and `value` (which is a `VariableValue` containing a primitive type).

The `UserTaskOutput` is essentially a Json output where each key is the name of each field, and the value is the value typed by the user. Let's say I have a user task def with two fields:
- `userName`, which is a `STR`
- `age`, which is an `INT`

You can use the value as follows:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
WfRunVariable age = wf.addVariable("age", VariableType.INT);
WfRunavariable userName = wf.addVariable("user-name", VariableType.STR);

UserTaskOutput formResults = wf.assignUserTask("my-user-task", "obi-wan", null);
wf.mutate(age, VariableMutationType.ASSIGN, formResults.jsonPath("$.age"));
wf.mutate(userName, VariableMutationType.ASSIGN, formResults.jsonPath("$.userName"));
```

  </TabItem>
  <TabItem value="go" label="Go">

GoLang docs for user tasks coming soon. But if you want, you can try it out anyways :wink:.

  </TabItem>
</Tabs>

### Reminder Tasks

You can use the `UserTaskOutput` to schedule a "Reminder Task", which is a `TaskRun` that runs a set period of time after the User Task is scheduled. If the `UserTaskRun` has been completed, cancelled, or reassigned by the time the Reminder Task is scheduled, _the Reminder Task does not execute._

<Tabs>
  <TabItem value="java" label="Java" default>

```java
UserTaskOutput userTask = wf.assignTaskToUser("some-task", "yoda");

int delaySeconds = 60; // wait one minute before reminder

String taskArg1 = "reply to my email, you must!";
String taskArg2 = "for my ally is the Force, and a powerful ally it is";

String taskDefName = "send-reminder";
wf.scheduleReminderTask(userTask, delaySeconds, taskDefName, taskArg1, taskArg2);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func QuickstartWorkflow(wf *littlehorse.WorkflowThread) {
	// Declare an input variable and make it searchable
	nameVar := wf.AddVariable("input-name", lhproto.VariableType_STR).Searchable()

	// Execute a task and pass in the variable.
	wf.Execute("greet", nameVar)

	arg1 := "This is the first argument passed to the reminder task"
	arg2 := "This is the second argument passed to the reminder task"
	delaySeconds := 10 // wait 10 seconds after the task is assigned to schedule the reminder
    reminderTaskDefName := "email-group"

	userTaskOutput := wf.AssignUserTask("my-user-task", nil, "some-group")
	wf.ScheduleReminderTask(userTaskOutput, delaySeconds, reminderTaskDefName, arg1, arg2)
}
```
  </TabItem>
  <TabItem value="python" label="Python">

    ```python
        def get_workflow() -> Workflow:
            def my_entrypoint(wf: WorkflowThread) -> None:
                task_def_name = "greet"
                user_task_output = wf.assign_user_task("person-details", None, "writer-group")
                delay_in_seconds = 10 // wait 10 seconds after the task is assigned to schedule the reminder
                arg1 = "Sam"
                arg2 = {"identification": "1258796641-4", "Address": "NA-Street", "Age": 28}
        
                wf.schedule_reminder_task(user_task_output, delay_in_seconds, task_def_name, arg1, arg2)
    
        return Workflow("example-user-tasks", my_entrypoint)
    ```
  </TabItem>
</Tabs>

### Automatic Reassignment

You can use the `UserTaskOutput` to automatically "release" a task from a specific user to that user's group after a period of time passes.

:::note
This only works if you assign the user task to a user _and specify the group the user belongs to_.
:::

<Tabs>
  <TabItem value="java" label="Java" default>

```java
String userGroup = "jedi-council";
UserTaskOutput userTask = wf.assignUsrTask("some-task", "Mace Windu", userGroup);

// If Mace Windu doesn't respond in 1 hour, allow any other Jedi Council member to claim
// the task.
wf.releaseToGroupOnDeadline(userTask, 60 * 60);
```
  </TabItem>
  <TabItem value="go" label="Go">

GoLang support for user tasks coming soon. But if you want, you can try it out anyways :wink:.

  </TabItem>
</Tabs>
