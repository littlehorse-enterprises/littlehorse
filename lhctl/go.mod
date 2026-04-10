module github.com/littlehorse-enterprises/lhctl

go 1.24.0

toolchain go1.24.4

require (
	github.com/coreos/go-oidc/v3 v3.11.0
	github.com/littlehorse-enterprises/littlehorse v0.0.0
	github.com/pkg/browser v0.0.0-20240102092130-5ac0b6a4141c
	github.com/spf13/cobra v1.8.1
	golang.org/x/oauth2 v0.34.0
	google.golang.org/grpc v1.79.3
	google.golang.org/protobuf v1.36.10
)

replace github.com/littlehorse-enterprises/littlehorse v0.0.0 => ../

require (
	cloud.google.com/go/compute/metadata v0.9.0 // indirect
	github.com/go-jose/go-jose/v4 v4.1.3 // indirect
	github.com/google/uuid v1.6.0 // indirect
	github.com/inconshreveable/mousetrap v1.1.0 // indirect
	github.com/magiconair/properties v1.8.7 // indirect
	github.com/spf13/pflag v1.0.5 // indirect
	github.com/ztrue/tracerr v0.4.0 // indirect
	golang.org/x/net v0.48.0 // indirect
	golang.org/x/sys v0.39.0 // indirect
	golang.org/x/text v0.32.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20251202230838-ff82c1b0f217 // indirect
)
