# LittleHorse CLI

## Installing `lhctl`

1. To install `lhctl` from the source code, first run the following commands within the `/lhctl/` directory:

```bash
go work init
go work use ./../sdk-go
go work use .
```

> [!NOTE]
> This sets up a multi-module workspace in Go that links `sdk-go` to `lhctl`. This is necessary to use `lhctl`, as it depends on the `sdk-go` implementation of the LittleHorse client. Alternatively,  `lhctl` is **not** bundled alongside official releases of `sdk-go`.

2. Run the following command within the `/lhctl/` directory to update your local installation of `lhctl` whenever you make changes.

```
go install .
```

> Make sure it's on the path <br />
> `export GOPATH="$(go env GOPATH)"` <br />
> `export PATH="$PATH:$GOPATH/bin"`

Verify the installation:

```
lhctl
```
