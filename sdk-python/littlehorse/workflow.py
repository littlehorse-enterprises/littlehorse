from inspect import signature
import inspect
from pathlib import Path
from typing import Any, Callable, Union
from littlehorse.model.service_pb2 import PutWfSpecRequest
from littlehorse.utils import proto_to_json


class NodeOutput:
    def __init__(self) -> None:
        pass


class ThreadBuilder:
    def __init__(self) -> None:
        pass

    def execute(self, task_name: str, *args: Any) -> NodeOutput:
        return NodeOutput()


ThreadEntrypoint = Callable[[ThreadBuilder], None]


class Workflow:
    def __init__(self, name: str, entrypoint: ThreadEntrypoint) -> None:
        """Workflow

        Args:
            name (str): Name of WfSpec.
            entrypoint (ThreadEntrypoint): Is the entrypoint thread function.
        """

        if name is None:
            raise ValueError("Name cannot be None")
        self.name = name

        self._validate_entrypoint(entrypoint)
        self._entrypoint = entrypoint

    def _validate_entrypoint(self, entrypoint: ThreadEntrypoint) -> None:
        if entrypoint is None:
            raise ValueError("ThreadEntrypoint cannot be None")

        if not inspect.isfunction(entrypoint):
            raise ValueError("Object is not a ThreadEntrypoint")

        sig = signature(entrypoint)

        if len(sig.parameters) != 1:
            raise ValueError("ThreadEntrypoint receives only one parameter")

        if list(sig.parameters.values())[0].annotation is not ThreadBuilder:
            raise ValueError("ThreadEntrypoint receives a ThreadBuilder")

        if sig.return_annotation is not None:
            raise ValueError("ThreadEntrypoint returns None")

    def compile(self) -> PutWfSpecRequest:
        """Compile the workflow into Protobuf Objects.

        Returns:
            PutWfSpecRequest: Spec.
        """
        spec = PutWfSpecRequest(name=self.name)
        return spec

    def save(self, file_path: Union[str, Path]) -> None:
        """Export the WorkflowSpec in JSON format.

        Args:
            file_path (Union[str, Path]): File location.
        """
        with open(file_path, "w") as file_output:
            file_output.write(str(self))

    def __str__(self) -> str:
        return proto_to_json(self.compile())


if __name__ == "__main__":

    def my_entrypoint(thread: ThreadBuilder) -> None:
        thread.execute("my-task", "1")

    wf = Workflow("my-wf", my_entrypoint)
    print(wf)
