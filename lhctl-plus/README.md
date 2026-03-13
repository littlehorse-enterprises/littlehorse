# lhctl-plus
 
LittleHorse client TUI based on `lhctl`

## Prerequisites

- Go 1.20 or newer installed and on your `PATH`.
- A checked-out copy of the repository (this README lives in `lhctl-plus/`).

## Build

From the `lhctl-plus` directory, run:

```bash
# format sources, then build
gofmt -w . && go build -v -o lhctl-plus .
```

Or to install the binary to your `$GOBIN`/$GOPATH bin:

```bash
go install ./...
```

## Run

After building, run the binary from the same directory:

```bash
./lhctl-plus [flags] [args]
```

Or run directly with `go run` for quick iteration:

```bash
go run . -- [flags] [args]
```

## Development

- Format code:

```bash
gofmt -w .
```

- Run tests (if present):

```bash
go test ./... -v
```

## Notes

- This project follows the repository's conventions; when making changes that touch protobufs or other repo-wide artifacts, follow the root-level development instructions in the main repository README.
- 
- If you need to build with a specific `GOFLAGS` or `CGO_ENABLED`, set them in your environment before running `go build`.