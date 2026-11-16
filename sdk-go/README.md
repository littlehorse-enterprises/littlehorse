# LittleHorse GoLang SDK

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.io/docs/server).

### Dependencies

Install Go 1.25 or newer.

Install the Go protobuf compilers as follows:

```
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
```

## Protobuf Compilation

```
../local-dev/compile-proto.sh
```

## Inline workflows

Use `NewInlineWorkflow` to execute a workflow without registering a WfSpec. It
supports the same `WorkflowThread` API as `NewWorkflow`:

```go
request, err := littlehorse.NewInlineWorkflow(func(thread *littlehorse.WorkflowThread) {
	thread.Execute("hello")
}).WithWfRunID("my-inline-run").Compile()
if err != nil {
	return err
}
run, err := client.RunInlineWf(ctx, request)
```

`WithWfRunID` is optional. Use `WithRetentionPolicy` to set workflow retention,
and set `request.Variables` to provide input values. TaskDefs and other referenced
metadata must already be registered. `Compile` only builds the request; it makes
no server calls.

## Run tests

```
go test -v ./...
```

## Code Formatter 

```
go fmt ./...
```
