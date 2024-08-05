import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Managing Metadata

Before you can run a `WfRun`, you need to create your `WfSpec`'s! This guide shows you how to do that.

You can manage Metadata Objects (`WfSpec`, `TaskDef`, `ExternalEventDef`, and `UserTaskDef`) either using `lhctl` or with a grpc client. This section details how to manage them using the SDK's and grpc clients.

Please note that, in LittleHorse, all metadata requests are idempotent. Additionally, if you make a metadata request to `Put` an object with the exact same specifications of the object that already exists, the API will return the `OK` grpc status. This is useful so that you can safely use CI/CD pipelines to manage metadata, or use a script that runs upon application startup to manage metadata, without having to catch `ALREADY_EXISTS` errors.


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

In the Python SDK, a function must be `async` in order to be used as a Task Function. No annotations are required.

The easiest way to register a `TaskDef` to your LittleHorse Cluster is using the `littlehorse.create_task_def()` utility function. The following example defines a Task Function and creates a `TaskDef` in the LittleHorse Cluster.

```python
import asyncio
from littlehorse import create_task_def
from littlehorse.config import LHConfig
from littlehorse.model.service_pb2 import *
from google.protobuf.json_format import MessageToJson


# This is the Task Function. It must be `async`.
async def greet(name: str) -> None:
    print(f"Hello there, {name}!")


async def main():
    config = LHConfig()

    # Let's use the string "greet-task" as the TaskDef's name
    create_task_def(greet, "greet-task", config)


if __name__ == '__main__':
    asyncio.run(main())
```

You can delete a `TaskDef` in python using the `LittleHorseStub` and the [`rpc DeleteTaskDef`](../../08-api.md#deletetaskdef). An example is shown below:

```python
from littlehorse.config import LHConfig
from littlehorse.model import *


if __name__ == '__main__':
    # Get the grpc client
    config = LHConfig()
    client = config.stub()

    # Formulate the request
    delete_td_request = DeleteTaskDefRequest(id=TaskDefId(name="greet-task"))

    # Delete the TaskDef
    client.DeleteTaskDef(delete_td_request)
```

  </TabItem>
</Tabs>

## `WfSpec`

In LittleHorse, the easiest way to deploy a `WfSpec` is using the `Workflow` class or struct provided by our Java, Go, and Python SDK's. The `Workflow` class takes in a `WorkflowThread` function reference that defines your `WfSpec` logic (this is covered in the [Developing Workflows Documentation](/docs/developer-guide/wfspec-development/)), and has a `compile()` method which returns a `PutWfSpecRequest`.

Like other metadata requests, the `rpc PutWfSpec` is idempotent. However, as described in our [`WfSpec` Versioning docs](../../04-concepts/30-advanced/00-wfspec-versioning.md), `WfSpec` objects have compound versioning that enforces certain compatibility rules between versions. In the `PutWfSpecRequest`, you have the option to set the `allowed_updates` field of the `PutWfSpecRequest`. There are three values:

1. `ALL_UPDATES`: both breaking changes and minor revisions are accepted.
2. `MINOR_REVISION_UPDATES`: breaking changes are rejected, but minor revisions are accepted.
3. `NO_UPDATES`: the request will fail if the specified new `WfSpec` differs from the latest version.

<Tabs>
  <TabItem value="java" label="Java" default>

You can execute the `PutWfSpecRequest` with a specific `AllowedUpdateType` as follows:

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.common.proto.AllowedUpdateType;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.WfSpec;

public class Main {

    private static void wfFunc(WorkflowThread wf) {
        // The `greet` TaskDef must already exist
        wf.execute("greet", "some-name");
    }

    public static void main(String[] args) throws IOException {
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        Workflow workflow = Workflow.newWorkflow("my-wfspec", Main::wfFunc);

        // Only allow updates that do not change the API of the WfSpec
        workflow.withUpdateType(AllowedUpdateType.MINOR_REVISION_UPDATES);

        PutWfSpecRequest request = workflow.compileWorkflow();
        WfSpec result = client.putWfSpec(request);

        System.out.println(LHLibUtil.protoToJson(result));
    }
}
```

You can get or delete a `WfSpec` as follows:

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;

public class Main {

    public static void main(String[] args) throws IOException {
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        WfSpecId wfSpecId = WfSpecId.newBuilder()
                .setName("my-wfspec")
                .setMajorVersion(0) // Set to whichever major version you want
                .setRevision(0) // Set to whichever revision you want
                .build();

        WfSpec wfSpec = client.getWfSpec(wfSpecId);
        System.out.println(LHLibUtil.protoToJson(wfSpec));

        // Delete the WfSpec
        DeleteWfSpecRequest req = DeleteWfSpecRequest.newBuilder().setId(wfSpecId).build();
        client.deleteWfSpec(req);
    }
}
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
from littlehorse.config import LHConfig
from littlehorse.model import *
from littlehorse.workflow import Workflow, WorkflowThread
from google.protobuf.json_format import MessageToJson

# The workflow logic. This function must have type annotations in
# order to conform to the `ThreadInitializer` interface.
def my_workflow_func(wf: WorkflowThread) -> None:
    # the `greet` TaskDef must already exist
    wf.execute("greet", "some-name")


if __name__ == '__main__':
    # Get the grpc client
    config = LHConfig()
    client = config.stub()

    workflow: Workflow = Workflow("my-wfspec", my_workflow_func)
    # Set the allowed update type
    workflow.with_update_type(AllowedUpdateType.MINOR_REVISION_UPDATES)

    # Create the WfSpec
    request: PutWfSpecRequest = workflow.compile()
    wf_spec: WfSpec = client.PutWfSpec(request)

    print(MessageToJson(wf_spec))
```

You can get or delete a WfSpec using a `stub`:

```python
from littlehorse.config import LHConfig
from littlehorse.model import *
from littlehorse.workflow import Workflow, WorkflowThread
from google.protobuf.json_format import MessageToJson

# The workflow logic. This function must have type annotations in
# order to conform to the `ThreadInitializer` interface.
def my_workflow_func(wf: WorkflowThread) -> None:
    # the `greet` TaskDef must already exist
    wf.execute("greet", "some-name")


if __name__ == '__main__':
    # Get the grpc client
    config = LHConfig()
    client = config.stub()

    wf_spec_id: WfSpecId = WfSpecId(
        name="my-wfspec",
        major_version=0, # replace with your preferred major version
        revision=0, # replaced with your desired revision
    )
    wf_spec: WfSpec = client.GetWfSpec(wf_spec_id)

    print(MessageToJson(wf_spec))

    # Delete a WfSpec
    client.DeleteWfSpec(DeleteWfSpecRequest(id=wf_spec_id))
```

  </TabItem>
</Tabs>

## `ExternalEventDef`

As of now, the only field required to create an `ExternalEventDef` is the `name` of the `ExternalEventDef`.

<Tabs>
  <TabItem value="java" label="Java" default>

You can create, get, and delete an `ExternalEventDef` as follows:

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        PutExternalEventDefRequest request = PutExternalEventDefRequest.newBuilder()
                .setName("my-external-event-def")
                .build();

        client.putExternalEventDef(request);

        // Metadata requests in LittleHorse take 50-100 ms to propagate to the global
        // store.
        Thread.sleep(100);

        // Retrieve the ExternalEventDef
        ExternalEventDefId id = ExternalEventDefId.newBuilder()
                .setName("my-external-event-def")
                .build();
        ExternalEventDef eventDef = client.getExternalEventDef(id);
        System.out.println(LHLibUtil.protoToJson(eventDef));

        // Delete the ExternalEventDef
        DeleteExternalEventDefRequest deleteRequest = DeleteExternalEventDefRequest.newBuilder()
                .setId(id)
                .build();
        client.deleteExternalEventDef(deleteRequest);
    }
}
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

In python, you can use the `littlehorse.create_external_event_def` utility to more easily create an `ExternalEventDef` as follows:

```python
from littlehorse import create_external_event_def
from littlehorse.config import LHConfig
from littlehorse.model import *
from google.protobuf.json_format import MessageToJson

from time import sleep


if __name__ == '__main__':
    # Get the grpc client
    config = LHConfig()
    client = config.stub()

    create_external_event_def(name="my-external-event-def", config=config)

    # In LittleHorse, metadata updates take 50-100ms to propagate to the global
    # store.
    sleep(0.2)

    # Fetch the ExternalEventDef
    external_event_def_id = ExternalEventDefId(name="my-external-event-def")
    event_def: ExternalEventDef = client.GetExternalEventDef(external_event_def_id)
    print(MessageToJson(event_def))

    # Delete the ExternalEventDef
    client.DeleteExternalEventDef(DeleteExternalEventDefRequest(id=external_event_def_id))
```

  </TabItem>
</Tabs>

## `UserTaskDef`

A `UserTaskDef` specifies the data that needs to be filled out in a form for a human to complete a `UserTaskRun`.

Note that a `UserTaskDef` is a versioned object (unlike a `WfSpec`, however, there is only a `version` number and no `revision`). As such, getting and deleting `UserTaskDef`s follows a similar pattern: we provide a `GetLatestUserTaskDef` rpc call which allows you to get the latest `UserTaskDef` with a given name.

<Tabs>
  <TabItem value="java" label="Java" default>

The easiest way to create a `UserTaskDef` in Java is using the `UserTaskSchema` class. Note that it infers the schema of the `UserTaskDef` from our `MyForm` class using the `UserTaskField` annotation.

The below example shows you how to create a `UserTaskDef`, get it, and delete it.

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskDefId;

// This Java class defines our form for the UserTaskDef
class SomeForm {
    @UserTaskField(
        displayName = "Approved?",
        description = "Reply 'true' if this is an acceptable request."
    )
    public boolean isApproved;

    @UserTaskField(
        displayName = "Explanation",
        description = "Explain your answer",
        required = false
    )
    public String explanation;
}


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        String userTaskDefName = "my-user-task-def";

        // Compile the above Java class into a UserTaskDef
        UserTaskSchema userTask = new UserTaskSchema(new SomeForm(), userTaskDefName);
        PutUserTaskDefRequest putRequest = userTask.compile();

        // Register the UserTaskDef into LittleHorse
        client.putUserTaskDef(putRequest);

        // Get the UserTaskDef. Note that metadata creation takes 50-100ms to propagate
        // through the LittleHorse cluster.
        Thread.sleep(200);

        UserTaskDefId id = UserTaskDefId.newBuilder()
                .setName(userTaskDefName)
                .setVersion(0)
                .build();
        UserTaskDef result = client.getUserTaskDef(id);
        System.out.println(LHLibUtil.protoToJson(result));

        // Delete the UserTaskDef
        DeleteUserTaskDefRequest deleteRequest = DeleteUserTaskDefRequest.newBuilder()
                .setId(id)
                .build();

        client.deleteUserTaskDef(deleteRequest);
    }
}
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

In python, you can create a `UserTaskDef` using [`rpc PutUserTaskDef`](../../08-api.md#putusertaskdef).

```python
from littlehorse import create_external_event_def
from littlehorse.config import LHConfig
from littlehorse.model import *
from google.protobuf.json_format import MessageToJson

from time import sleep


if __name__ == '__main__':
    # Get the grpc client
    config = LHConfig()
    client = config.stub()

    # Manually construct the PutUserTaskDefRequest, specifying the fields we want
    # the UserTaskRun's to have.
    put_user_task_def_req = PutUserTaskDefRequest(
        name="my-user-task-def",
        fields=[
            UserTaskField(
                name="isApproved",
                description="Is the request Approved?",
                display_name="Approved?",
                required=True,
            ),
            UserTaskField(
                name="explanation",
                description="Explanation or comments for decision.",
                required=False,
                display_name="Comments",
            )
        ]
    )

    # Create the UserTaskDef
    client.PutUserTaskDef(put_user_task_def_req)

    # Wait for metadata to propagate
    sleep(0.5)

    # Get the UserTaskDef
    user_task_def_id = UserTaskDefId(name="my-user-task-def", version=0)
    user_task_def = client.GetUserTaskDef(user_task_def_id)
    print(MessageToJson(user_task_def))

    # Delete the UserTaskDef
    client.DeleteUserTaskDef(DeleteUserTaskDefRequest(id=user_task_def_id))
```

  </TabItem>
</Tabs>
