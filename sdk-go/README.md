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

## Run tests

```
go test -v ./...
```

## Code Formatter 

```
go fmt ./...
```
