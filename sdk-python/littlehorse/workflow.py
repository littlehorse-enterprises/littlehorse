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

ENTRYPOINT = "entrypoint"


class NodeOutput:
    def __init__(self) -> None:
        pass


class WfRunVariable:
    def __init__(
        self, variable_name: str, variable_type: VariableType, default_value: Any = None
    ) -> None:
        """Defines a Variable in the ThreadSpec and returns a handle to it.

        Args:
            variable_name (str): The name of the variable.
            variable_type (VariableType): The variable type.
            default_value (Any, optional): A default value. Defaults to None.

        Returns:
            WfRunVariable: A handle to the created WfRunVariable.

        Raises:
            TypeError: If variable_type and type(default_value) are not compatible.
        """
        self.name = variable_name
        self.type = variable_type
        self.default_value: Optional[VariableValue] = None
        self._json_path: Optional[str] = None
        self.index_type: Optional[IndexType] = None
        self.json_indexes: list[JsonIndex] = []

        if default_value is not None:
            self.default_value = parse_value(default_value)
            if self.default_value.type != self.type:
                raise TypeError(
                    f"Default value is not a {VariableType.Name(variable_type)}"
                )

    @property
    def json_path(self) -> Optional[str]:
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
        """Valid only for output of the JSON_OBJ or JSON_ARR types. Returns a new
        NodeOutput handle which points to Json element referred to by the json path.

        Args:
            json_path (str): is the json path to evaluate.

        Raises:
            ValueError: Is variable is not either JSON_OBJ or JSON_ARR.

        Returns:
            WfRunVariable: Another WfRunVariable.
        """
        if self.json_path is not None:
            raise ValueError("Cannot set a json_path twice on same var")

        if self.type != VariableType.JSON_OBJ and self.type != VariableType.JSON_ARR:
            raise ValueError(
                f"JsonPath not allowed in a {VariableType.Name(self.type)} variable"
            )

        out = WfRunVariable(self.name, self.type, self.default_value)
        out.json_path = json_path
        return out

    def with_index(self, index_type: IndexType) -> "WfRunVariable":
        """Enables the storage of variables with a Non-null. For enhanced efficiency,
        it offers two types of indexing: Remote Index and Local Index.
        IndexType.REMOTE_INDEX: This type of indexing is recommended for
        variables with low cardinality, which means they have relatively
        few distinct values. For example, storing userId. IndexType.LOCAL_INDEX:
        Local Index is designed for variables with high cardinality.

        Args:
            index_type (IndexType): Defines Local or Remote Index.

        Returns:
            WfRunVariable: Same instance.
        """
        self.index_type = index_type
        return self

    def with_json_index(self, json_path: str, index_type: IndexType) -> "WfRunVariable":
        """Enables the storage of specific attributes inside a Json Variable.
        For enhanced efficiency, it offers two types of indexing:
        Remote Index and Local Index. IndexType.REMOTE_INDEX: This
        type of indexing is recommended for variables with low
        cardinality, which means they have relatively few
        distinct values. For example, storing userId. IndexType.LOCAL_INDEX:
        Local Index is designed for variables with high cardinality.

        Args:
            json_path (str): Defines Local or Remote Index.
            index_type (IndexType): Json Attribute path starting with $. e.g: $.userId.

        Returns:
            WfRunVariable: Same instance.
        """
        if json_path is None or index_type is None:
            raise ValueError("None is not allowed")

        if not json_path.startswith("$."):
            raise ValueError(f"Invalid JsonPath: {json_path}")

        if self.type != VariableType.JSON_OBJ:
            raise ValueError(
                f"JsonPath not allowed in a {VariableType.Name(self.type)} variable"
            )

        self.json_indexes.append(JsonIndex(path=json_path, index_type=index_type))
        return self

    def compile(self) -> VariableDef:
        """Compile this into Protobuf Objects.

        Returns:
            VariableDef: Spec.
        """
        return VariableDef(
            type=self.type,
            name=self.name,
            index_type=self.index_type,
            default_value=self.default_value,
            json_indexes=self.json_indexes.copy(),
        )

    def __str__(self) -> str:
        return proto_to_json(self.compile())


class ThreadBuilder:
    def __init__(self, workflow: "Workflow", initializer: "ThreadInitializer") -> None:
        """This is used to define the logic of a ThreaSpec in a ThreadInitializer.

        Args:
            workflow (Workflow): Parent.
            initializer (ThreadInitializer): Initializer.
        """
        self.wf_run_variables: list[WfRunVariable] = []
        self._workflow = workflow
        initializer(self)

    def execute(self, task_name: str, *args: Any) -> NodeOutput:
        """Adds a TASK node to the ThreadSpec.

        Args:
            task_name (str): The name of the TaskDef to execute.
            *args (Any):  The input parameters to pass into the Task Run.
            If the type of an arg is a WfRunVariable, then that
            WfRunVariable is passed in as the argument; otherwise, the
            library will attempt to cast the provided argument to a
            LittleHorse VariableValue and pass that literal value in.

        Returns:
            NodeOutput: A NodeOutput for that TASK node.
        """
        return NodeOutput()

    def add_variable(
        self, variable_name: str, variable_type: VariableType, default_value: Any = None
    ) -> WfRunVariable:
        """Defines a Variable in the ThreadSpec and returns a handle to it.

        Args:
            variable_name (str): The name of the variable.
            variable_type (VariableType): The variable type.
            default_value (Any, optional): A default value. Defaults to None.

        Returns:
            WfRunVariable: A handle to the created WfRunVariable.
        """
        new_var = WfRunVariable(variable_name, variable_type, default_value)
        self.wf_run_variables.append(new_var)
        return new_var

    def compile(self) -> ThreadSpec:
        """Compile this into Protobuf Objects.

        Returns:
            ThreadSpec: Spec.
        """
        variable_defs = [variable.compile() for variable in self.wf_run_variables]
        return ThreadSpec(variable_defs=variable_defs)

    def __str__(self) -> str:
        return proto_to_json(self.compile())


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

        self._thread_initializers: list[tuple[str, ThreadInitializer]] = []

    def add_sub_thread(self, name: str, initializer: ThreadInitializer) -> str:
        """Add a subthread.

        Args:
            name (str): Name of the subthread.
            initializer (ThreadInitializer): Is a callable that initialize the thread.

        Raises:
            ValueError: If already exists.

        Returns:
            str: The name of the subthread.
        """
        for n, _ in self._thread_initializers:
            if n == name:
                raise ValueError(f"Thread {name} already added")

        self._thread_initializers.append((name, initializer))
        return name

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
        self.add_sub_thread(ENTRYPOINT, self._entrypoint)
        threads_iterator = iter(self._thread_initializers)
        thread_specs: dict[str, ThreadSpec] = {}

        for name, initializer in threads_iterator:
            builder = ThreadBuilder(self, initializer)
            thread_specs[name] = builder.compile()

        self._thread_initializers = []

        return PutWfSpecRequest(
            name=self.name, entrypoint_thread_name=ENTRYPOINT, thread_specs=thread_specs
        )


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
        thread.add_variable("input-name", VariableType.STR)

    wf = Workflow("my-wf", my_entrypoint)
    print(wf)
