## Quick Start for Java

### Prerequisites

- `docker`.
- `go`.

### Running Locally

Install `lhctl`:

```
go install github.com/littlehorse-enterprises/littlehorse/lhctl@latest
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
docker run --name littlehorse -d -p 2023:2023 public.ecr.aws/k7z9g8i4/littlehorse-standalone:latest
```

> This example is intended to be used in development environments.

> To persist the data, add an option volume: `-v lh-data:/data`.

When you run the LH Server according to the command above, the API Host is `localhost` and the API Port is `2023`.

Now configure open the configuration `~/.config/littlehorse.config`:

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

Now you are ready to run your workflows.

### Using Docker Compose

If you need more flexibility to experiment with other settings you can our [docker-compose example](../examples/docker-compose).
