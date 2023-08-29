FROM ubuntu:22.04

ENV PROTOC_VERSION="23.4"

RUN apt update && \
    apt install -y --no-install-recommends python3 pip wget ca-certificates unzip golang && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    wget -q https://github.com/protocolbuffers/protobuf/releases/download/v23.4/protoc-23.4-linux-x86_64.zip -O /tmp/protoc.zip && \
    unzip -d /usr/local/ /tmp/protoc.zip && \
    wget -q https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.57.2/protoc-gen-grpc-java-1.57.2-linux-x86_64.exe -O /usr/local/bin/protoc-gen-grpc-java && \
    GOBIN=/usr/local/bin go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.31.0 && \
    GOBIN=/usr/local/bin go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.3.0 && \
    pip install grpcio-tools==1.57.0 && \
    chmod +x /usr/local/bin/* && \
    rm -f /tmp/*
