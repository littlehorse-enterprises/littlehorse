import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Running a Workflow

You can run a `WfSpec`, thus creating a `WfRun`, in two ways:

1. Using a grpc client in an SDK of your choice.
2. Using `lhctl`.

For a tutorial on running a `WfSpec` to create a `WfRun` using `lhctl`, see the [`lhctl` docs](/docs/developer-guide/lhctl).

## Simple

The most basic way to run a `WfRun` is as follows. This will run the latest version of the `WfSpec` with the provided name.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
LittleHorseBlockingStub client = ...;

WfRun result = client.runWf(RunWfRequest.newBuilder().setWfSpecName("some-wf-spec").build());
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
config := littlehorse.NewConfigFromEnv()
client, _ := config.GetGrpcClient()

result, err := (*client).RunWf(context.Background(), &lhproto.RunWfRequest{
    WfSpecName: "some-wf-spec",
})
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
config = LHConfig()
stub = config.stub()
stub.RunWf(RunWfRequest(wf_spec_name="some-wf-spec"))
```

  </TabItem>
</Tabs>

## Pinning to a Major Version

As a consumer of a `WfSpec`, an application may wish to pin to a specific `majorVersion` in order to allow the owner of the `WfSpec` to evolve the `WfSpec` in a compatible manner (i.e. without changing the exposed input variables or searchable variables). You can do this by providing the `majorVersion` flag. When you run a `WfSpec` and specify the `majorVersion`, the resulting `WfRun` will be a member of the `WfSpec` with the provided name and `majorVersion` and the **latest** `revision` that is still compatible with that `majorVersion`.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
LittleHorseBlockingStub client = ...;

WfRun result = client.runWf(RunWfRequest.newBuilder()
        .setWfSpecName("some-wf-spec")
        .setMajorVersion(2)
        .build());

```

  </TabItem>
  <TabItem value="go" label="Go">

```go
config := littlehorse.NewConfigFromEnv()
client, _ := config.GetGrpcClient()

var version int32
version = 2

result, err := (*client).RunWf(context.Background(), &lhproto.RunWfRequest{
    WfSpecName:   "some-wf-spec",
    MajorVersion: &version,
})
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
config = LHConfig()
stub = config.stub()
stub.RunWf(RunWfRequest(wf_spec_name="some-wf-spec", majorVersion=2))
```

  </TabItem>
</Tabs>

## Pinning the Revision

You can also pin the exact `revision` as follows, thus guaranteeing the exact `WfSpec` that is run:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
LittleHorseBlockingStub client = ...;

WfRun result = client.runWf(RunWfRequest.newBuilder()
        .setWfSpecName("some-wf-spec")
        .setMajorVersion(2)
        .setRevision(1)
        .build());

```

  </TabItem>
  <TabItem value="go" label="Go">

```go
config := littlehorse.NewConfigFromEnv()
client, _ := config.GetGrpcClient()

var version int32
version = 2
var revision int32
revision = 1

result, err := (*client).RunWf(context.Background(), &lhproto.RunWfRequest{
    WfSpecName:   "some-wf-spec",
    MajorVersion: &version,
    Revision:     &revision,
})
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
config = LHConfig()
stub = config.stub()
stub.RunWf(RunWfRequest(wf_spec_name="some-wf-spec", majorVersion=2, revision=1))
```

  </TabItem>
</Tabs>


## Passing the ID

You can pass the `wfRunId` in the `RunWfRequest` to pre-specify the ID of a `WfRun`. **We recommend you always do this as a best practice** because:

1. This makes your requests idempotent and safe to retry, because LittleHorse guarantees that only one `WfRun` can exist with a given ID.
2. Specifying an ID makes it easier to correlate `ExternalEvent`s or records in a database.

To do so, just set the `Id` field in the `RunWfRequest` proto.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
LittleHorseBlockingStub client = ...;

WfRun result = client.runWf(RunWfRequest.newBuilder()
        .setWfSpecName("some-wf-spec")
        .setId("my-wfrun-id")
        .build());

```

  </TabItem>
  <TabItem value="go" label="Go">

```go
config := littlehorse.NewConfigFromEnv()
client, _ := config.GetGrpcClient()

var wfRunId string
wfRunId = "my-wfrun-id"

result, err := (*client).RunWf(context.Background(), &lhproto.RunWfRequest{
    WfSpecName: "some-wf-spec",
    Id:         &wfRunId,
})
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
config = LHConfig()
stub = config.stub()
stub.RunWf(RunWfRequest(wf_spec_name="some-wf-spec", id="my-wfrun-id"))
```

  </TabItem>
</Tabs>


## Parameters

You can pass variables into a `WfRun`. To do so, set the `variables` field on the `RunWfRequest`.

<Tabs>
  <TabItem value="java" label="Java" default>

In Java, the `LHLibUtil#objToVarVal` method is a useful convenience function to convert any object into a `VariableValue`.

```java
LittleHorseBlockingStub client = ...;

WfRun result = client.runWf(RunWfRequest.newBuilder()
        .setWfSpecName("some-wf-spec")
        .putVariables("my-int-var", LHLibUtil.objToVarVal(1234))
        .putVariables("my-str-var", LHLibUtil.objToVarVal("asdf"))
        .build());
```

  </TabItem>
  <TabItem value="go" label="Go">

The Go SDK has a useful `littlehorse.InterfaceToVarVal()` function which converts an arbitrary Go interface into a `VariableValue`.

```go
config := littlehorse.NewConfigFromEnv()
client, _ := config.GetGrpcClient()

var wfRunId string
wfRunId = "my-wfrun-id"

stringVar, err := littlehorse.InterfaceToVarVal("some-string")
intVar, err := littlehorse.InterfaceToVarVal(1234)

result, err := (*client).RunWf(context.Background(), &lhproto.RunWfRequest{
	WfSpecName: "some-wf-spec",
	Variables: map[string]*lhproto.VariableValue{
		"my-string-var": stringVar,
		"my-int-var": intVar,
	},
})
```

  </TabItem>
  <TabItem value="python" label="Python">

In python you can use `littlehorse.to_variable_value()` utility.

```python
config = LHConfig()
stub = config.stub()
request = RunWfRequest(
    wf_spec_name="some-wf-spec",
    id="my-wfrun-id",
    variables={
        "my-string-var": littlehorse.to_variable_value("ABCD"),
        "my-int-var": littlehorse.to_variable_value(1234),
    },
)
stub.RunWf(request)
```

  </TabItem>
</Tabs>

## Scheduling

You can schedule `WfRun`s using Cron expressions.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
LittleHorseBlockingStub client = ...;

ScheduledWfRun result = client.scheduleWf(ScheduleWfRequest.newBuilder()
        .setWfSpecName("some-wf-spec")
        .setMajorVersion(0)
        .setRevision(2)
        .setCronExpression("5 4 * * *")
        .putVariables("my-int-var", LHLibUtil.objToVarVal(1234))
        .build());
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
config := littlehorse.NewConfigFromEnv()
client, _ := config.GetGrpcClient()

var wfRunId string
wfRunId = "my-wfrun-id"

stringVar, err := littlehorse.InterfaceToVarVal("some-string")
intVar, err := littlehorse.InterfaceToVarVal(1234)

result, err := (*client).ScheduleWf(context.Background(), &lhproto.ScheduleWfRequest{
	WfSpecName: "some-wf-spec",
	CronExpression: "5 4 * * *",
	Variables: map[string]*lhproto.VariableValue{
		"my-string-var": stringVar,
	},
})
```

  </TabItem>
  <TabItem value="python" label="Python">


```python
config = LHConfig()
stub = config.stub()
request = ScheduleWfRequest(
    wf_spec_name="some-wf-spec",
    cron_expression="5 4 * * *",
    variables={
        "name": littlehorse.to_variable_value("bob"),
    },
)
stub.RunWf(request)
```

  </TabItem>
</Tabs>

Note that you are not required to pick a `WfSpec` version; if you don't, the most current version will be used for every run.