# Workers/Clients Configurations
- [Workers/Clients Configurations](#workersclients-configurations)
  - [Client](#client)
    - [`LHC_API_HOST`](#lhc_api_host)
    - [`LHC_API_PORT`](#lhc_api_port)
    - [`LHC_CLIENT_ID`](#lhc_client_id)
    - [`LHC_CA_CERT`](#lhc_ca_cert)
    - [`LHC_CLIENT_CERT`](#lhc_client_cert)
    - [`LHC_CLIENT_KEY`](#lhc_client_key)
    - [`LHC_OAUTH_CLIENT_ID`](#lhc_oauth_client_id)
    - [`LHC_OAUTH_CLIENT_SECRET`](#lhc_oauth_client_secret)
    - [`LHC_OAUTH_AUTHORIZATION_SERVER`](#lhc_oauth_authorization_server)
  - [Worker](#worker)
    - [`LHW_SERVER_CONNECT_LISTENER`](#lhw_server_connect_listener)
    - [`LHW_NUM_WORKER_THREADS`](#lhw_num_worker_threads)
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

### `LHC_CLIENT_ID`

Unique client identifier. It is used by the LittleHorse cluster to load balance the worker requests across all servers.
It's highly recommended to set this value for production environments.

- **Type:** string
- **Default:** a random value
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

### `LHC_OAUTH_AUTHORIZATION_SERVER`

Optional Authorization Server URL. Used by the Worker to know the OpenId Connect endpoints.
It is mandatory if `LHC_OAUTH_CLIENT_ID` and `LHC_OAUTH_CLIENT_SECRET` are provided.

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

### `LHW_TASK_WORKER_VERSION`

Optional version identifier. Intended to be informative. Useful when you're running different version of a worker.

- **Type:** string
- **Default:** ""
- **Importance:** low
