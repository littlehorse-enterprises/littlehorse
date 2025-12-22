FROM golang:1.24-alpine AS builder

ARG VERSION=0.0.0-development
ARG COMMIT=HEAD

WORKDIR /lh
COPY go.mod go.sum ./
COPY lhctl/ lhctl/
COPY sdk-go/ sdk-go/

WORKDIR /lh/lhctl

RUN go build -ldflags "-X main.version=${VERSION} -X main.commit=${COMMIT}"

FROM scratch
COPY --from=builder /lh/lhctl/lhctl /usr/local/bin/lhctl

ENTRYPOINT ["/usr/local/bin/lhctl"]
