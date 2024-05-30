import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Managing Metadata

Before you can run a `WfRun`, you need to create your `WfSpec`'s! This guide shows you how to do that.

You can manage Metadata Objects (`WfSpec`, `TaskDef`, `ExternalEventDef`, and `UserTaskDef`) either using `lhctl` or with a grpc client. This section details how to manage them using the SDK's and grpc clients.

Please note that as of LittleHorse `0.7.0`, all metadata requests are idempotent.


## `TaskDef`

In general, the easiest way to register a `TaskDef` in LittleHorse is through a `LHTaskWorker` object or struct.

<Tabs>
  <TabItem value="java" label="Java" default>

Let's say I have a properly-annotated Task Worker class:

```java
class Greeter {
    @LHTaskMethod("greet")
    public String greeting(String name) {
        return "Hello there, " + name;
    }
}
```

You can use the `LHTaskWorker` to create the `TaskDef` as follows:
```java
LHConfig config = ...;
LHTaskWorker worker = new LHTaskWorker(new Greeter(), "greeting", config);
worker.registerTaskDef();
```

You can get a `TaskDef` or delete it using the grpc client:

```java
LittleHorseBlockingStub client = ...;

// Get a TaskDef
TaskDefId taskId = TaskDefId.newBuilder().setName("my-task").build();
TaskDef myTask = client.getTaskDef(taskId);

// Delete the task
client.deleteTaskDef(DeleteTaskDefRequest.newBuilder().setId(taskId).build());
```

  </TabItem>
  <TabItem value="go" label="Go">

You can use the `LHTaskWorker` struct to automatically register a `TaskDef` from your Task Function. First, create your `LHTaskWorker` as follows (assuming that `myTaskFunc` is a function pointer to your Task Function):

```go
config := common.NewConfigFromEnv()
client, err := config.GetGrpcClient()

tw, _ := taskworker.NewTaskWorker(config, myTaskFunc, "my-task")

_, err := tw.RegisterTaskDef()
```

The above automatically generates a `TaskDef` from the function signature using GoLang reflection, and registers it with the API.

You can get and delete `TaskDef`'s as follows:

```go
client, _ := config.GetGrpcClient()
taskDefId := &model.TaskDefId{Name: "my-task"}

taskDef, err := (*client).GetTaskDef(context.Background(), taskDefId)

// delete the TaskDef
_, err = (*client).DeleteTaskDef(context.Background(), &model.DeleteTaskDefRequest{
    Id: taskDefId,
})
```

  </TabItem>
  <TabItem value="python" label="Python">

Any `async` function could be a task.

```python
async def greeting(name: str) -> str:
    return f"Hello {name}!."
```

You can use the `littlehorse.create_task_def()` util to create a new task at the server.

```python
async def main() -> None:
    config = get_config()
    littlehorse.create_task_def(greeting, "greet", config)
    ...
```

If you want to get or delete a TaskDef you can use `config.stub()` function:

```python
config = get_config()
stub = config.stub()
task_id = TaskDefId(name="greet")
stub.GetTaskDef(task_id)
stub.DeleteTaskDef(DeleteTaskDefRequest(task_id))
```

  </TabItem>
</Tabs>

## `WfSpec`

In LittleHorse, the easiest way to deploy a `WfSpec` is using the `Workflow` class or struct provided by our Java, Go, and Python SDK's. The `Workflow` class takes in a `WorkflowThread` function reference that defines your `WfSpec` logic (this is covered in the [Developing Workflows Documentation](/docs/developer-guide/wfspec-development/)), and has a `compile()` method which returns a `PutWfSpecRequest`.

Like other metadata requests, the `rpc PutWfSpec` is idempotent. However, as described in our [`WfSpec` Versioning docs](/docs/concepts/workflows#wfspec-versioning), `WfSpec` objects have compound versioning that enforces certain compatibility rules between versions. In the `PutWfSpecRequest`, you have the option to set the `allowed_updates` field of the `PutWfSpecRequest`. There are three values:

1. `ALL_UPDATES`: both breaking changes and minor revisions are accepted.
2. `MINOR_REVISION_UPDATES`: breaking changes are rejected, but minor revisions are accepted.
3. `NO_UPDATES`: the request will fail if the specified new `WfSpec` differs from the latest version.

<Tabs>
  <TabItem value="java" label="Java" default>

You can execute the `PutWfSpecRequest` with a specific `AllowedUpdateType` as follows:

```java
LittleHorseBlockingStub client = ...;
Workflow wf = Workflow.newWorkflow("my-workflow", someWorkflowThreadFunc); // see WfSpec Development Docs

// Optionally set the update type on the workflow.
wf.withUpdateType(AllowedUpdateType.MINOR_REVISION_UPDATES);

wf.registerWf(client);

// Alternatively, use the raw grpc client.
PutWfSpecRequest request = wf.compile();
client.putWfSpec(request);
```

You can get a `WfSpec` as follows:

```java
LittleHorseBlockingStub client = ...;
WfSpecId wfId = WfSpecId.newBuilder()
        .setName("my-wf")
        .setMajorVersion(2)
        .setRevision(1)
        .build();

WfSpec wfSpec = client.getWfSpec(wfId);

// Get the latest `WfSpec` with a given name
WfSpec latestWfSpec = client.getLatestWfSpec(
    GetLatestWfSpecRequest.newBuilder().setName("my-wf").build()
);

// Get the latest `WfSpec` with a given name and majorVersion 1
WfSpec latestWfSpec = client.getLatestWfSpec(
    GetLatestWfSpecRequest.newBuilder().setName("my-wf").setMajorVersion(1).build()
);

// Delete a WfSpec
client.deleteWfSpec(DeleteWfSpecRequest.newBuilder().setId(wfId).build());
```

  </TabItem>
  <TabItem value="go" label="Go">

Assuming that you have a function `basic.MyWorkflow` which is a valid Workflow Function in Go, you can create a `WfSpec` as follows:

```go
config := common.NewConfigFromEnv()
client, err := config.GetGrpcClient()

wf := wflib.NewWorkflow(basic.MyWorkflow, "my-workflow").WithUpdateType(model.AllowedUpdateType_MINOR_REVISION_UPDATES)
putWf, _ := wf.Compile()

resp, err := (*client).PutWfSpec(context.Background(), putWf)
```

You can get and delete `WfSpec`s as follows:

```go
wfSpecId := &model.WfSpecId{
    Name:         "my-wf",
    MajorVersion: 2,
    Revision:     1,
}

wfSpec, err := (*client).GetWfSpec(context.Background(), wfSpecId)

// Get the latest wfSpec. Setting majorVersion is optional for this request
majorVersion := int32(2)

someWf, err := (*client).GetLatestWfSpec(
    context.Background(),
    &model.GetLatestWfSpecRequest{
        Name:         "some-wf",
        MajorVersion: &majorVersion,
    },
)

// delete the WfSpec
_, err = (*client).DeleteWfSpec(context.Background(), &model.DeleteWfSpecRequest{
    Id: wfSpecId,
})
```

  </TabItem>
  <TabItem value="python" label="Python">

Assuming you have a workflow, you can use the `littlehorse.create_workflow_spec()` utility function.

```python
async def main() -> None:
    config = LHConfig()
    wf = Workflow("some-wf", my_wf_func)
    wf.with_update_type(AllowedUpdateType.ALL_UPDATES)

    littlehorse.create_workflow_spec(wf, config)
```

You can get or delete a WfSpec using a `stub`:

```python
client = config.stub()

wf_spec_id = WfSpecId(name="my-workflow", majorVersion=2, revision=1)
wf_spec = client.GetWfSpec(wf_spec_id)

client.DeleteWfSpec(DeleteWfSpecRequest(wf_spec_id))
```

  </TabItem>
</Tabs>

## `ExternalEventDef`

As of now, the only field required to create an `ExternalEventDef` is the `name` of the `ExternalEventDef`.

<Tabs>
  <TabItem value="java" label="Java" default>

To create an `ExternalEventDef` in Java
```java
LittleHorseBlockingStub client = ...;

client.putExternalEventDef(PutExternalEventDefRequest.newBuilder().setName("some-event").build());
```

You can get an `ExternalEventDef` as follows:

```java
LittleHorseBlockingStub client = ...;

ExternalEventDefId id = ExternalEventDefId.newBuilder().setName("my-event").build();

ExternalEventDef eed = client.getExternalEventDef(id);

// Delete an ExternalEventDef
client.deleteExternalEventDef(DeleteExternalEventDef.newBuilder().setId(id).build());
```

  </TabItem>
  <TabItem value="go" label="Go">

Assuming that you have a function `basic.MyWorkflow` which is a valid Workflow Function in Go, you can create a `WfSpec` as follows:

```go
config := common.NewConfigFromEnv()
client, err := config.GetGrpcClient()

resp, err := (*client).PutExternalEventDef(context.Background(),
    &model.PutExternalEventDefRequest{
        Name: "some-event",
    },
)
```

You can get and delete `ExternalEventDef`s as follows:

```go
externalEventDefId := &model.ExternalEventDefId{
    Name: "some-event",
}
eed, err := (*client).GetExternalEventDef(context.Background(), externalEventDefId)

// delete the ExternalEventDef
_, err = (*client).DeleteExternalEventDef(context.Background(), &model.DeleteExternalEventDefRequest{
    Id: externalEventDefId,
})
```

  </TabItem>
  <TabItem value="python" label="Python">

To create a `ExternalEventDef` in python you can use the littlehorse utils:

```python
config = LHConfig()
client = config.stub()

client.putExternalEventDef(PutExternalEventDefRequest(name="some-external-event"))

# or, use a wrapper convenience method.
littlehorse.create_external_event_def("my-external-event", config)
```

You can get or delete it using our stub:

```python
client = config.stub()
ext_event_def_id = ExternalEventDefId(name="my-workflow")
client.GetExternalEventDef(ext_event_def_id)
client.DeleteExternalEventDef(DeleteExternalEventDefRequest(ext_event_def_id))
```

  </TabItem>
</Tabs>

## `UserTaskDef`

A `UserTaskDef` specifies the data that needs to be filled out in a form for a human to complete a `UserTaskRun`.

Note that a `UserTaskDef` is a versioned object (unlike a `WfSpec`, however, there is only a `version` number and no `revision`). As such, getting and deleting `UserTaskDef`s follows a similar pattern: we provide a `GetLatestUserTaskDef` rpc call which allows you to get the latest `UserTaskDef` with a given name.

<Tabs>
  <TabItem value="java" label="Java" default>

The easiest way to create a `UserTaskDef` in Java is using the `UserTaskSchema` class. First, define a User Task Form using the `@UserTaskField` annotation:

```java
class SomeForm {
    @UserTaskField(
        displayName = "Approved?",
        description = "Reply 'true' if this is an acceptable request."
    )
    public boolean isApproved;

    @UserTaskField(displayName = "Explanation", description = "Explain your answer")
    public String explanation;
}
```

Next, use that class to create a `UserTaskDef`:

```java
LHConfig config = new LHConfig();
LittleHorseBlockingStub client = config.getBlockingStub();

UserTaskSchema requestForm = new UserTaskSchema(
        new SomeForm(), "some-form-usertaskdef");

client.putUserTaskDef(requestForm.compile());
```

  </TabItem>
  <TabItem value="go" label="Go">

To create a `UserTaskDef` in Go, you can create the `PutUserTaskDefRequest` object.

```go
config := common.NewConfigFromEnv()
client, err := config.GetGrpcClient()

description := "this is a cool usertaskdef!"
result, err := (*client).PutUserTaskDef(context.Background(),
     &model.PutUserTaskDefRequest{
        Name: "my-user-task",
		Fields: []*model.UserTaskField{
			&model.UserTaskField{
				Name: "my-first-int-field",
				Type: model.VariableType_INT,
			},
			&model.UserTaskField{
				Name: "my-second-str-field",
				Type: model.VariableType_STR,
			},
		},
		Description: &description,
    },
)
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
stub = config.stub()
stub.PutUserTaskDef(
    PutUserTaskDefRequest(
        name="some-form-usertaskdef",
        description="This is a cool usertaskdef!",
        fields=[
            UserTaskField(name="my-first-int-field", type=VariableType.INT),
            UserTaskField(name="my-first-str-field", type=VariableType.STR),
        ],
    )
)
```

  </TabItem>
</Tabs>
