from enum import Enum
from inspect import signature
import inspect
from pathlib import Path
from typing import Any, Callable, Optional, Union
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import (
    IndexType,
    JsonIndex,
    TaskNode,
    VariableAssignment,
    VariableDef,
    VariableMutation,
    VariableMutationType,
)
from littlehorse.model.service_pb2 import PutWfSpecRequest
from littlehorse.model.variable_pb2 import VariableValue
from littlehorse.model.wf_spec_pb2 import (
    Edge,
    EntrypointNode,
    ExitNode,
    ExternalEventNode,
    Node,
    NopNode,
    SleepNode,
    StartThreadNode,
    ThreadSpec,
    UserTaskNode,
    WaitForThreadsNode,
)
from littlehorse.proto_utils import (
    to_variable_value,
    to_variable_assignment,
    to_json,
)

ENTRYPOINT = "entrypoint"

NodeType = Union[
    TaskNode,
    EntrypointNode,
    ExitNode,
    ExternalEventNode,
    SleepNode,
    StartThreadNode,
    WaitForThreadsNode,
    NopNode,
    UserTaskNode,
]


class NodeCase(Enum):
    ENTRYPOINT = "ENTRYPOINT"
    EXIT = "EXIT"
    TASK = "TASK"
    EXTERNAL_EVENT = "EXTERNAL_EVENT"
    START_THREAD = "START_THREAD"
    WAIT_FOR_THREADS = "WAIT_FOR_THREADS"
    NOP = "NOP"
    SLEEP = "SLEEP"
    USER_TASK = "USER_TASK"

    @classmethod
    def from_node(cls, node: NodeType) -> "NodeCase":
        if isinstance(node, TaskNode):
            return cls.TASK
        if isinstance(node, EntrypointNode):
            return cls.ENTRYPOINT
        if isinstance(node, ExitNode):
            return cls.EXIT
        if isinstance(node, ExternalEventNode):
            return cls.EXTERNAL_EVENT
        if isinstance(node, SleepNode):
            return cls.SLEEP
        if isinstance(node, StartThreadNode):
            return cls.START_THREAD
        if isinstance(node, WaitForThreadsNode):
            return cls.WAIT_FOR_THREADS
        if isinstance(node, NopNode):
            return cls.NOP
        if isinstance(node, UserTaskNode):
            return cls.USER_TASK

        raise TypeError("Unrecognized node type")


class FormatString:
    def __init__(self, format: str, *args: Any) -> None:
        """Generates a FormatString object that can be understood by the ThreadBuilder.

        Args:
            format (str): String format with variables with curly brackets {}.
            *args (Any): Arguments.

        Returns:
            FormatString: A FormatString.
        """
        self.format = format
        self.args = args


class NodeOutput:
    def __init__(self, node_name: str) -> None:
        self.node_name = node_name
        self._json_path: Optional[str] = None

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

    def with_json_path(self, json_path: str) -> "NodeOutput":
        """Valid only for output of the JSON_OBJ or JSON_ARR types. Returns a new
        NodeOutput handle which points to Json element referred to by the json path.

        Args:
            json_path (str): is the json path to evaluate.

        Returns:
            NodeOutput: Another NodeOutput.
        """
        if self.json_path is not None:
            raise ValueError("Cannot set a json_path twice on same var")

        out = NodeOutput(self.node_name)
        out.json_path = json_path
        return out


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
            self.default_value = to_variable_value(default_value)
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
        WfRunVariable handle which points to Json element referred to by the json path.

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
        return to_json(self.compile())


class ThreadBuilder:
    def __init__(self, workflow: "Workflow", initializer: "ThreadInitializer") -> None:
        """This is used to define the logic of a ThreadSpec in a ThreadInitializer.

        Args:
            workflow (Workflow): Parent.
            initializer (ThreadInitializer): Initializer.
        """
        self.wf_run_variables: list[WfRunVariable] = []
        self._workflow = workflow
        self._nodes: dict[str, Node] = {}

        if initializer is None:
            raise ValueError("None is not allowed")

        self.is_active = True
        self.add_node("entrypoint", EntrypointNode())
        initializer(self)
        self.add_node("exit", ExitNode())
        self.is_active = False

    def compile(self) -> ThreadSpec:
        """Compile this into Protobuf Objects.

        Returns:
            ThreadSpec: Spec.
        """
        variable_defs = [variable.compile() for variable in self.wf_run_variables]
        return ThreadSpec(variable_defs=variable_defs, nodes=self._nodes)

    def __str__(self) -> str:
        return to_json(self.compile())

    def _check_if_active(self) -> None:
        if not self.is_active:
            raise ReferenceError("Using an inactive thread, check your workflow")

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
        self._check_if_active()
        task_node = TaskNode(
            task_def_name=task_name,
            variables=[to_variable_assignment(arg) for arg in args],
        )
        node_name = self.add_node(task_name, task_node)
        return NodeOutput(node_name)

    def wait_for_event(self, event_name: str, timeout: int = -1) -> NodeOutput:
        """Adds an EXTERNAL_EVENT node which blocks until an
        'ExternalEvent' of the specified type arrives.

        Args:
            event_name (str): The name of ExternalEvent to wait for
            timeout (int, optional): Timeout in seconds. If
            it is 0 or less it does not set a timeout. Defaults to -1.

        Returns:
            NodeOutput: A NodeOutput for this event.
        """
        self._check_if_active()
        wait_node = ExternalEventNode(
            external_event_def_name=event_name,
            timeout_seconds=None if timeout <= 0 else to_variable_assignment(timeout),
        )
        node_name = self.add_node(event_name, wait_node)
        return NodeOutput(node_name)

    def mutate(
        self, left_hand: WfRunVariable, operation: VariableMutationType, right_hand: Any
    ) -> None:
        """Adds a VariableMutation to the last Node.

        Args:
            left_hand (WfRunVariable): It is a handle to the WfRunVariable to mutate.
            type (VariableMutationType): It is the mutation type to use,
            for example, `VariableMutationType.ASSIGN`.
            right_hand (Any): It is either a literal value
            (which the Library casts to a Variable Value), a
            `WfRunVariable` which determines the right hand side
            of the expression, or a `NodeOutput` (which allows you to
            use the output of a Node Run to mutate variables).
        """
        self._check_if_active()

        if len(self._nodes) == 0:
            raise ReferenceError("No node is found")

        previous_node_name = list(self._nodes)[-1]
        previous_node = self._nodes[previous_node_name]

        node_output: Optional[VariableMutation.NodeOutputSource] = None
        source_variable: Optional[VariableAssignment] = None
        literal_value: Optional[VariableValue] = None

        if isinstance(right_hand, NodeOutput):
            if previous_node_name != right_hand.node_name:
                raise ReferenceError("NodeOutput does not match with previous node")
            node_output = VariableMutation.NodeOutputSource(
                jsonpath=right_hand.json_path
            )
        elif isinstance(right_hand, WfRunVariable):
            source_variable = to_variable_assignment(right_hand)
        else:
            literal_value = to_variable_value(right_hand)

        mutation = VariableMutation(
            lhs_name=left_hand.name,
            lhs_json_path=left_hand.json_path,
            operation=operation,
            node_output=node_output,
            source_variable=source_variable,
            literal_value=literal_value,
        )

        previous_node.variable_mutations.append(mutation)

    def format(self, format: str, *args: Any) -> FormatString:
        """Generates a FormatString object that can be understood by the ThreadBuilder.

        Args:
            format (str): String format with variables with curly brackets {}.
            *args (Any): Arguments.

        Returns:
            FormatString: A FormatString.
        """
        return FormatString(format, *args)

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
        self._check_if_active()
        for var in self.wf_run_variables:
            if var.name == variable_name:
                raise ValueError(f"Variable {variable_name} already added")

        new_var = WfRunVariable(variable_name, variable_type, default_value)
        self.wf_run_variables.append(new_var)
        return new_var

    def add_node(self, name: str, sub_node: NodeType) -> str:
        """Add a given node.

        Args:
            name (str): Name of the node.
            sub_node (NodeType): One of node: [TaskNode, EntrypointNode,
            ExitNode, ExternalEventNode, SleepNode, StartThreadNode,
            WaitForThreadsNode,  NopNode, UserTaskNode]

        Returns:
            str: The name.
        """
        self._check_if_active()
        node_type = NodeCase.from_node(sub_node)
        next_node_name = f"{len(self._nodes)}-{name}-{node_type.name}"

        if len(self._nodes) == 0 and node_type != NodeCase.ENTRYPOINT:
            raise TypeError("The first node should be a EntrypointNode")

        if len(self._nodes) > 0:
            previous_node = self._nodes[list(self._nodes)[-1]]
            previous_node.outgoing_edges.append(Edge(sink_node_name=next_node_name))

            # TODO add node condition

        if node_type == NodeCase.TASK:
            self._nodes[next_node_name] = Node(task=sub_node)  # type: ignore[arg-type]  # noqa: E501
        if node_type == NodeCase.ENTRYPOINT:
            self._nodes[next_node_name] = Node(entrypoint=sub_node)  # type: ignore[arg-type]  # noqa: E501
        if node_type == NodeCase.EXIT:
            self._nodes[next_node_name] = Node(exit=sub_node)  # type: ignore[arg-type]  # noqa: E501
        if node_type == NodeCase.EXTERNAL_EVENT:
            self._nodes[next_node_name] = Node(external_event=sub_node)  # type: ignore[arg-type]  # noqa: E501
        if node_type == NodeCase.SLEEP:
            self._nodes[next_node_name] = Node(sleep=sub_node)  # type: ignore[arg-type]  # noqa: E501
        if node_type == NodeCase.START_THREAD:
            self._nodes[next_node_name] = Node(start_thread=sub_node)  # type: ignore[arg-type]  # noqa: E501
        if node_type == NodeCase.WAIT_FOR_THREADS:
            self._nodes[next_node_name] = Node(wait_for_threads=sub_node)  # type: ignore[arg-type]  # noqa: E501
        if node_type == NodeCase.NOP:
            self._nodes[next_node_name] = Node(nop=sub_node)  # type: ignore[arg-type]  # noqa: E501
        if node_type == NodeCase.USER_TASK:
            self._nodes[next_node_name] = Node(user_task=sub_node)  # type: ignore[arg-type]  # noqa: E501

        return next_node_name


ThreadInitializer = Callable[[ThreadBuilder], None]


class Workflow:
    def __init__(
        self, name: str, entrypoint: ThreadInitializer, retention_hours: int = -1
    ) -> None:
        """Workflow.

        Args:
            name (str): Name of WfSpec.
            entrypoint (ThreadInitializer):Is the entrypoint thread function.
            retention_hours (int, optional): Add the hours of life that the
            workflow will have in the system. Defaults to -1.
        """
        if name is None:
            raise ValueError("Name cannot be None")
        self.name = name
        self.retention_hours = retention_hours

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
        return to_json(self.compile())

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
            name=self.name,
            entrypoint_thread_name=ENTRYPOINT,
            thread_specs=thread_specs,
            retention_hours=None if self.retention_hours <= 0 else self.retention_hours,
        )
