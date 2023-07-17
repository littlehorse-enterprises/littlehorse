## Quick Start for Java

### Prerequisites

- `openjdk`, preferably version 17 or later.
- `gradle`, preferably version 7.4 or later.
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

Start a LH Server with:

```
cd docker
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
  "code":  "OK",
  "objectIds":  []
}
```

Now let's run an example:

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

# This will show you all nodes in tha run
lhctl get nodeRun <wf run id> 0 1

# This shows the task run information
lhctl get taskRun <wf run id> <task run global id>
```
