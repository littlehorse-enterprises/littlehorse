## Quick Start for Go

### Prerequisites

- `docker`.
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

Start your LH Server with:

```
docker run --name littlehorse -d -p 2023:2023 ghcr.io/littlehorse-enterprises/littlehorse//littlehorse/lh-standalone:master
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

Now let's run an example inside the folder: `cd sdk-go/examples`

> More examples at [sdk-go/examples](../../sdk-go/examples).

Run a worker:

```
go run ./basic/worker
```

Register the WfSpec:

```
go run ./basic/deploy
```

In another terminal, use `lhctl` to run the workflow:

```
# Here, we specify that the "name" variable = "Obi-Wan"
lhctl run my-workflow name Obi-Wan
```

Now let's inspect the result:

```
# This call shows the result
lhctl get wfRun <wf run id>

# This will show you all nodes in tha run
lhctl get nodeRun <wf run id> 0 1

# This shows the task run information
lhctl get taskRun <wf run id> <task run global id>
```
