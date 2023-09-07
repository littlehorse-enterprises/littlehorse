## Quick Start for Java

### Prerequisites

- `java`
    - [sdk-java](sdk-java): Java 11
    - [server](server): Java 17
- `gradle`, preferably version 8 or later.
- `docker` and `docker-compose-plugin`.
- `go`.

### Running Locally

Install `lhctl`:

```
cd lhctl
go install .
```

Update your path:

```
export GOPATH="$(go env GOPATH)"
export PATH="$PATH:$GOPATH/bin"
```

Verify the installation:

```
lhctl
```

Start a LH Server with:

```
docker run --name littlehorse -d -p 2023:2023 public.ecr.aws/littlehorse/lh-standalone:latest
```

When you run the LH Server according to the command above, the API Host is `localhost` and the API Port is `2023`.
Now configure `~/.config/littlehorse.config`:

```
LHC_API_HOST=localhost
LHC_API_PORT=2023
```

You can confirm that the Server is running via:

```
lhctl search wfSpec
```

Result:

```
{
  "results": []
}
```

Now let's run an example. This example lives in the `examples/basic` folder, and is a "Hello World" example of LittleHorse.

> More examples at [examples](../examples).

```
gradle example-basic:run
```

In another terminal, use `lhctl` to run the workflow:

```
# Here, we specify that the "input-name" variable = "Obi-Wan"
lhctl run example-basic input-name Obi-Wan
```

Now let's inspect the result:

```
# This call shows the result
lhctl get wfRun <wf run id>

# Inspect the first NodeRun of the WfRun
lhctl get nodeRun <wf run id> 0 1

# This shows the task run information
lhctl get taskRun <wfRunId> <taskGuid>
```
