FROM ubuntu:22.04
COPY . /lh
RUN apt update && apt install -y --no-install-recommends \
    jq git ca-certificates wget curl tar unzip golang && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /lh/lhctl
RUN GOBIN=/usr/local/bin go install .
WORKDIR /
RUN rm -rf /lh
