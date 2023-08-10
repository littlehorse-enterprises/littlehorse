import os
from pathlib import Path
from typing import Optional, Union
from jproperties import Properties
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


class Config:
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
        """Gets a configuration or return a default instead.

        Args:
            key (str): COnfiguration key.
            default (Optional[str], optional): Default value in case the configuration
            does not exist. Defaults to None.

        Returns:
            Optional[str]: The con configuration's value or the given default value.
        """
        value = self.configs.get(key)
        return value if value else default

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
        """Returns True is a secure connection is configured.

        Returns:
            bool: True if a secure connection is expected.
        """
        return bool(self.get(CA_CERT))
