from inspect import signature
import inspect
from typing import Any, Callable
from littlehorse.model.service_pb2 import PutWfSpecRequest


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

        if initializer is None:
            raise ValueError("ThreadInitializer cannot be None")

        self.name = name
        self._initializer = initializer

        # validate initializer
        self._validate_initializer(initializer)

    def _validate_initializer(self, initializer: ThreadInitializer) -> None:
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


if __name__ == "__main__":

    def my_thread_builder(thread: ThreadBuilder) -> None:
        thread.execute("my-task", "1")

    wf = Workflow("my-wf", my_thread_builder)
    print(wf.compile())
