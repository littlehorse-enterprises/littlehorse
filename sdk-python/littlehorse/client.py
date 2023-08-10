from littlehorse.config import Config
import grpc
from grpc import Channel
from littlehorse.model.service_pb2 import (
    LHResponseCodePb,
    WfSpecIdPb,
    WfSpecPb,
)

from littlehorse.model.service_pb2_grpc import LHPublicApiStub


class Client:
    def __init__(self, config: Config) -> None:
        self.config = config

    def stablish_channel(self) -> Channel:
        """Open a RPC channel.

        Returns:
            Channel: A closable channel. Use 'with' or channel.close().
        """
        return grpc.insecure_channel(self.config.bootstrap_server())

    def get_wf_spec(self, name: str, version: int | None = None) -> WfSpecPb | None:
        """Gets the workflow specification for a given workflow name and version.

        Args:
            name (str): Workflow name.
            version (int | None, optional): Version of the registered workflow.
            Defaults to None.

        Returns:
            WfSpecPb: A workflow specification with the workflow's data and status,
            or null if the spec does not exist.
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

    config = Config()
    config.load(Path.home().joinpath(".config", "littlehorse.config"))

    client = Client(config)
    wf_spec = client.get_wf_spec("example-basic", 0)

    print(wf_spec.name if wf_spec else "Not found")
