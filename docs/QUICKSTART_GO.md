## Quick Start for Go

### Prerequisites

- `docker` and `docker-compose-plugin`.
- `go`.

### Running Locally

Install `lhctl`:

```
cd lhctl
go install .
```

> Make sure it's on the path <br />
> `export GOPATH="$(go env GOPATH)"` <br />
> `export PATH="$PATH:$GOPATH/bin"`

Verify the installation:

```
lhctl
```

Start your LH Server with:

```
cd examples/docker-compose
docker compose up -d
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

> More examples at [sdk-go/examples](../sdk-go/examples).

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
