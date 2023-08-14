import os
import uuid
from pathlib import Path
from typing import Optional, Union
from grpc import CallCredentials, Channel, ChannelCredentials
import grpc
from jproperties import Properties
from littlehorse.auth import GrpcAuth
from littlehorse.model.service_pb2_grpc import LHPublicApiStub
from littlehorse.utils import read_binary
import logging

PREFIXES = ("LHC_", "LHW_")
API_HOST = "LHC_API_HOST"
API_PORT = "LHC_API_PORT"
CLIENT_ID = "LHC_CLIENT_ID"
CLIENT_CERT = "LHC_CLIENT_CERT"
CLIENT_KEY = "LHC_CLIENT_KEY"
CA_CERT = "LHC_CA_CERT"
OAUTH_CLIENT_ID = "LHC_OAUTH_CLIENT_ID"
OAUTH_CLIENT_SECRET = "LHC_OAUTH_CLIENT_SECRET"
OAUTH_AUTHORIZATION_SERVER = "LHC_OAUTH_AUTHORIZATION_SERVER"
NUM_WORKER_THREADS = "LHW_NUM_WORKER_THREADS"
SERVER_CONNECT_LISTENER = "LHW_SERVER_CONNECT_LISTENER"
TASK_WORKER_VERSION = "LHW_TASK_WORKER_VERSION"


class LHConfig:
    """Littlehorse Client/Worker configuration.
    A property configured using an environment property
    overrides the value provided using a worker.config file.
    """

    _log = logging.getLogger("LHConfig")

    def __init__(self) -> None:
        self.configs = {
            key.upper(): value
            for key, value in os.environ.items()
            if key.startswith(PREFIXES)
        }
        self._channel: Channel = None

    def __str__(self) -> str:
        return "\n".join(
            [
                f"{key}={'******' if 'SECRET' in key or 'PASSWORD' in key else value}"
                for key, value in self.configs.items()
            ]
        )

    def load(self, file_path: Union[str, Path]) -> None:
        """Loads configurations from properties file.

        Args:
            file_path (Union[str, Path]): Path to the properties file.
        """
        properties = Properties()
        properties.load(read_binary(file_path), "utf-8")

        new_configs = {
            key.upper(): value.data
            for key, value in properties.items()
            if key.startswith(PREFIXES)
        }

        self.configs = new_configs | self.configs

    def get(self, key: str) -> Optional[str]:
        """Gets a configuration.

        Args:
            key (str): Configuration key.

        Returns:
            Optional[str]: Configuration value or None if it does not exit.
        """
        return self.configs.get(key)

    def get_or_set_default(self, key: str, default: str) -> str:
        """Gets a configuration, or return a default instead. If the configuration
        does not exit, then the default value will be set.

        Args:
            key (str): Configuration key.
            default (str): Default value in case the configuration
            does not exist.

        Returns:
            str: The configuration's value, or the given default value.
        """
        value = self.configs.get(key)

        if value is None:
            self.configs[key] = default
            return default

        return value

    def bootstrap_server(self) -> str:
        """Returns the LH Bootstrap server address.

        Returns:
            str: Bootstrap server. Default localhost:2023.
        """
        host = self.get_or_set_default(API_HOST, "localhost")
        port = self.get_or_set_default(API_PORT, "2023")
        return f"{host}:{port}"

    def ca_cert(self) -> Optional[bytes]:
        """Returns the CA Certificates.

        Returns:
            Optional[bytes]: Root certificates as a byte string,
            or None in case it is not configured.
        """
        cert_path = self.get(CA_CERT)
        return None if cert_path is None else read_binary(cert_path)

    def client_key(self) -> Optional[bytes]:
        """Returns the client certificate key. For MTLS.

        Returns:
            Optional[bytes]: Certificate key as a byte string.
        """
        client_key_path = self.get(CLIENT_KEY)
        return None if client_key_path is None else read_binary(client_key_path)

    def client_cert(self) -> Optional[bytes]:
        """Returns the client certificate. For MTLS.

        Returns:
            Optional[bytes]: Certificate as a byte string.
        """
        client_cert_path = self.get(CLIENT_CERT)
        return None if client_cert_path is None else read_binary(client_cert_path)

    def is_secure(self) -> bool:
        """Returns True if a secure connection is configured.

        Returns:
            bool: True if a secure connection is expected.
        """
        return bool(self.get(CA_CERT))

    def client_id(self) -> str:
        """Returns a client id to identify an instance.

        Returns:
            str: A configured client id or a random string otherwise.
        """
        random_id = f"client-{str(uuid.uuid4()).replace('-', '')}"
        return str(self.get_or_set_default(CLIENT_ID, random_id))

    def oauth_client_id(self) -> Optional[str]:
        """Returns the configured OAuth2 client id. Used for OIDC authorization.

        Returns:
            str: The OAuth client id.
        """
        return self.get(OAUTH_CLIENT_ID)

    def needs_credentials(self) -> bool:
        """Returns True if OAuth is configured.

        Returns:
            bool: True if OAuth is configured.
        """
        return bool(self.get(OAUTH_CLIENT_ID))

    def oauth_client_secret(self) -> Optional[str]:
        """Returns the configured OAuth2 client secret. Used for OIDC authorization.

        Returns:
            str: The OAuth client secret.
        """
        return self.get(OAUTH_CLIENT_SECRET)

    def oauth_authorization_server(self) -> Optional[str]:
        """Returns the OAuth2 authorization server endpoint.
        Used for OIDC authorization.

        Returns:
            str: The OAuth2 authorization server endpoint.
        """
        return self.get(OAUTH_AUTHORIZATION_SERVER)

    def num_worker_threads(self) -> int:
        """Returns the number of worker threads to run.

        Returns:
            int: The number of worker threads to run. Default 8.
        """
        return int(self.get_or_set_default(NUM_WORKER_THREADS, "8"))

    def server_listener(self) -> str:
        """Returns the name of the listener to connect to.

        Returns:
            str: The name of the listener on the LH Server to connect to.
            Default PLAIN.
        """
        return self.get_or_set_default(SERVER_CONNECT_LISTENER, "PLAIN")

    def worker_version(self) -> str:
        """Returns the version of this worker.

        Returns:
            str: Task Worker Version. Default empty.
        """
        return self.get_or_set_default(TASK_WORKER_VERSION, "")

    def establish_channel(self, async_channel: bool = False) -> Channel:
        """Open a RPC channel. Returns a new channel.

        Returns:
            Channel: A closable channel. Use 'with' or channel.close().
        """
        secure_channel = grpc.secure_channel
        insecure_channel = grpc.insecure_channel

        if async_channel:
            self._log.debug("Establishing an async channel")
            secure_channel = grpc.aio.secure_channel
            insecure_channel = grpc.aio.insecure_channel

        def get_ssl_config() -> ChannelCredentials:
            return grpc.ssl_channel_credentials(
                root_certificates=self.ca_cert(),
                private_key=self.client_key(),
                certificate_chain=self.client_cert(),
            )

        def get_oauth_config() -> CallCredentials:
            return grpc.metadata_call_credentials(
                GrpcAuth(
                    client_id=self.oauth_client_id(),
                    client_secret=self.oauth_client_secret(),
                    authorization_server=self.oauth_authorization_server(),
                )
            )

        if self.is_secure() and self.needs_credentials():
            self._log.debug("Using secure channel with OAuth")
            return secure_channel(
                self.bootstrap_server(),
                grpc.composite_channel_credentials(
                    get_ssl_config(),
                    get_oauth_config(),
                ),
            )

        if self.is_secure():
            self._log.debug("Using secure channel")
            return secure_channel(
                self.bootstrap_server(),
                get_ssl_config(),
            )

        return insecure_channel(self.bootstrap_server())

    # TODO not sure about this method, should we just provide establish_channel
    # or both establish_channel and blocking_stub?
    def blocking_stub(self) -> LHPublicApiStub:
        """Gets a Blocking gRPC stub for the LH Public API
        on the configured bootstrap server. It creates a new LHPublicApiStub,
        but reuse the grpc.Channel. It is assumed that the channel
        will not be closed until the execution of the main thread ends.

        Returns:
            LHPublicApiStub: A blocking gRPC stub.
        """
        self._channel = self._channel or self.establish_channel()

        return LHPublicApiStub(self._channel)


if __name__ == "__main__":
    from pathlib import Path
    from littlehorse.model.service_pb2 import WfSpecIdPb

    logging.basicConfig(level=logging.DEBUG)

    config_path = Path.home().joinpath(".config", "littlehorse.config")

    config = LHConfig()
    config.load(config_path)

    stub = config.blocking_stub()
    id = WfSpecIdPb(name="example-basic")
    reply = stub.GetWfSpec(id)

    print(reply.result.name)
