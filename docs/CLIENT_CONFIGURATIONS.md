# Workers/Clients Configurations
- [Workers/Clients Configurations](#workersclients-configurations)
  - [Client](#client)
    - [`LHC_API_HOST`](#lhc_api_host)
    - [`LHC_API_PORT`](#lhc_api_port)
    - [`LHC_API_PROTOCOL`](#lhc_api_protocol)
    - [`LHC_CA_CERT`](#lhc_ca_cert)
    - [`LHC_CLIENT_CERT`](#lhc_client_cert)
    - [`LHC_CLIENT_KEY`](#lhc_client_key)
    - [`LHC_OAUTH_CLIENT_ID`](#lhc_oauth_client_id)
    - [`LHC_OAUTH_CLIENT_SECRET`](#lhc_oauth_client_secret)
    - [`LHC_OAUTH_ACCESS_TOKEN_URL`](#lhc_oauth_access_token_url)
    - [`LHC_GRPC_KEEPALIVE_TIME_MS`](#lhc_grpc_keepalive_time_ms)
    - [`LHC_GRPC_KEEPALIVE_TIMEOUT_MS`](#lhc_grpc_keepalive_timeout_ms)
  - [LHCTL](#lhctl)
    - [`LHC_OAUTH_SERVER_URL`](#lhc_oauth_server_url)
  - [Worker](#worker)
    - [`LHW_SERVER_CONNECT_LISTENER`](#lhw_server_connect_listener)
    - [`LHW_NUM_WORKER_THREADS`](#lhw_num_worker_threads)
    - [`LHW_TASK_WORKER_ID`](#lhw_task_worker_id)
    - [`LHW_TASK_WORKER_VERSION`](#lhw_task_worker_version)

## Client

### `LHC_API_HOST`

The bootstrap host for the LH Server.

- **Type:** string
- **Default:** localhost
- **Importance:** high

---

### `LHC_API_PORT`

The bootstrap port for the LH Server.

- **Type:** int
- **Default:** 2023
- **Importance:** high

---

### `LHC_API_PROTOCOL`

The bootstrap protocol for the LH Server. Valid values: `PLAINTEXT` and `TLS`.

- **Type:** string
- **Default:** PLAINTEXT
- **Importance:** high

---

### `LHC_CA_CERT`

Optional location of CA Cert file that issued the server side certificates. For TLS and mTLS connection.

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHC_CLIENT_CERT`

Optional location of Client Cert file for mTLS connection.

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHC_CLIENT_KEY`

Optional location of Client Private Key file for mTLS connection.

- **Type:** path
- **Default:** null
- **Importance:** low

---

### `LHC_OAUTH_CLIENT_ID`

Optional OAuth2 Client Id. Used by the Worker to identify itself at an Authorization Server. Client Credentials Flow.

- **Type:** string
- **Default:** null
- **Importance:** low

---

### `LHC_OAUTH_CLIENT_SECRET`

Optional OAuth2 Client Secret. Used by the Worker to identify itself at an Authorization Server. Client Credentials Flow.

- **Type:** string
- **Default:** null
- **Importance:** low

---

### `LHC_OAUTH_ACCESS_TOKEN_URL`

Optional Access Token URL provided by the OAuth Authorization Server. Used by the Worker to obtain a token using client credentials flow.
It is mandatory if `LHC_OAUTH_CLIENT_ID` and `LHC_OAUTH_CLIENT_SECRET` are provided.

- **Type:** url
- **Default:** null
- **Importance:** low

### `LHC_GRPC_KEEPALIVE_TIME_MS`

Time in milliseconds to configure keepalive pings on the grpc client.

- **Type:** int64
- **Default:** 45000 (45 seconds)
- **Importance:** low

### `LHC_GRPC_KEEPALIVE_TIMEOUT_MS`

Time in milliseconds to configure the timeout for the keepalive pings on the grpc client.

- **Type:** int64
- **Default:** 5000 (5 seconds)
- **Importance:** low


## LHCTL

### `LHC_OAUTH_SERVER_URL`

Optional Authorization Server URL. Used by the client to obtain a token using OAuth 2 authorization code credentials flow. It is used by OIDC to discover the server endpoints.

It is mandatory if `LHC_OAUTH_CLIENT_ID` is provided.

- **Type:** url
- **Default:** null
- **Importance:** low

## Worker

### `LHW_SERVER_CONNECT_LISTENER`

LH Server Listener to connect to.

- **Type:** string
- **Default:** PLAIN
- **Importance:** high

---

### `LHW_NUM_WORKER_THREADS`

The number of worker threads to run. It allows you to improve the task execution performance parallelizing the tasks
assigned to this worker.

- **Type:** int
- **Default:** 8
- **Importance:** medium

---

### `LHW_TASK_WORKER_ID`

Unique identifier for the Task Worker. It is used by the LittleHorse cluster to load balance the worker requests across all servers.
Additionally, it is journalled on every `TaskAttempt` run by the Task Worker, so that you can more easily debug where a request was executed from. It is recommended to set this value for production environments.

- **Type:** string
- **Default:** a random value
- **Importance:** medium

---

### `LHW_TASK_WORKER_VERSION`

Optional version identifier. Intended to be informative. Useful when you're running different version of a worker. Along with the `LHW_TASK_WORKER_ID`, this is journalled on every `TaskAttempt`.

- **Type:** string
- **Default:** ""
- **Importance:** low
