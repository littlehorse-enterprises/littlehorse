from inspect import signature
import inspect
from pathlib import Path
from typing import Any, Callable, Optional, Union
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import IndexType, JsonIndex, VariableDef
from littlehorse.model.service_pb2 import PutWfSpecRequest
from littlehorse.model.variable_pb2 import VariableValue
from littlehorse.model.wf_spec_pb2 import ThreadSpec
from littlehorse.utils import parse_value, proto_to_json


class NodeOutput:
    def __init__(self) -> None:
        pass


class WfRunVariable:
    def __init__(
        self, variable_name: str, variable_type: VariableType, default_value: Any = None
    ) -> None:
        self.name = variable_name
        self.type = variable_type
        self.default: Optional[VariableValue] = None
        self._json_path: Optional[str] = None
        self.index_type: Optional[IndexType] = None
        self.json_indexes: list[JsonIndex] = []

        if default_value is not None:
            self.default = parse_value(default_value)

            if self.default.type != self.type:
                raise TypeError(
                    f"Default value is not a {VariableType.Name(variable_type)}"
                )

    @property
    def json_path(self) -> Optional[str]:
        # if not json_path.startswith("$."):
        #     raise ValueError(f"Invalid JsonPath: {json_path}")
        return self._json_path

    @json_path.setter
    def json_path(self, json_path: str) -> None:
        if json_path is None:
            raise ValueError("None is not allowed")

        if not json_path.startswith("$."):
            raise ValueError(f"Invalid JsonPath: {json_path}. Use $. at the beginning")

        if self._json_path is not None:
            raise ValueError("Cannot set a json_path twice on same var")

        self._json_path = json_path

    def with_json_path(self, json_path: str) -> "WfRunVariable":
        if self.json_path is not None:
            raise ValueError("Cannot set a json_path twice on same var")

        out = WfRunVariable(self.name, self.type, self.default)
        out.json_path = json_path
        return out

    def with_index(self, index_type: IndexType) -> "WfRunVariable":
        self.index_type = index_type
        return self

    def with_json_index(self, json_path: str, index_type: IndexType) -> "WfRunVariable":
        if json_path is None or index_type is None:
            raise ValueError("None is not allowed")

        if not json_path.startswith("$."):
            raise ValueError(f"Invalid JsonPath: {json_path}")

        if self.type != VariableType.JSON_OBJ:
            raise ValueError(f"Non-Json {self.name} variable contains jsonIndex")

        self.json_indexes.append(JsonIndex(path=json_path, index_type=index_type))
        return self

    def compile(self) -> VariableDef:
        return VariableDef(type=self.type, name=self.name)


class ThreadBuilder:
    def __init__(self) -> None:
        pass

    def execute(self, task_name: str, *args: Any) -> NodeOutput:
        return NodeOutput()

    def add_variable(
        self, variable_name: str, variable_type: VariableType, default_value: Any = None
    ) -> WfRunVariable:
        return WfRunVariable(variable_name, variable_type, default_value)

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
