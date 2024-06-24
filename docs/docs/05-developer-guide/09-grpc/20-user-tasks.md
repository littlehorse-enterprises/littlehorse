import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Handling User Tasks

[User Tasks](/docs/concepts/user-tasks) enable a `ThreadRun` to block until a human user provides some input into the workflow. Additionally, User Tasks have several useful hooks such as automatic reassignment, reminders, and auditing capabilities.

This page shows you how to interact with a `UserTaskRun` on an already-running `WfRun`.

:::tip
For documentation about how to insert a `USER_TASK` node into your `WfSpec`, please refer to the [`WfSpec` development documentation](/docs/developer-guide/wfspec-development/user-tasks).
:::

Note that LittleHorse does not provide an out-of-the-box implementation of a User Task Manager application. This is because it would likely be of limited use to our users, because the implementation of User Task Applications is highly use-case specific. For example, each of the following considerations might be handled vastly differently depending on the application:

* **Presentation:** is the user task form presented in a mobile app, standalone internal web application, or embedded into a customer-facing site?
* **Identity Management:** what system is used to manage and determine the identity of the person executing User Tasks?
* **Look and Feel:** what is the style of the actual page?
* **Access Permisions:** while the `userGroup` field of a `UserTaskRun` is useful for determining who may execute a `UserTaskRun`, how should the Task Manager application determine who can perform additional acctions, such as reassignment and viewing audit information?

While those considerations are left to the user of LittleHorse, User Tasks still provide an incredibly valuable tool to our users, specifically:

* Direct integrations with the `WfSpec`
* Built-in reassignment and reminder capabilities
* Built-in search capabilities.

This documentation explains everything you need in order to build your own application-specific User Tasks integration.

## `UserTaskRun` Lifecycle

In order to use User Tasks, you must first create a `WfSpec` that has a `USER_TASK` node in it, for example by using the `WorkflowThread#assignUserTask()` method (see our [`WfSpec` Development Docs](/docs/developer-guide/wfspec-development/user-tasks)).

When a `ThreadRun` arrives at such a `Node`, then a `UserTaskRun` object is created in the LittleHorse Data Store. The `ThreadRun` will "block" at that `Node` until the `UserTaskRun` is either completed or cancelled. When the `UserTaskRun` is completed, the `NodeRun` returns an output which is a `JSON_OBJ` containing a key-value pair for every field in the `UserTaskDef` (in plain English, this is just one key-value for each field in the User Task form). When the `UserTaskRun` is cancelled, an `EXCEPTION` is propagated to the `ThreadRun`.

The only way to Complete a `UserTaskRun` is via the `rpc CompleteUserTaskRun` endpoint on the LH Server. A `UserTaskRun` may be cancelled either by the `rpc CancelUserTaskRun` or by lifecycle hooks built-in to the `WfSpec`.

A `UserTaskRun` may be in one of the four statuses below:

* `UNASSIGNED`: the `UserTaskRun` does not have a specific `user_id` set. In this case, `user_group` must be set.
* `ASSIGNED`: the `UserTaskRun` has a specific `user_id` set, and may have a `user_group` set as well.
* `DONE`: the `UserTaskRun` has been completed.
* `CANCELLED`: the `UserTaskRun` has been cancelled either by a manual `rpc CancelUserTaskRun` or by a built-in User Task lifecycle hook.

## Search for `UserTaskRun`s

Before you can do anything useful with User Tasks, you need to be able to search for a list of `UserTaskRun`'s matching certain criteria. The endpoint `rpc SearchUserTaskRun` allows you to do this.

You can find the documentation for `rpc SearchUserTaskRun` [here in our API documentation](../../08-api.md#searchusertaskrun).

There are six filters that can be provided:
* `status`: an enum of either `DONE`, `UNASSIGNED`, `ASSIGNED`, or `CANCELLED`.
* `user_task_def_name`: the name of the associated `UserTaskDef`.
* `user_id`: Only returns `UserTaskRun`'s assigned to a specific user.
* `user_group`: only returns `UserTaskRun`'s assigned to a group.
* `earliest_start`: only returns `UserTaskRun`'s created after this date.
* `latest_start`: only returns `UserTaskRun`'s created before this date.

All fields are additive; meaning that you can specify any combination of the fields, and only `UserTaskRun`'s matching _all_ of the criteria will be

:::info
The `user_id` and `user_group` fields are _not_ managed by LittleHorse. Rather, they are intended to allow the user of LittleHorse to pass values managed by an external identity provider. This allows User Tasks to support a wide array of identity management systems.
:::

See below for an example of searching for `UserTaskRun`'s with the following criteria:
* Assigned to the `jedi-council` group, and specifically to be executed by `obiwan`
* Created in the past week but at least 24 hours ago.
* In the `ASSIGNED` status.
* Of the type `approve-funds-for-mission`.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.google.protobuf.Timestamp;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunIdList;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        Timestamp oneWeekAgo = Timestamp.newBuilder()
                .setSeconds(Instant.now().minus(7, ChronoUnit.DAYS).getEpochSecond())
                .build();

        Timestamp oneDayAgo = Timestamp.newBuilder()
                .setSeconds(Instant.now().minus(7, ChronoUnit.DAYS).getEpochSecond())
                .build();

        // You can omit certain search criteria here if desired. Only one criterion is needed
        // but you may provide as many criteria as you wish. This request shows all of the
        // available search criteria.
        //
        // Note that it is a paginated request as per the "Basics" section of our grpc docs.
        SearchUserTaskRunRequest req = SearchUserTaskRunRequest.newBuilder()
                .setUserId("obiwan")
                .setUserGroup("jedi-council")
                .setUserTaskDefName("it-request")
                .setStatus(UserTaskRunStatus.ASSIGNED)
                .setEarliestStart(oneWeekAgo)
                .setLatestStart(oneDayAgo)
                .build();

        UserTaskRunIdList results = client.searchUserTaskRun(req);
        System.out.println(LHLibUtil.protoToJson(results));

        // Omitted: process the UserTaskRunIdList, maybe using pagination.
    }
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
package main

import (
	"context"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func main() {
	// Get a client
	config := common.NewConfigFromEnv()
	client, _ := config.GetGrpcClient()

	oneWeekAgo := timestamppb.New(time.Now().Add(-7 * 24 * time.Hour))
	oneDayAgo := timestamppb.New(time.Now().Add(-24 * time.Hour))

	userTaskDefName := "it-request"
	userGroup := "jedi-temple"
	userId := "obi-wan"
	status := model.UserTaskRunStatus_ASSIGNED

	// You may provide any or all of the following options. The only requirement
	// is that you must specify at least one criterion.
	searchReq := &model.SearchUserTaskRunRequest{
		UserTaskDefName: &userTaskDefName,
		UserId:          &userId,
		UserGroup:       &userGroup,
		Status:          &status,
		EarliestStart:   oneWeekAgo,
		LatestStart:     oneDayAgo,
	}

	results, _ := (*client).SearchUserTaskRun(context.Background(), searchReq)
	common.PrintProto(results)
}
```
  </TabItem>
  <TabItem value="python" label="Python">

```python
from littlehorse.config import LHConfig
from littlehorse.model import *
from datetime import datetime, timedelta
from google.protobuf.timestamp_pb2 import Timestamp
from google.protobuf.json_format import MessageToJson

config = LHConfig()
client = config.stub()

one_week_ago = Timestamp()
one_week_ago.FromDatetime(datetime.now() - timedelta(weeks=1))

one_day_ago = Timestamp()
one_day_ago.FromDatetime(datetime.now() - timedelta(days=1))

results: UserTaskRunIdList = client.SearchUserTaskRun(SearchUserTaskRunRequest(
    user_task_def_name="it-request",
    user_id="obiwan",
    user_group="jedi-council",
    status=UserTaskRunStatus.ASSIGNED,
    earliest_start=one_week_ago,
    latest_start=one_day_ago,
))

print(MessageToJson(results))
```
  </TabItem>
</Tabs>


## Display a `UserTaskRun`

Now that you've found some relevant `UserTaskRun`'s that you want to display in your application, how do you show them? This is particularly important to understand when building a generic User Task Manager.

First, the `rpc GetUserTaskRun` request can be used to get the details for of a `UserTaskRun`. To use this request, you need a `UserTaskRunId` (see the note on searching above!).

Once you have the `UserTaskRun`, you can inspect the results (if it's already completed). If you are trying to develop a frontend to execute the `UserTaskRun`, you can iterate through the `fields` of the `UserTaskDef` and determine what fields are to be displayed.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import java.util.Map;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskField;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        // Get a UserTaskRunId somehow. For example, you could search for one as shown
        // in the section above.
        UserTaskRunId id = UserTaskRunId.newBuilder()
                .setWfRunId(WfRunId.newBuilder().setId("e0e49b53298a4965b059a1a5df095b09"))
                .setUserTaskGuid("8bb5d43e14894c82bb1deab7a68b32ae")
                .build();

        // Fetch the UserTaskRun
        UserTaskRun userTaskRun = client.getUserTaskRun(id);

        // See the current owners
        String userId = userTaskRun.hasUserId() ? userTaskRun.getUserId() : null;
        String userGroup = userTaskRun.hasUserGroup() ? userTaskRun.getUserGroup() : null;
        System.out.println(
                "The UserTaskRun is assigned to group '%s' and user '%s'".formatted(userGroup, userId));

        // In order to see the fields, you need to fetch the `UserTaskDef`.
        UserTaskDef utd = client.getUserTaskDef(userTaskRun.getUserTaskDefId());
        for (UserTaskField field : utd.getFieldsList()) {
            System.out.println("Field %s has type %s".formatted(field.getName(), field.getType()));
        }

        // If the UserTaskRun is in the `DONE` state, it will have `results`.
        if (userTaskRun.getStatus() == UserTaskRunStatus.DONE) {
            for (Map.Entry<String, VariableValue> resultEntry : userTaskRun.getResultsMap().entrySet()) {
                System.out.println(
                        resultEntry.getKey() +
                        ": " +
                        LHLibUtil.protoToJson(resultEntry.getValue()));
            }
        }
    }
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
package main

import (
	"context"
	"fmt"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func main() {
	// Get a client
	config := common.NewConfigFromEnv()
	client, _ := config.GetGrpcClient()

	// Get a UserTaskRunId
	id := &model.UserTaskRunId{
		WfRunId: &model.WfRunId{
			Id: "e0e49b53298a4965b059a1a5df095b09",
		},
		UserTaskGuid: "8bb5d43e14894c82bb1deab7a68b32ae",
	}

	// Fetch the UserTaskRun
	userTaskRun, err := (*client).GetUserTaskRun(context.Background(), id)
	if err != nil {
		log.Fatalf("Failed to get UserTaskRun: %v", err)
	}

	// See the current owners
	userId := ""
	if userTaskRun.UserId != nil {
		userId = *userTaskRun.UserId
	}
	userGroup := ""
	if userTaskRun.UserGroup != nil {
		userGroup = *userTaskRun.UserGroup
	}
	fmt.Printf("The UserTaskRun is assigned to group '%s' and user '%s'\n", userGroup, userId)

	// Fetch the UserTaskDef
	utd, err := (*client).GetUserTaskDef(context.Background(), userTaskRun.UserTaskDefId)
	if err != nil {
		log.Fatalf("Failed to get UserTaskDef: %v", err)
	}
	for _, field := range utd.Fields {
		fmt.Printf("Field %s has type %s\n", field.Name, field.Type)
	}

	// If the UserTaskRun is in the DONE state, it will have results
	if userTaskRun.Status == model.UserTaskRunStatus_DONE {
		fmt.Println(userTaskRun.Results)
	}
}
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
from littlehorse.config import LHConfig
from littlehorse.model import *


if __name__ == '__main__':
    config = LHConfig()
    client = config.stub()

    # Get a UserTaskRunId from somewhere; for example, use the search described above
    id = UserTaskRunId(
        wf_run_id=WfRunId(id="e0e49b53298a4965b059a1a5df095b09"),
        user_task_guid="8bb5d43e14894c82bb1deab7a68b32ae"
    )

    # Fetch the UserTaskRun
    user_task_run: UserTaskRun = client.GetUserTaskRun(id)

    # See the current owners
    user_id = user_task_run.user_id if user_task_run.user_id else ""
    user_group = user_task_run.user_group if user_task_run.user_group else ""
    print(f"The UserTaskRun is assigned to group '{user_group}' and user '{user_id}'")

    # Fetch the UserTaskDef
    utd: UserTaskDef = client.GetUserTaskDef(user_task_run.user_task_def_id)
    for field in utd.fields:
        print(f"Field {field.name} has type {VariableType.Name(field.type)}")

    # If the UserTaskRun is in the DONE state, it will have results
    if user_task_run.status == UserTaskRunStatus.DONE:
        print(user_task_run.results)
```

  </TabItem>
</Tabs>

## Complete a `UserTaskRun`

To complete a `UserTaskRun`, you can use the `rpc CompleteUserTaskRun`. The protobuf for the call is as follows:

```
rpc CompleteUserTaskRun(CompleteUserTaskRunRequest) returns (google.protobuf.Empty) {}
```

The `CompleteUserTaskRunRequest` message is defined as follows:

```protobuf
message CompleteUserTaskRunRequest {
  UserTaskRunId user_task_run_id = 1;
  map<string, VariableValue> results = 2;
  string user_id = 3;
}
```

You can also consult our autogenerated API documentation for the [`rpc CompleteUserTaskRun`](../../08-api.md#completeusertaskrun) or for [`message CompleteUserTaskRunRequest`](../../08-api.md#completeusertaskrunrequest).

The first field is the `UserTaskRunId` of the `UserTaskRun` which you intend to complete. The second is a map where each key is the name of a `field` in the `UserTaskDef`, and the value is a `VariableValue` representing the value of that User Task Field. The `user_id` field must be set and is the `user_id` of the person completing the `UserTaskRun`.

The current behavior of the `user_id` field is that, if it differs from the current owner of the `UserTaskRun`, then the `UserTaskRun` will be re-assigned to the new `user_id`. We have an [open ticket](https://github.com/littlehorse-enterprises/littlehorse/issues/617) to make this behavior configurable. If this is an important feature for you, please comment on the ticket! We're happy to bump its priority; alternatively, we do accept Pull Requests :smile:.

In the examples below, the user `obiwan` will complete a `UserTaskRun` that has two fields: a `STR` field called `requestedItem` set to `"lightsaber"`, and a `STR` field called `justification`.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.WfRunId;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        // Get a UserTaskRunId somehow. For example, you could search for one as shown
        // in the section above.
        UserTaskRunId id = UserTaskRunId.newBuilder()
                .setWfRunId(WfRunId.newBuilder().setId("e0e49b53298a4965b059a1a5df095b09"))
                .setUserTaskGuid("8bb5d43e14894c82bb1deab7a68b32ae")
                .build();

        // Complete the UserTaskRun. The key of `putResults` is the `name` of the `UserTaskField`,
        // and the value comes from the `LHLibUtil#objToVarVal()` method which is a convenience
        // for creating a `VariableValue`.
        client.completeUserTaskRun(CompleteUserTaskRunRequest.newBuilder()
                .setUserId("obiwan") // if different than the current value, it will overwrite it.
                .putResults("requestedItem", LHLibUtil.objToVarVal("lightsaber"))
                .putResults("justification", LHLibUtil.objToVarVal("Darth Maul kicked it down the mine shaft"))
                .setUserTaskRunId(id)
                .build());
    }
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
package main

import (
	"context"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func main() {
	// Get a client
	config := common.NewConfigFromEnv()
	client, _ := config.GetGrpcClient()

	// Get a UserTaskRunId
	id := &model.UserTaskRunId{
		WfRunId: &model.WfRunId{
			Id: "f2491b41b7354382988215b789187b74",
		},
		UserTaskGuid: "aa87109f001b432394cec35713ef3359",
	}

	completeRequest := &model.CompleteUserTaskRunRequest{
		UserTaskRunId: id,
		UserId:        "obi-wan",
		Results:       make(map[string]*model.VariableValue),
	}

	requestedItem, _ := common.InterfaceToVarVal("lightsaber")
	justification, _ := common.InterfaceToVarVal("Darth Maul took it away!")

	completeRequest.Results["requestedItem"] = requestedItem
	completeRequest.Results["justification"] = justification

	(*client).CompleteUserTaskRun(context.Background(), completeRequest)
}
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
from littlehorse import to_variable_value
from littlehorse.config import LHConfig
from littlehorse.model import *


if __name__ == '__main__':
    config = LHConfig()
    client = config.stub()

    # Get a UserTaskRunId from somewhere; for example, use the search described above
    id = UserTaskRunId(
        wf_run_id=WfRunId(id="ec9d975af1524f4cbcb988512b258623"),
        user_task_guid="f686ec1384404c27a90f86dcb4fd9edf"
    )

    client.CompleteUserTaskRun(CompleteUserTaskRunRequest(
        user_task_run_id=id,
        user_id="obiwan",
        results={
            "requestedItem": to_variable_value("lightsaber"),
            "justification": to_variable_value("Darth Maul kicked my old one off the balcony!")
        }
    ))
```

  </TabItem>
</Tabs>


## Re-Assign a `UserTaskRun`

When building a task manager application, you may wish to have an administrative panel in which an admin may assign or re-assign tasks to various people. To re-assign a `UserTaskRun`, you can use the request `rpc AssignUserTaskRun`. The request proto is as follows:

```protobuf
message AssignUserTaskRunRequest {
  UserTaskRunId user_task_run_id = 1;

  bool override_claim = 2;

  optional string user_group = 3;
  optional string user_id = 4;
}
```



If the `override_claim` field is set to `false` and the `UserTaskRun` is already assigned to a specific `user_id`, then the request will fail with `FAILED_PRECONDITION`.

It is important to note that the request will _overwrite_ both the `user_id` _and_ the `user_group` with the provided values from this request. If the `UserTaskRun` is currently assigned to `user_group == 'sales'` and `user_id == null`, and a client makes the following request:

```
{
    user_task_run_id: ...,
    override_claim: false,
    user_group: null,
    user_id: "sarah"
}
```

The `UserTaskRun` will be assigned to `user_id: "sarah"` and `user_group: null`. An example request is shown below.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.WfRunId;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        // Get a UserTaskRunId somehow. For example, you could search for one as shown
        // in the section above.
        UserTaskRunId id = UserTaskRunId.newBuilder()
                .setWfRunId(WfRunId.newBuilder().setId("a7476518fdff4dd49f47dbe40df3c5a6"))
                .setUserTaskGuid("709cac9fcd424d87810a6cabf66d400e")
                .build();

        // Reassign the UserTaskRun.
        client.assignUserTaskRun(AssignUserTaskRunRequest.newBuilder()
                .setUserId("mace-windu")
                .setUserGroup("jedi-temple")
                .setOverrideClaim(true)
                .setUserTaskRunId(id)
                .build());
    }
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
package main

import (
	"context"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func main() {
	// Get a client
	config := common.NewConfigFromEnv()
	client, _ := config.GetGrpcClient()

	// Get a UserTaskRunId
	id := &model.UserTaskRunId{
		WfRunId: &model.WfRunId{
			Id: "a7476518fdff4dd49f47dbe40df3c5a6",
		},
		UserTaskGuid: "709cac9fcd424d87810a6cabf66d400e",
	}

	newUserId := "yoda"
	newUserGroup := "jedi-temple"

	(*client).AssignUserTaskRun(context.Background(), &model.AssignUserTaskRunRequest{
		UserTaskRunId: id,
		UserGroup:     &newUserGroup,
		UserId:        &newUserId,
		OverrideClaim: true,
	})
}

```

  </TabItem>
  <TabItem value="python" label="Python">

```python
from littlehorse import to_variable_value
from littlehorse.config import LHConfig
from littlehorse.model import *


if __name__ == '__main__':
    config = LHConfig()
    client = config.stub()

    # Get a UserTaskRunId from somewhere; for example, use the search described above
    id = UserTaskRunId(
        wf_run_id=WfRunId(id="a7476518fdff4dd49f47dbe40df3c5a6"),
        user_task_guid="709cac9fcd424d87810a6cabf66d400e"
    )

    client.AssignUserTaskRun(AssignUserTaskRunRequest(
        user_task_run_id=id,
        user_id="yaddle",
        user_group="jedi-temple",
        override_claim=True,
    ))
```

  </TabItem>
</Tabs>


## Cancel a `UserTaskRun`

The last useful operation you may need to do when building an application using User Tasks is to "cancel" a `UserTaskRun`.

:::info
By default, when a `UserTaskRun` is cancelled, the `NodeRun` fails with an `ERROR`. However, in the `WfSpec` SDK's you can configure this behavior on a case-by-case basis. For example, you can override the behavior to throw a specific business `EXCEPTION` upon cancellation.
:::

The request `rpc CancelUserTaskRun` is quite simple. The only edge-case is that the request throws `FAILED_PRECONDITION` if the `UserTaskRun` is already in the `DONE` status.

```protobuf
message CancelUserTaskRunRequest {
  UserTaskRunId user_task_run_id = 1;
}
```

A simple example is shown below:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.WfRunId;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        // Get a UserTaskRunId somehow. For example, you could search for one as shown
        // in the section above.
        UserTaskRunId id = UserTaskRunId.newBuilder()
                .setWfRunId(WfRunId.newBuilder().setId("a7476518fdff4dd49f47dbe40df3c5a6"))
                .setUserTaskGuid("709cac9fcd424d87810a6cabf66d400e")
                .build();

        // Reassign the UserTaskRun.
        client.cancelUserTaskRun(CancelUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(id)
                .build());
    }
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
package main

import (
	"context"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func main() {
	// Get a client
	config := common.NewConfigFromEnv()
	client, _ := config.GetGrpcClient()

	// Get a UserTaskRunId
	id := &model.UserTaskRunId{
		WfRunId: &model.WfRunId{
			Id: "a7476518fdff4dd49f47dbe40df3c5a6",
		},
		UserTaskGuid: "709cac9fcd424d87810a6cabf66d400e",
	}

	(*client).CancelUserTaskRun(context.Background(), &model.CancelUserTaskRunRequest{
		UserTaskRunId: id,
	})
}
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
from littlehorse.config import LHConfig
from littlehorse.model import *


if __name__ == '__main__':
    config = LHConfig()
    client = config.stub()

    # Get a UserTaskRunId from somewhere; for example, use the search described above
    id = UserTaskRunId(
        wf_run_id=WfRunId(id="a7476518fdff4dd49f47dbe40df3c5a6"),
        user_task_guid="709cac9fcd424d87810a6cabf66d400e"
    )

    client.CancelUserTaskRun(CancelUserTaskRunRequest(
        user_task_run_id=id,
    ))
```

  </TabItem>
</Tabs>
