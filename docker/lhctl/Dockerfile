FROM golang:1.22-alpine AS builder

ARG VERSION=0.0.0-development
ARG COMMIT=HEAD

WORKDIR /lh
COPY . .
WORKDIR /lh/lhctl

# remove go.work file to avoid error
RUN rm -f go.work

RUN go work init && go work use ../sdk-go && go work use .
RUN go build -ldflags "-X main.version=${VERSION} -X main.commit=${COMMIT}"

FROM scratch
COPY --from=builder /lh/lhctl/lhctl /usr/local/bin/lhctl

ENTRYPOINT ["/usr/local/bin/lhctl"]
