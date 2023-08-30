from inspect import signature
import inspect
from pathlib import Path
from typing import Any, Callable, Union
from littlehorse.model.service_pb2 import PutWfSpecRequest
from google.protobuf.json_format import MessageToJson


class NodeOutput:
    def __init__(self) -> None:
        pass


class ThreadBuilder:
    def __init__(self) -> None:
        pass

    def execute(self, task_name: str, *args: Any) -> NodeOutput:
        return NodeOutput()


ThreadInitializer = Callable[[ThreadBuilder], None]


class Workflow:
    def __init__(self, name: str, initializer: ThreadInitializer) -> None:
        if name is None:
            raise ValueError("Name cannot be None")
        self.name = name

        self._validate_initializer(initializer)
        self._initializer = initializer

    def _validate_initializer(self, initializer: ThreadInitializer) -> None:
        if initializer is None:
            raise ValueError("ThreadInitializer cannot be None")

        if not inspect.isfunction(initializer):
            raise ValueError("Object is not a ThreadInitializer")

        sig = signature(initializer)

        if len(sig.parameters) != 1:
            raise ValueError("ThreadInitializer receives only one parameter")

        if list(sig.parameters.values())[0].annotation is not ThreadBuilder:
            raise ValueError("ThreadInitializer receives a ThreadBuilder")

        if sig.return_annotation is not None:
            raise ValueError("ThreadInitializer returns None")

    def compile(self) -> PutWfSpecRequest:
        spec = PutWfSpecRequest(name=self.name)
        return spec

    def save(self, file_path: Union[str, Path]) -> None:
        with open(file_path, "w") as file_output:
            file_output.write(str(self))

    def __str__(self) -> str:
        return MessageToJson(self.compile())


if __name__ == "__main__":

    def my_thread_builder(thread: ThreadBuilder) -> None:
        thread.execute("my-task", "1")

    wf = Workflow("my-wf", my_thread_builder)
    print(wf)
