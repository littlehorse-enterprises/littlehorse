from typing import Optional
from littlehorse.config import LHConfig
import grpc
from grpc import Channel
from littlehorse.model.service_pb2 import (
    LHResponseCodePb,
    WfSpecIdPb,
    WfSpecPb,
)

from littlehorse.model.service_pb2_grpc import LHPublicApiStub


class Client:
    def __init__(self, config: LHConfig) -> None:
        """
        Args:
            config (Config): Client configuration.
        """
        self.config = config

    def stablish_channel(self) -> Channel:
        """Open a RPC channel.

        Returns:
            Channel: A closable channel. Use 'with' or channel.close().
        """
        if self.config.is_secure():
            tls_credentials = grpc.ssl_channel_credentials(
                root_certificates=self.config.ca_cert(),
                private_key=self.config.client_key(),
                certificate_chain=self.config.client_cert(),
            )
            return grpc.secure_channel(
                self.config.bootstrap_server(), tls_credentials
            )

        return grpc.insecure_channel(self.config.bootstrap_server())

    def wf_spec(self, name: str, version: Optional[int] = None) -> Optional[WfSpecPb]:
        """Gets the workflow specification for a given workflow name and version.

        Args:
            name (str): Workflow name.
            version (Optional[int], optional): Version of the registered workflow.
            Defaults to None. Defaults to None.

        Returns:
            Optional[WfSpecPb]: A workflow specification with the workflow's
            data and status, or None if the spec does not exist.
        """
        with self.stablish_channel() as channel:
            stub = LHPublicApiStub(channel)
            id = WfSpecIdPb(name=name, version=version)
            reply = stub.GetWfSpec(id)

            if reply.code == LHResponseCodePb.NOT_FOUND_ERROR:
                return None

            return reply.result


if __name__ == "__main__":
    from pathlib import Path

    config = LHConfig()
    config.load(Path.home().joinpath(".config", "littlehorse.config"))

    client = Client(config)
    wf_spec = client.wf_spec("example-basic", 0)

    print(wf_spec.name if wf_spec else "Not found")
