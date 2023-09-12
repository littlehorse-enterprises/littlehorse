from enum import Enum
from inspect import signature
import inspect
from pathlib import Path
from typing import Any, Callable, Optional, Union
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import (
    Comparator,
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
    EdgeCondition,
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
    negate_comparator,
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


class WorkflowCondition:
    def __init__(self, left_hand: Any, comparator: Comparator, right_hand: Any) -> None:
        """Returns a WorkflowCondition that can be used in
        `ThreadBuilder.doIf()` or `ThreadBuilder.doElse()`.

        Args:
            left_hand (Any): is either a literal value
            (which the Library casts to a Variable Value) or a
            `WfRunVariable` representing the LHS of the expression.
            comparator (Comparator): is a Comparator defining the
            comparator, for example, `ComparatorTypePb.EQUALS`.
            right_hand (Any): is either a literal value
            (which the Library casts to a Variable Value) or a
            `WfRunVariable` representing the RHS of the expression.
        """
        self.left_hand = left_hand
        self.comparator = comparator
        self.right_hand = right_hand

    def negate(self) -> "WorkflowCondition":
        """Negates a comparator:

        Comparator.LESS_THAN => Comparator.GREATER_THAN_EQ
        Comparator.GREATER_THAN_EQ => Comparator.LESS_THAN
        Comparator.GREATER_THAN => Comparator.LESS_THAN_EQ
        Comparator.LESS_THAN_EQ => Comparator.GREATER_THAN
        Comparator.IN => Comparator.NOT_IN
        Comparator.NOT_IN => Comparator.IN
        Comparator.EQUALS => Comparator.NOT_EQUALS
        Comparator.NOT_EQUALS => Comparator.EQUALS

        Returns:
            WorkflowCondition: A condition.
        """
        return WorkflowCondition(
            self.left_hand, negate_comparator(self.comparator), self.right_hand
        )

    def __str__(self) -> str:
        return to_json(self.compile())

    def compile(self) -> EdgeCondition:
        """Compile this into Protobuf Objects.

        Returns:
            EdgeCondition: Spec.
        """
        return EdgeCondition(
            comparator=self.comparator,
            left=to_variable_assignment(self.left_hand),
            right=to_variable_assignment(self.right_hand),
        )


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
        self._persistent = False

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

    def persistent(self) -> "WfRunVariable":
        self._persistent = True
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
            persistent=self._persistent,
        )

    def __str__(self) -> str:
        return to_json(self.compile())


class WorkflowNode:
    def __init__(
        self,
        name: str,
        node_case: NodeCase,
        sub_node: NodeType,
    ) -> None:
        self.name = name
        self.sub_node = sub_node
        self.node_case = node_case
        self.outgoing_edges: list[Edge] = []
        self.variable_mutations: list[VariableMutation] = []

    def __str__(self) -> str:
        return to_json(self.compile())

    def _find_outgoing_edge(self, sink_node_name: str) -> Edge:
        for edge in self.outgoing_edges:
            if sink_node_name == edge.sink_node_name:
                return edge

        raise ValueError("Edge not found")

    def compile(self) -> Node:
        """Compile this into Protobuf Objects.

        Returns:
            Node: Spec.
        """

        def new_node(**kwargs: Any) -> Node:
            return Node(
                outgoing_edges=self.outgoing_edges,
                variable_mutations=self.variable_mutations,
                **kwargs,
            )

        if self.node_case == NodeCase.TASK:
            return new_node(task=self.sub_node)
        if self.node_case == NodeCase.ENTRYPOINT:
            return new_node(entrypoint=self.sub_node)
        if self.node_case == NodeCase.EXIT:
            return new_node(exit=self.sub_node)
        if self.node_case == NodeCase.EXTERNAL_EVENT:
            return new_node(external_event=self.sub_node)
        if self.node_case == NodeCase.SLEEP:
            return new_node(sleep=self.sub_node)
        if self.node_case == NodeCase.START_THREAD:
            return new_node(start_thread=self.sub_node)
        if self.node_case == NodeCase.WAIT_FOR_THREADS:
            return new_node(wait_for_threads=self.sub_node)
        if self.node_case == NodeCase.NOP:
            return new_node(nop=self.sub_node)
        if self.node_case == NodeCase.USER_TASK:
            return new_node(user_task=self.sub_node)

        raise ValueError("Node type not supported")


class ThreadBuilder:
    def __init__(self, workflow: "Workflow", initializer: "ThreadInitializer") -> None:
        """This is used to define the logic of a ThreadSpec in a ThreadInitializer.

        Args:
            workflow (Workflow): Parent.
            initializer (ThreadInitializer): Initializer.
        """
        self.wf_run_variables: list[WfRunVariable] = []
        self._workflow = workflow
        self._nodes: list[WorkflowNode] = []

        if initializer is None:
            raise ValueError("None is not allowed")

        self._validate_initializer(initializer)

        self.is_active = True
        self.add_node("entrypoint", EntrypointNode())
        initializer(self)
        self.add_node("exit", ExitNode())
        self.is_active = False

    def _validate_initializer(self, initializer: "ThreadInitializer") -> None:
        if initializer is None:
            raise ValueError("ThreadInitializer cannot be None")

        if not inspect.isfunction(initializer) and not inspect.ismethod(initializer):
            raise TypeError("Object is not a ThreadInitializer")

        sig = signature(initializer)

        if len(sig.parameters) != 1:
            raise TypeError("ThreadInitializer receives only one parameter")

        if list(sig.parameters.values())[0].annotation is not ThreadBuilder:
            raise TypeError("ThreadInitializer receives a ThreadBuilder")

        if sig.return_annotation is not None:
            raise TypeError("ThreadInitializer returns None")

    def compile(self) -> ThreadSpec:
        """Compile this into Protobuf Objects.

        Returns:
            ThreadSpec: Spec.
        """
        variable_defs = [variable.compile() for variable in self.wf_run_variables]
        nodes = {node.name: node.compile() for node in self._nodes}
        return ThreadSpec(
            variable_defs=variable_defs,
            nodes=nodes,
        )

    def __str__(self) -> str:
        return to_json(self.compile())

    def _check_if_active(self) -> None:
        if not self.is_active:
            raise ReferenceError("Using an inactive thread, check your workflow")

    def _last_node(self) -> WorkflowNode:
        if len(self._nodes) == 0:
            raise ReferenceError("No node found")
        return self._nodes[-1]

    def _find_node(self, name: str) -> WorkflowNode:
        for node in self._nodes:
            if node.name == name:
                return node
        raise ReferenceError("Node not found")

    def _find_next_node(self, name: str) -> WorkflowNode:
        nodes_count = len(self._nodes)
        for i, node in enumerate(self._nodes, 1):
            if node.name == name and i < nodes_count:
                return self._nodes[i]
        raise ReferenceError("Next node not found")

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
        last_node = self._last_node()

        node_output: Optional[VariableMutation.NodeOutputSource] = None
        source_variable: Optional[VariableAssignment] = None
        literal_value: Optional[VariableValue] = None

        if isinstance(right_hand, NodeOutput):
            if last_node.name != right_hand.node_name:
                raise ReferenceError("NodeOutput does not match with last node")
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

        last_node.variable_mutations.append(mutation)

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

    def find_variable(self, variable_name: str) -> WfRunVariable:
        """Search for a variable.

        Args:
            variable_name (str): he name of the variable.

        Returns:
            WfRunVariable: Variable found.
        """
        # TODO look in all threads
        for var in self.wf_run_variables:
            if var.name == variable_name:
                return var

        raise ValueError(f"Variable {variable_name} not found")

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
            last_node = self._last_node()
            last_node.outgoing_edges.append(Edge(sink_node_name=next_node_name))

        self._nodes.append(WorkflowNode(next_node_name, node_type, sub_node))

        return next_node_name

    def condition(
        self, left_hand: Any, comparator: Comparator, right_hand: Any
    ) -> WorkflowCondition:
        """Returns a WorkflowCondition that can be used in
        `ThreadBuilder.doIf()` or `ThreadBuilder.doElse()`.

        Args:
            left_hand (Any): is either a literal value
            (which the Library casts to a Variable Value) or a
            `WfRunVariable` representing the LHS of the expression.
            comparator (Comparator): is a Comparator defining the
            comparator, for example, `ComparatorTypePb.EQUALS`.
            right_hand (Any): is either a literal value
            (which the Library casts to a Variable Value) or a
            `WfRunVariable` representing the RHS of the expression.

        Returns:
            WorkflowCondition: a WorkflowCondition.
        """
        return WorkflowCondition(left_hand, comparator, right_hand)

    def do_if(
        self,
        condition: WorkflowCondition,
        if_body: "ThreadInitializer",
        else_body: Optional["ThreadInitializer"] = None,
    ) -> None:
        """Conditionally executes some workflow code; equivalent
        to an if() statement in programming.

        Args:
            condition (WorkflowCondition): is the WorkflowCondition
            to be satisfied.
            if_body (ThreadInitializer): is the block of
            ThreadSpec code to be executed if the provided
            WorkflowCondition is satisfied.
            else_body (ThreadInitializer): is the block of
            ThreadSpec code to be executed if the provided
            WorkflowCondition is NOT satisfied. Default None.
        """
        self._check_if_active()
        self._validate_initializer(if_body)

        # execute if branch
        start_node_name = self.add_node("nop", NopNode())
        if_body(self)
        end_node_name = self.add_node("nop", NopNode())

        # manipulate if branch
        if_condition_node = self._find_next_node(start_node_name)
        start_node = self._find_node(start_node_name)
        if_edge = start_node._find_outgoing_edge(if_condition_node.name)
        if_edge.MergeFrom(
            Edge(
                condition=condition.compile(),
            )
        )

        # execute else branch
        if else_body is not None:
            self._validate_initializer(else_body)

            # change positions
            self._nodes.remove(start_node)
            self._nodes.append(start_node)
            else_body(self)

            # find else edge
            else_condition_node = self._find_next_node(start_node_name)
            else_edge = start_node._find_outgoing_edge(else_condition_node.name)
            else_edge.MergeFrom(
                Edge(
                    condition=condition.negate().compile(),
                )
            )

            # add edge for last node
            last_else_node = self._last_node()
            last_else_node.outgoing_edges.append(Edge(sink_node_name=end_node_name))

            # change positions again
            end_node = self._find_node(end_node_name)
            self._nodes.remove(end_node)
            self._nodes.append(end_node)
        else:
            # add else
            start_node.outgoing_edges.append(
                Edge(
                    sink_node_name=end_node_name,
                    condition=condition.negate().compile(),
                )
            )


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
