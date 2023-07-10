# LittleHorse GoLang Library

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.dev). In the Developer Guide, you will find detailed instructions for installing and using `lhctl` and the Go Client Library.

This README is intended for developing.

## Prerequisites (for developing)

### Dependencies

Install golang.

Install Golang protobuf compilers, as follows:

```
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.2
go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.28
```

### Repository Location

You should also move this repository to the following location: `${GOPATH}/src/bitbucket.org/littlehorse-core/littlehorse/sdk-go`.

### Setup Pre-commit

```bash
pre-commit install
```

### Protobuf Compilation

In LittleHorse, protobuf definitions are kept in a separate repository as they are used across various other projects (eg. golang and java clients). We access that via Git SubModules.

```
git submodule init
git submodule update
cd lh-proto
git checkout master
git pull
cd ..
```

Finally, compile the protobuf:

```
./local-dev/compile-proto.sh
```
