module github.com/littlehorse-enterprises/lhctl

go 1.21.3
toolchain go1.24.1

require (
	github.com/coreos/go-oidc/v3 v3.11.0
	github.com/littlehorse-enterprises/littlehorse v0.0.0
	github.com/pkg/browser v0.0.0-20240102092130-5ac0b6a4141c
	github.com/spf13/cobra v1.8.1
	golang.org/x/oauth2 v0.22.0
	google.golang.org/grpc v1.66.0
	google.golang.org/protobuf v1.34.2
)

replace github.com/littlehorse-enterprises/littlehorse v0.0.0 => ../

require (
	cloud.google.com/go/compute/metadata v0.5.0 // indirect
	github.com/go-jose/go-jose/v4 v4.0.4 // indirect
	github.com/google/uuid v1.6.0 // indirect
	github.com/inconshreveable/mousetrap v1.1.0 // indirect
	github.com/magiconair/properties v1.8.7 // indirect
	github.com/spf13/pflag v1.0.5 // indirect
	github.com/ztrue/tracerr v0.4.0 // indirect
	golang.org/x/crypto v0.35.0 // indirect
	golang.org/x/net v0.28.0 // indirect
	golang.org/x/sys v0.30.0 // indirect
	golang.org/x/text v0.22.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20240827150818-7e3bb234dfed // indirect
)
