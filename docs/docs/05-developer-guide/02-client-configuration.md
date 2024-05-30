# Configuring the Clients

In every LittleHorse SDK, there is an `LHConfig` class or struct that handles the configuration required for allowing your clients and Task Workers to talk to LittleHorse Server.

## Creating the `LHConfig`

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

You can create a config object in three ways (this is the same for `LHClientConfig` and `LHWorkerConfig`):

1. With no arguments, eg. `new LHClientConfig()`. This loads all configurations from the environment variables.
2. Passing in a properties object, eg. `new LHClientConfig(properties)`. This uses the configuration from the provided `Properties` object.
3. Passing in a path to a properties file, eg. `new LHClientConfig("/opt/littlehorse.config")`. This uses the configuration from the provided file.

The java class can be found at:
```java
import io.littlehorse.sdk.common.config.LHConfig;
```

  </TabItem>
  <TabItem value="go" label="Go">

You can create a config object in two ways:

1. Using the environment variables as defaults, via `common.NewConfigFromEnv()`.
2. Passing in a path to a properties file, via `common.NewConfigFromProps("/opt/littlehorse.config")`. This assumes that the file is in the format of a Java `Properties` file.

To import it, you can use the following:

```go
import "github.com/littlehorse-enterprises/littlehorse/sdk-go/common";
```

  </TabItem>
  <TabItem value="python" label="Python">

You can create a `LHConfig` in Python as follows:

```python
from littlehorse.config import LHConfig

def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config

config: LHConfig = get_config()
```

If you do not call `config.load(...)`, then the configurations are loaded from the environment variables.

  </TabItem>
</Tabs>

## Client Config Options

All of our SDK's share common configuration options. They are listed below:

- `LHC_API_HOST`
  - This is the host name that the client should use to connect to the LH Server. It is equivalent to `bootstrap.servers` in Kafka; on LittleHorse Cloud this host always points to a Load Balancer.
- `LHC_API_PORT`
  - This is the port that the client should connect to.
- `LHC_CLIENT_ID`
  - This is the client ID. This should be unique for every client.
- `LHC_CLIENT_KEY`
  - This is the path to the Client Private Key. Used for mTLS authentication.
- `LHC_CLIENT_CERT`
  - This is the path to the Client Public Key (Certificate). Used for mTLS authentication.
- `LHC_CA_CERT`
  - This is the path to the CA Certificate. Used for TLS.
- `LHC_OAUTH_CLIENT_ID`
  - This is the OAuth client ID. Used for OAuth authorization.
- `LHC_OAUTH_CLIENT_SECRET`
  - Optional OAuth2 Client Secret. Used by the Worker to identify itself at an Authorization Server. Client Credentials Flow.
- `LHC_OAUTH_ACCESS_TOKEN_URL`
  - Optional Access Token URL provided by the OAuth Authorization Server. Used by the Worker to obtain a token using client credentials flow. It is mandatory if `LHC_OAUTH_CLIENT_ID` and `LHC_OAUTH_CLIENT_SECRET` are provided.

The following configurations are used for the Task Workers (in addition to the above configuration options for all):
- `LHW_NUM_WORKER_THREADS`
  - The number of worker threads in the `Threadpool` which executes Task Runs.
- `LHW_TASK_WORKER_VERSION`
  - The version of this Task Worker. This is an optional config which is useful for keeping track of when you release new versions of your Task Workers.
- `LHW_SERVER_CONNECT_LISTENER`
  - The name of the Listener to connect to on the LH Server. This defaults to `PLAIN`.

The following is specific for `lhctl` cli:
- `LHC_OAUTH_SERVER_URL`
  - Optional Authorization Server URL. Used by the client to obtain a token using OAuth 2 authorization code credentials flow. It is used by OIDC to discover the server endpoints. It is mandatory if `LHC_OAUTH_CLIENT_ID` is provided.

## Configuring the CLI

By default, `lhctl` looks for a configuration file at `${HOME}/.config/littlehorse.config`. This configuration file tells `lhctl` where and how to connect to the LittleHorse Servers. An example `littlehorse.config` file looks like:

```filename: ${HOME}/.config/littlehorse.config
LHC_API_HOST=localhost
LHC_API_PORT=2023

LHW_SERVER_CONNECT_LISTENER=PLAIN
```

The `LHC_API_HOST` and `LHC_API_PORT` determine the endpoint that `lhctl` connects to. The `LHW_SERVER_CONNECT_LISTENER` is a config used by the Task Worker to tell the servers which listener to advertise to it. More details can be found in the configuration docs.

If you have properly installed `lhctl` and configured it to point to a running LittleHorse Server, you should be able to run the following command:

```
-> lhctl search wfSpec
{
  "results":  []
}
```

## Getting a GRPC Client

In an effort to not reinvent the wheel, LittleHorse exposes a grpc API to its users. The `LHConfig` allows you to get a grpc client.



<Tabs>
  <TabItem value="java" label="Java" default>

You can get an async grpc stub as follows:

```
LHConfig config = new LHConfig();
LittleHorseStub client = config.getAsyncStub();
```

You can get a blocking grpc stub as follows:

```
LHConfig config = new LHConfig();
LittleHorseBlockingStub client = config.getBlockingStub();
```

  </TabItem>
  <TabItem value="go" label="Go">

In Go, you can get a grpc client as follows:

```
config := common.NewConfigFromEnv()
client := config.getGrpcClient()
```

Note that in grpc in Go, there is no distinction between async or blocking grpc clients.

  </TabItem>
  <TabItem value="python" label="Python">
In python, you can get a grpc client as follows:

```
config = LHConfig()
blocking_stub = config.stub(async_channel=False)
async_stub = config.stub(async_channel=True)
```

  </TabItem>
</Tabs>
