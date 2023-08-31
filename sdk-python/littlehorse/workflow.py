from inspect import signature
import inspect
from pathlib import Path
from typing import Any, Callable, Union
from littlehorse.model.common_wfspec_pb2 import VariableDef
from littlehorse.model.service_pb2 import PutWfSpecRequest
from littlehorse.model.wf_spec_pb2 import ThreadSpec
from littlehorse.utils import proto_to_json


class NodeOutput:
    def __init__(self) -> None:
        pass


class WfRunVariable:
    def __init__(self) -> None:
        pass

    def compile(self) -> VariableDef:
        spec = VariableDef()
        return spec


class ThreadBuilder:
    def __init__(self) -> None:
        pass

    def execute(self, task_name: str, *args: Any) -> NodeOutput:
        return NodeOutput()

    def add_variable(self, name: str, type_or_default: Any) -> WfRunVariable:
        return WfRunVariable()

    def compile(self) -> ThreadSpec:
        spec = ThreadSpec()
        return spec


ThreadInitializer = Callable[[ThreadBuilder], None]


class Workflow:
    def __init__(self, name: str, entrypoint: ThreadInitializer) -> None:
        """Workflow

        Args:
            name (str): Name of WfSpec.
            entrypoint (ThreadInitializer): Is the entrypoint thread function.
        """

        if name is None:
            raise ValueError("Name cannot be None")
        self.name = name

        self._validate_entrypoint(entrypoint)
        self._entrypoint = entrypoint

    def _validate_entrypoint(self, entrypoint: ThreadInitializer) -> None:
        if entrypoint is None:
            raise ValueError("ThreadInitializer cannot be None")

        if not inspect.isfunction(entrypoint):
            raise TypeError("Object is not a ThreadInitializer")

        sig = signature(entrypoint)

        if len(sig.parameters) != 1:
            raise TypeError("ThreadInitializer receives only one parameter")

        if list(sig.parameters.values())[0].annotation is not ThreadBuilder:
            raise TypeError("ThreadInitializer receives a ThreadBuilder")

        if sig.return_annotation is not None:
            raise TypeError("ThreadInitializer returns None")

    def save(self, file_path: Union[str, Path]) -> None:
        """Export the WorkflowSpec in JSON format.

        Args:
            file_path (Union[str, Path]): File location.
        """
        with open(file_path, "w") as file_output:
            file_output.write(str(self))

    def __str__(self) -> str:
        return proto_to_json(self.compile())

    def compile(self) -> PutWfSpecRequest:
        """Compile the workflow into Protobuf Objects.

        Returns:
            PutWfSpecRequest: Spec.
        """
        spec = PutWfSpecRequest(name=self.name, entrypoint_thread_name="entrypoint")
        return spec


"""
{
  "name": "example-basic",
  "threadSpecs": {
    "entrypoint": {
      "nodes": {
        "0-entrypoint-ENTRYPOINT": {
          "outgoingEdges": [{
            "sinkNodeName": "1-greet-TASK"
          }],
          "variableMutations": [],
          "failureHandlers": [],
          "entrypoint": {
          }
        },
        "1-greet-TASK": {
          "outgoingEdges": [{
            "sinkNodeName": "2-exit-EXIT"
          }],
          "variableMutations": [],
          "failureHandlers": [],
          "task": {
            "taskDefName": "greet",
            "timeoutSeconds": 0,
            "retries": 0,
            "variables": [{
              "variableName": "input-name"
            }]
          }
        },
        "2-exit-EXIT": {
          "outgoingEdges": [],
          "variableMutations": [],
          "failureHandlers": [],
          "exit": {
          }
        }
      },
      "variableDefs": [{
        "type": "STR",
        "name": "input-name",
        "jsonIndexes": []
      }],
      "interruptDefs": []
    }
  },
  "entrypointThreadName": "entrypoint"
}
"""

if __name__ == "__main__":

    def my_entrypoint(thread: ThreadBuilder) -> None:
        thread.execute("my-task", "1")

    wf = Workflow("my-wf", my_entrypoint)
    print(wf)
