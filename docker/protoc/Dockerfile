FROM ubuntu:22.04

ENV PROTOC_VERSION="23.4"

RUN apt update && \
    apt install -y --no-install-recommends python3 wget ca-certificates unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    wget -q https://github.com/protocolbuffers/protobuf/releases/download/v23.4/protoc-23.4-linux-x86_64.zip -O /tmp/protoc.zip && \
    unzip -d /usr/local/ /tmp/protoc.zip && \
    wget -q https://github.com/protocolbuffers/protobuf-go/releases/download/v1.31.0/protoc-gen-go.v1.31.0.linux.amd64.tar.gz -O /tmp/protoc-gen-go.tar.gz && \
    tar -xzf /tmp/protoc-gen-go.tar.gz -C /usr/local/bin/