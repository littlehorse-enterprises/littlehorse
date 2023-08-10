import os
import uuid
from pathlib import Path
from typing import Optional, Union
from grpc import Channel
import grpc
from jproperties import Properties
from littlehorse.model.service_pb2_grpc import LHPublicApiStub
from littlehorse.utils import read_binary

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


class LHConfig:
    """Littlehorse Client/Worker configuration.
    A property configured using an environment property
    overrides the value provided using a worker.config file.
    """

    def __init__(self) -> None:
        self.configs = {
            key.upper(): value
            for key, value in os.environ.items()
            if key.startswith(PREFIXES)
        }
        self.channel: Channel = None

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
        new_configs.update(self.configs)

        self.configs = new_configs

    def get(self, key: str, default: Optional[str] = None) -> Optional[str]:
        """Gets a configuration, or return a default instead. If a default value is
        passed and the configuration is None, then the default value will be set.

        Args:
            key (str): Configuration key.
            default (Optional[str], optional): Default value in case the configuration
            does not exist. Defaults to None.

        Returns:
            Optional[str]: The configuration's value, or the given default value.
        """
        value = self.configs.get(key)

        if not value and default:
            self.configs[key] = default
            return default

        return value

    def bootstrap_server(self) -> str:
        """Returns the LH Bootstrap server address.

        Returns:
            str: Bootstrap server. Default localhost:2023.
        """
        host = self.get(API_HOST, "localhost")
        port = self.get(API_PORT, "2023")
        return f"{host}:{port}"

    def ca_cert(self) -> Optional[bytes]:
        """Returns the CA Certificates.

        Returns:
            Optional[bytes]: Root certificates as a byte string,
            or None in case it is not configured.
        """
        cert_path = self.get(CA_CERT)
        return read_binary(cert_path) if cert_path else None

    def client_key(self) -> Optional[bytes]:
        """Returns the client certificate key. For MTLS.

        Returns:
            Optional[bytes]: Certificate key as a byte string.
        """
        client_key_path = self.get(CLIENT_KEY)
        return read_binary(client_key_path) if client_key_path else None

    def client_cert(self) -> Optional[bytes]:
        """Returns the client certificate. For MTLS.

        Returns:
            Optional[bytes]: Certificate as a byte string.
        """
        client_cert_path = self.get(CLIENT_CERT)
        return read_binary(client_cert_path) if client_cert_path else None

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
        return str(self.get(CLIENT_ID, random_id))

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

    def establish_channel(self) -> Channel:
        """Open a RPC channel. Returns a new channel.

        Returns:
            Channel: A closable channel. Use 'with' or channel.close().
        """
        if self.is_secure():
            tls_credentials = grpc.ssl_channel_credentials(
                root_certificates=self.ca_cert(),
                private_key=self.client_key(),
                certificate_chain=self.client_cert(),
            )
            return grpc.secure_channel(self.bootstrap_server(), tls_credentials)

        return grpc.insecure_channel(self.bootstrap_server())

    def blocking_stub(self) -> LHPublicApiStub:
        """Gets a Blocking gRPC stub for the LH Public API
        on the configured bootstrap server. It creates a new LHPublicApiStub,
        but reuse the grpc.Channel. It is assumed that the channel
        will not be closed until the execution of the main thread ends.

        Returns:
            LHPublicApiStub: A blocking gRPC stub.
        """
        self.channel = self.channel or self.establish_channel()
        return LHPublicApiStub(self.channel)


if __name__ == "__main__":
    from pathlib import Path
    from littlehorse.model.service_pb2 import WfSpecIdPb

    config_path = Path.home().joinpath(".config", "littlehorse.config")

    config = LHConfig()
    config.load(config_path)

    stub = config.blocking_stub()

    id = WfSpecIdPb(name="example-basic")
    reply = stub.GetWfSpec(id)

    print(reply.result.name)
