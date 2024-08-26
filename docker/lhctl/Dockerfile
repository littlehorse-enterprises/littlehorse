FROM golang:1.22-alpine

WORKDIR /lh
COPY . .
WORKDIR /lh/lhctl

# remove go.work file to avoid error
RUN rm -f go.work

RUN go work init && go work use ../sdk-go && go work use .
RUN GOBIN=/usr/local/bin go install .
WORKDIR /
RUN rm -rf /lh

ENTRYPOINT ["/usr/local/bin/lhctl"]
