FROM ubuntu:22.04
ENV PROTOC_VERSION          23.4
ENV PROTO_GEN_JAVA           1.57.2
ENV PROTO_GEN_GO             1.31.0
ENV PROTO_GEN_GO_GRPC        1.3.0
ENV PROTO_GEN_PYTHON         1.69.0
ENV PROTO_GEN_JS             1.178.0

ENV GOBIN /usr/local/bin

RUN apt update && apt install -y --no-install-recommends \
  python3 \
  git \
  pip \
  wget \
  ca-certificates \
  unzip \
  golang \
  nodejs \
  npm && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

# Install protoc
RUN set -x; \
  dpkgArch="$(uname -m)" && \
  case "$dpkgArch" in \
    x86_64) ARCH='x86_64';; \
    aarch64) ARCH='aarch_64';; \
  *) echo >&2 "error: unsupported architecture: $dpkgArch"; exit 1 ;; \
  esac && \
  wget -q https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOC_VERSION}/protoc-${PROTOC_VERSION}-linux-${ARCH}.zip -O /tmp/protoc.zip && \
  unzip /tmp/protoc.zip -d /usr/local && \
  rm /tmp/protoc.zip && \
  wget -q https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/${PROTO_GEN_JAVA}/protoc-gen-grpc-java-${PROTO_GEN_JAVA}-linux-${ARCH}.exe -O /usr/local/bin/protoc-gen-grpc-java

RUN chmod +x /usr/local/bin/* && \
  rm -f /tmp/*

  # Install protoc-gen-go
RUN go install google.golang.org/protobuf/cmd/protoc-gen-go@v${PROTO_GEN_GO} && \
  go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v${PROTO_GEN_GO_GRPC}

# Install protoc-gen-python
RUN pip3 install grpcio-tools==${PROTO_GEN_PYTHON}
# Install protoc-gen-js
RUN npm install -g ts-proto@${PROTO_GEN_JS}
