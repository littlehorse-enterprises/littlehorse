from __future__ import annotations

import inspect
import logging
import typing
from collections import deque
from enum import Enum
from pathlib import Path
from typing import Any, Callable, List, Optional, Union

from google.protobuf.json_format import MessageToJson
from google.protobuf.message import Message

from littlehorse.config import LHConfig
from littlehorse.model import (
    LHErrorType,
    VariableType,
    Comparator,
    TaskNode,
    UTActionTrigger,
    VariableAssignment,
    VariableDef,
    VariableMutation,
    VariableMutationType,
    ExponentialBackoffRetryPolicy,
    ExternalEventDefId,
    TaskDefId,
    WorkflowEventDefId,
    PutExternalEventDefRequest,
    CorrelatedEventConfig,
    PutWorkflowEventDefRequest,
    PutWfSpecRequest,
    AllowedUpdateType,
    VariableValue,
    Edge,
    EdgeCondition,
    EntrypointNode,
    ExitNode,
    ExternalEventNode,
    FailureDef,
    InterruptDef,
    JsonIndex,
    Node,
    NopNode,
    ReturnType,
    SleepNode,
    StartThreadNode,
    StartMultipleThreadsNode,
    ThreadRetentionPolicy,
    ThreadSpec,
    ThreadVarDef,
    ThrowEventNode,
    TypeDefinition,
    UserTaskNode,
    WaitForThreadsNode,
    FailureHandlerDef,
    WfSpec,
    WfRunVariableAccessLevel,
    WorkflowRetentionPolicy,
)
from littlehorse.model.wf_spec_pb2 import PRIVATE_VAR
from littlehorse.utils import negate_comparator, to_variable_value
from littlehorse.worker import _create_task_def

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
    StartMultipleThreadsNode,
    ThrowEventNode,
]


def to_json(proto: Message) -> str:
    """Convert a proto object to json.

    Args:
        proto (Message): A proto object.

    Returns:
        str: JSON format.
    """
    return MessageToJson(proto, sort_keys=True)


def to_variable_assignment(value: Any) -> VariableAssignment:
    """Receives a value and return a Protobuf VariableAssignment.

    Args:
        value (Any): Any value.

    Returns:
        VariableAssignment: Protobuf.
    """
    if isinstance(value, NodeOutput):
        jsonpath: Optional[str] = None

        if value.json_path is not None:
            jsonpath = value.json_path

        return VariableAssignment(
            node_output=VariableAssignment.NodeOutputReference(
                node_name=value.node_name,
            ),
            json_path=jsonpath,
        )

    if isinstance(value, LHFormatString):
        return VariableAssignment(
            format_string=VariableAssignment.FormatString(
                format=to_variable_assignment(value._format),
                args=[to_variable_assignment(arg) for arg in value._args],
            )
        )

    if isinstance(value, WfRunVariable):
        json_path: Optional[str] = None
        variable_name = value.name

        if value.json_path is not None:
            json_path = value.json_path

        return VariableAssignment(
            json_path=json_path,
            variable_name=variable_name,
        )

    if isinstance(value, LHExpression):
        expression: LHExpression = value
        return VariableAssignment(
            expression=VariableAssignment.Expression(
                lhs=to_variable_assignment(expression.lhs()),
                operation=expression.operation(),
                rhs=to_variable_assignment(expression.rhs()),
            )
        )

    return VariableAssignment(
        literal_value=to_variable_value(value),
    )


class LHExpression:
    def __init__(self, lhs: Any, operation: VariableMutationType, rhs: Any) -> None:
        self._lhs = lhs
        self._operation = operation
        self._rhs = rhs

    def add(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.ADD, other)

    def subtract(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.SUBTRACT, other)

    def multiply(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.MULTIPLY, other)

    def divide(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.DIVIDE, other)

    def extend(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.EXTEND, other)

    def remove_if_present(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.REMOVE_IF_PRESENT, other)

    def remove_index(self, index: Optional[Union[int, Any]] = None) -> LHExpression:
        if index is None:
            raise ValueError("Expected 'index' to be set, but it was None.")
        return LHExpression(self, VariableMutationType.REMOVE_INDEX, index)

    def remove_key(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.REMOVE_KEY, other)

    def lhs(self) -> Any:
        return self._lhs

    def rhs(self) -> Any:
        return self._rhs

    def operation(self) -> Any:
        return self._operation


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
    START_MULTIPLE_THREADS = "START_MULTIPLE_THREADS"
    THROW_EVENT = "THROW_EVENT"

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
        if isinstance(node, StartMultipleThreadsNode):
            return cls.START_MULTIPLE_THREADS
        if isinstance(node, ThrowEventNode):
            return cls.THROW_EVENT

        raise TypeError("Unrecognized node type")


class LHFormatString:
    def __init__(self, format: str, *args: Any) -> None:
        """Generates a FormatString object that can be understood by the ThreadBuilder.

        Args:
            format (str): String format with variables with curly brackets {}.
            *args (Any): Arguments.

        Returns:
            FormatString: A FormatString.
        """
        self._format = format
        self._args = args


class NodeOutput(LHExpression):
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

    def add(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.ADD, other)

    def subtract(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.SUBTRACT, other)

    def multiply(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.MULTIPLY, other)

    def divide(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.DIVIDE, other)

    def extend(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.EXTEND, other)

    def remove_if_present(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.REMOVE_IF_PRESENT, other)

    def remove_index(self, index: Optional[Union[int, Any]] = None) -> LHExpression:
        if index is None:
            raise ValueError("Expected 'index' to be set, but it was None.")
        return LHExpression(self, VariableMutationType.REMOVE_INDEX, index)

    def remove_key(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.REMOVE_KEY, other)


class WorkflowIfStatement:
    def __init__(
        self,
        parent_workflow_thread: WorkflowThread,
        first_nop_node_name: str,
        last_nop_node_name: str,
    ) -> None:
        self._parent_workflow_thread = parent_workflow_thread
        self._first_nop_node_name = first_nop_node_name
        self._last_nop_node_name = last_nop_node_name
        self._was_else_executed = False

    def get_first_nop_node_name(self) -> str:
        return self._first_nop_node_name

    def get_last_nop_node_name(self) -> str:
        return self._last_nop_node_name

    def _is_not_initialized(self) -> bool:
        return self._first_nop_node_name == "" and self._last_nop_node_name == ""

    def do_else_if(
        self, condition: WorkflowCondition, body: "ThreadInitializer"
    ) -> WorkflowIfStatement:
        """After checking the previous condition(s) of the If Statement,
        conditionally executes some workflow code; equivalent to
        an elseif() statement in programming.

        Args:
            condition (WorkflowCondition): is the WorkflowCondition
            to be satisfied.
            body (ThreadInitializer): is the block of
            ThreadSpec code to be executed if the provided
            WorkflowCondition is satisfied.
        """
        if self._is_not_initialized():
            raise AttributeError(
                "'WorkflowIfStatement' object has no attribute 'do_else_if'"
            )
        self._parent_workflow_thread.organize_edges_for_else_if_execution(
            self, condition, body
        )

        return self

    def do_else(self, body: "ThreadInitializer") -> None:
        """After checking all previous condition(s) of the If Statement,
        executes some workflow code; equivalent to
        an else block in programming.

        Args:
            body (ThreadInitializer): the block of
            ThreadSpec code to be executed if all previous
            WorkflowConditions were not satisfied.
        """
        if self._is_not_initialized():
            raise AttributeError(
                "'WorkflowIfStatement' object has no attribute 'do_else'"
            )
        if self._was_else_executed:
            raise RuntimeError(
                "Else block has already been executed. Cannot add another else block."
            )

        self._was_else_executed = True
        self._parent_workflow_thread.organize_edges_for_else_if_execution(
            self, None, body
        )


class WfRunVariable:
    def __init__(
        self,
        variable_name: str,
        variable_type: VariableType,
        parent: WorkflowThread,
        default_value: Any = None,
        access_level: Optional[
            Union[WfRunVariableAccessLevel, str]
        ] = WfRunVariableAccessLevel.PRIVATE_VAR,
    ) -> None:
        """Defines a Variable in the ThreadSpec and returns a handle to it.

        Args:
            variable_name (str): The name of the variable.
            variable_type (VariableType): The variable type.
            parent (WorkflowThread): The parent WorkflowThread of this WfRunVariable.
            default_value (Any, optional): A default value. Defaults to None.
            access_level (WfRunVariableAccessLevel): Sets the access level of a WfRunVariable. Defaults to PRIVATE_VAR.

        Returns:
            WfRunVariable: A handle to the created WfRunVariable.

        Raises:
            TypeError: If variable_type and type(default_value) are not compatible.
        """
        if parent is None:
            raise ValueError("Parent workflow thread cannot be None.")

        self.name = variable_name
        self.type = variable_type
        self.parent = parent
        self.default_value: Optional[VariableValue] = None
        self._json_path: Optional[str] = None
        self._required = False
        self._masked = False
        self._searchable = False
        self._json_indexes: List[JsonIndex] = []
        self._access_level = access_level

        if default_value is not None:
            self._set_default(default_value)

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

        out = WfRunVariable(self.name, self.type, self.parent, self.default_value)
        out.json_path = json_path
        return out

    def as_public(self) -> "WfRunVariable":
        """Sets the access level to PUBLIC_VAR, which has three implications:
        - Future versions of this WfSpec cannot define a variable with the
          same name and a different type.
        - Child workflows can access this variable.
        - This variable is now considered in determining whether a new
          version of the WfSpec is a majorVersion or revision.
        """
        self._access_level = WfRunVariableAccessLevel.PUBLIC_VAR
        return self

    def as_inherited(self) -> "WfRunVariable":
        """Sets the access level to INHERITED_VAR, which has three implications:
        - Future versions of this WfSpec cannot define a variable with the
          same name and a different type.
        - Child workflows can access this variable.
        - This variable is now considered in determining whether a new
          version of the WfSpec is a majorVersion or revision.
        """
        self._access_level = WfRunVariableAccessLevel.INHERITED_VAR
        return self

    def with_access_level(
        self, access_level: WfRunVariableAccessLevel
    ) -> "WfRunVariable":
        """Sets the access level of a WfRunVariable."""
        self._access_level = access_level
        return self

    def searchable(self) -> "WfRunVariable":
        """Allows for searching for the WfRunVariable by its value.

        Returns:
            WfRunVariable: Same instance.
        """
        self._searchable = True
        return self

    def searchable_on(
        self,
        field_path: str,
        field_type: VariableType,
    ) -> "WfRunVariable":
        """Creates an index on a specified field of a Json Path variable,
        allowing you to search for this field using the SearchVariableRequest.

        Args:
            field_path (str): The JSON Path of the field to index,
                starting with '$.', eg: $.userId
            field_type (IndexType): the type of the field that is indexed.

        Returns:
            WfRunVariable: Same instance.
        """
        if field_path is None or field_type is None:
            raise ValueError("None is not allowed")

        if not field_path.startswith("$."):
            raise ValueError(f"Invalid JsonPath: {field_path}")

        if self.type != VariableType.JSON_OBJ:
            raise ValueError(
                f"JsonPath not allowed in a {VariableType.Name(self.type)} variable"
            )

        self._json_indexes.append(
            JsonIndex(field_path=field_path, field_type=field_type),
        )
        return self

    def required(self) -> "WfRunVariable":
        self._required = True
        return self

    def with_default(self, default_value: Any) -> WfRunVariable:
        self._set_default(default_value)

        return self

    def _set_default(self, default_value: Any) -> None:
        self.default_value = to_variable_value(default_value)
        if (
            self.default_value.WhichOneof("value")
            != str(VariableType.Name(self.type)).lower()
        ):
            raise TypeError(
                f"Default value type does not match LH variable type {VariableType.Name(self.type)}"
            )

    def masked(self) -> "WfRunVariable":
        self._masked = True
        return self

    def compile(self) -> ThreadVarDef:
        """Compile this into Protobuf Objects.

        Returns:
            VariableDef: Spec.
        """
        return ThreadVarDef(
            var_def=VariableDef(
                type_def=TypeDefinition(type=self.type, masked=self._masked),
                name=self.name,
                default_value=self.default_value,
            ),
            json_indexes=self._json_indexes.copy(),
            searchable=self._searchable,
            required=self._required,
            access_level=self._access_level,
        )

    def is_equal_to(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(self, Comparator.EQUALS, rhs)

    def is_not_equal_to(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(self, Comparator.NOT_EQUALS, rhs)

    def is_greater_than(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(self, Comparator.GREATER_THAN, rhs)

    def is_greater_than_eq(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(self, Comparator.GREATER_THAN_EQ, rhs)

    def is_less_than_eq(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(self, Comparator.LESS_THAN_EQ, rhs)

    def is_less_than(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(self, Comparator.LESS_THAN, rhs)

    def does_contain(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(rhs, Comparator.IN, self)

    def does_not_contain(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(rhs, Comparator.NOT_IN, self)

    def is_in(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(self, Comparator.IN, rhs)

    def is_not_in(self, rhs: Any) -> WorkflowCondition:
        return self.parent.condition(self, Comparator.NOT_IN, rhs)

    def assign(self, rhs: Any) -> None:
        active_thread = self.parent
        last_thread = self.parent.get_parent_workflow().get_threads()[-1]
        if last_thread.is_active:
            active_thread = last_thread

        active_thread.mutate(self, VariableMutationType.ASSIGN, rhs)

    def add(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.ADD, other)

    def subtract(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.SUBTRACT, other)

    def multiply(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.MULTIPLY, other)

    def divide(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.DIVIDE, other)

    def extend(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.EXTEND, other)

    def remove_if_present(self, other: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.REMOVE_IF_PRESENT, other)

    def remove_index(self, index: Optional[Union[int, Any]] = None) -> LHExpression:
        if index is None:
            raise ValueError("Expected 'index' to be set, but it was None.")
        return LHExpression(self, VariableMutationType.REMOVE_INDEX, index)

    def remove_key(self, key: Any) -> LHExpression:
        return LHExpression(self, VariableMutationType.REMOVE_KEY, key)

    def __str__(self) -> str:
        return to_json(self.compile())


class WaitForThreadsNodeOutput(NodeOutput):
    def __init__(self, node_name: str, builder: "WorkflowThread") -> None:
        super().__init__(node_name)
        self.node_name = node_name
        self.builder = builder

    def handle_exception_on_child(
        self, handler: ThreadInitializer, exception_name: Optional[str] = None
    ) -> WaitForThreadsNodeOutput:
        """
        Specifies a Failure Handler to run in case any of the ThreadRun's
        that we are waiting for in this WaitForThreadsNode fails with a
        specific EXCEPTION.

        Args:
            handler (ThreadInitializer): the handler logic.
            exception_name (Optional[str])): the specific EXCEPTION to handle.
        """
        self.builder._check_if_active()
        thread_name = f"exn-handler-{self.node_name}" + (
            f"-{exception_name}" if exception_name is not None else ""
        )
        thread_name = self.builder._workflow.add_sub_thread(thread_name, handler)
        node = self.builder._find_node(self.node_name)
        failure_handler: FailureHandlerDef
        if exception_name is not None:
            failure_handler = FailureHandlerDef(
                handler_spec_name=thread_name,
                specific_failure=exception_name,
            )
        else:
            failure_handler = FailureHandlerDef(
                handler_spec_name=thread_name,
                any_failure_of_type=FailureHandlerDef.LHFailureType.FAILURE_TYPE_EXCEPTION,
            )
        node.sub_node.__getattribute__("per_thread_failure_handlers").append(
            failure_handler,
        )
        return self

    def handle_error_on_child(
        self, handler: ThreadInitializer, error_type: Optional[LHErrorType] = None
    ) -> WaitForThreadsNodeOutput:
        """
        Specifies a Failure Handler to run in case any of the ThreadRun's
        that we are waiting for in this WaitForThreadsNode fails with a
        specific ERROR.

        Args:
            handler (ThreadInitializer): the handler logic.
            error_type (Optional[str])): the specific ERROR to handle.
        """
        self.builder._check_if_active()
        thread_name = f"error-handler-{self.node_name}" + (
            f"-{LHErrorType.Name(error_type)}" if error_type is not None else ""
        )
        thread_name = self.builder._workflow.add_sub_thread(thread_name, handler)
        node = self.builder._find_node(self.node_name)
        failure_handler: FailureHandlerDef
        if error_type is not None:
            failure_handler = FailureHandlerDef(
                handler_spec_name=thread_name,
                specific_failure=LHErrorType.Name(error_type),
            )
        else:
            failure_handler = FailureHandlerDef(
                handler_spec_name=thread_name,
                any_failure_of_type=FailureHandlerDef.LHFailureType.FAILURE_TYPE_ERROR,
            )
        node.sub_node.__getattribute__("per_thread_failure_handlers").append(
            failure_handler,
        )
        return self

    def handle_any_failure_on_child(
        self, handler: ThreadInitializer
    ) -> WaitForThreadsNodeOutput:
        self.builder._check_if_active()
        thread_name = f"failure-handler-{self.node_name}-ANY_FAILURE"
        thread_name = self.builder._workflow.add_sub_thread(thread_name, handler)
        node = self.builder._find_node(self.node_name)
        failure_handler = FailureHandlerDef(handler_spec_name=thread_name)
        node.sub_node.__getattribute__("per_thread_failure_handlers").append(
            failure_handler,
        )
        return self


class ThrowEventNodeOutput:
    """
    Represents the output of a ThrowEvent node in a workflow, allowing event definition registration.
    """

    def __init__(
        self,
        event_name: str,
        parent: WorkflowThread,
        payload_type: Optional[type] = None,
    ) -> None:
        """
        Initializes a new instance of the ThrowEventNodeOutput class.

        Args:
            event_name (str): The name of the workflow event definition.
            parent (WorkflowThread): The parent workflow thread.
            payload_type (Optional[type]): The type of the payload for the event. If None, no payload type is set.
        """
        self._event_name = event_name
        self._parent = parent
        self._payload_type: Optional[type] = payload_type

    def to_put_workflow_event_def_request(self) -> PutWorkflowEventDefRequest:
        """
        Returns a PutWorkflowEventDefRequest for registering this workflow event definition.

        Returns:
            PutWorkflowEventDefRequest: The request object for event definition registration.

        Raises:
            ValueError: If `_payload_type` is not set before generating the request.

        """
        output = PutWorkflowEventDefRequest(name=self._event_name)
        if self._payload_type is not None:
            output.content_type.CopyFrom(python_type_to_return_type(self._payload_type))

        return output


class ExternalEventNodeOutput(NodeOutput):
    def __init__(
        self,
        node_name: str,
        event_name: str,
        parent: WorkflowThread,
        payload_type: Optional[type] = None,
        correlated_event_config: Optional[CorrelatedEventConfig] = None,
    ) -> None:
        """
        Initializes a new instance of the ExternalEventNodeOutput class.

        Args:
            node_name (str): The specified node name.
            event_name (str): The external event definition name.
            parent (WorkflowThread): The workflow thread where the ExternalEventNodeOutput belongs to.
            payload_type (Optional[type]): The type of the payload for the external event. If None, no payload type is set.
            correlated_event_config (Optional[CorrelatedEventConfig]): Configuration for correlated event
        """
        super().__init__(node_name)
        self.event_name = event_name
        self.parent = parent
        self._payload_type: Optional[type] = payload_type
        self._correlated_event_config: Optional[CorrelatedEventConfig] = (
            correlated_event_config
        )

    def to_put_external_event_def_request(self) -> PutExternalEventDefRequest:
        """
        Returns a PutExternalEventDefRequest for registering this external event definition.

        Returns:
            PutExternalEventDefRequest: The request object for external event definition registration.

        Raises:
            ValueError: If `_payload_type` is not set before generating the request.
        """
        request = PutExternalEventDefRequest(name=self.event_name)

        if self._payload_type:
            request.content_type.CopyFrom(
                python_type_to_return_type(self._payload_type)
            )

        if self._correlated_event_config:
            request.correlated_event_config.CopyFrom(self._correlated_event_config)

        return request


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
        self.failure_handlers: list[FailureHandlerDef] = []

    def __str__(self) -> str:
        return to_json(self.compile())

    def _find_outgoing_edge(self, sink_node_name: str) -> Edge:
        for edge in self.outgoing_edges:
            if sink_node_name == edge.sink_node_name:
                return edge

        raise ValueError("Edge not found")

    def _has_outgoing_edge(self) -> bool:
        return len(self.outgoing_edges) > 0

    def compile(self) -> Node:
        """Compile this into Protobuf Objects.

        Returns:
            Node: Spec.
        """

        def new_node(**kwargs: Any) -> Node:
            return Node(
                outgoing_edges=self.outgoing_edges,
                failure_handlers=self.failure_handlers,
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
        if self.node_case == NodeCase.START_MULTIPLE_THREADS:
            return new_node(start_multiple_threads=self.sub_node)
        if self.node_case == NodeCase.THROW_EVENT:
            return new_node(throw_event=self.sub_node)

        raise ValueError("Node type not supported")


class WorkflowInterruption:
    def __init__(self, name: str, thread_name: str) -> None:
        self.name = name
        self.thread_name = thread_name

    def compile(self) -> InterruptDef:
        """Compile this into Protobuf Objects.

        Returns:
            InterruptDef: Spec.
        """
        return InterruptDef(
            external_event_def_id=ExternalEventDefId(name=self.name),
            handler_spec_name=self.thread_name,
        )

    def __str__(self) -> str:
        return to_json(self.compile())


class SpawnedThread:
    def __init__(self, name: str, number: WfRunVariable) -> None:
        self.name = name
        self.number = number


class SpawnedThreads:
    def __init__(
        self,
        iterable: Optional[WfRunVariable] = None,
        fixed_threads: Optional[list[SpawnedThread]] = None,
    ) -> None:
        self._iterable = iterable
        self._fixed_threads = fixed_threads

    @classmethod
    def from_list(cls, *spawned_threads: SpawnedThread) -> "SpawnedThreads":
        return SpawnedThreads(iterable=None, fixed_threads=list(spawned_threads))

    def compile(self) -> WaitForThreadsNode:
        def build_fixed_threads(
            fixed_threads: Optional[list[SpawnedThread]],
        ) -> WaitForThreadsNode:
            threads: list[WaitForThreadsNode.ThreadToWaitFor] = []
            if fixed_threads is not None:
                for spawned_thread in fixed_threads:
                    thread_to_wait_for = WaitForThreadsNode.ThreadToWaitFor(
                        thread_run_number=to_variable_assignment(spawned_thread.number)
                    )
                    threads.append(thread_to_wait_for)
            return WaitForThreadsNode(
                threads=WaitForThreadsNode.ThreadsToWaitFor(threads=threads),
            )

        def build_iterator_threads(
            iterable: Optional[WfRunVariable],
        ) -> WaitForThreadsNode:
            return WaitForThreadsNode(
                thread_list=to_variable_assignment(iterable),
            )

        return (
            build_iterator_threads(self._iterable)
            if self._fixed_threads is None
            else build_fixed_threads(self._fixed_threads)
        )


class UserTaskOutput(NodeOutput):
    def __init__(
        self,
        node_name: str,
        thread: "WorkflowThread",
        user_task_def_name: str,
        user_id: Optional[Union[str, WfRunVariable]] = None,
        user_group: Optional[Union[str, WfRunVariable]] = None,
        notes: Optional[Union[str, WfRunVariable, LHFormatString]] = None,
        on_cancellation_exception_name: Optional[Union[str, WfRunVariable]] = None,
    ) -> None:
        super().__init__(node_name)
        self._thread = thread
        self._node_name = node_name
        self._user_task_def_name = user_task_def_name
        self._user_group = user_group
        self._user_id = user_id
        self._notes = notes
        self._on_cancellation_exception_name = on_cancellation_exception_name

    def with_notes(
        self, notes: Union[str, WfRunVariable, LHFormatString]
    ) -> "UserTaskOutput":
        node = self._thread._last_node()
        if node.name != self._node_name:
            raise ValueError("tried to mutate stale UserTaskOutput!")

        ug = to_variable_assignment(self._user_group) if self._user_group else None
        ui = to_variable_assignment(self._user_id) if self._user_id else None
        if self._on_cancellation_exception_name:
            exception_name = to_variable_assignment(
                self._on_cancellation_exception_name
            )
        else:
            exception_name = None

        ut_node = UserTaskNode(
            user_task_def_name=self._user_task_def_name,
            user_group=ug,
            user_id=ui,
            notes=to_variable_assignment(notes),
            on_cancellation_exception_name=exception_name,
        )
        self._notes = notes

        node.sub_node = ut_node
        return self

    def with_on_cancellation_exception(
        self, exception_name: Union[str, WfRunVariable]
    ) -> "UserTaskOutput":
        node = self._thread._last_node()
        if node.name != self._node_name:
            raise ValueError("tried to mutate stale UserTaskOutput!")

        ug = to_variable_assignment(self._user_group) if self._user_group else None
        ui = to_variable_assignment(self._user_id) if self._user_id else None
        if self._notes:
            notes = to_variable_assignment(self._notes)
        else:
            notes = None
        ut_node = UserTaskNode(
            user_task_def_name=self._user_task_def_name,
            user_group=ug,
            user_id=ui,
            notes=notes,
            on_cancellation_exception_name=to_variable_assignment(exception_name),
        )

        node.sub_node = ut_node
        return self


class WorkflowThread:
    def __init__(
        self,
        workflow: "Workflow",
        initializer: "ThreadInitializer",
        default_retries: Optional[int] = None,
        default_exponential_backoff: Optional[ExponentialBackoffRetryPolicy] = None,
        default_timeout_seconds: Optional[int] = None,
    ) -> None:
        """This is used to define the logic of a ThreadSpec in a ThreadInitializer.

        Args:
            workflow (Workflow): Parent.
            initializer (ThreadInitializer): Initializer.
        """
        self._default_exponential_backoff: Optional[ExponentialBackoffRetryPolicy] = (
            default_exponential_backoff
        )
        self._default_timeout_seconds: Optional[int] = default_timeout_seconds
        self._default_retries: Optional[int] = default_retries
        self._wf_run_variables: list[WfRunVariable] = []
        self._wf_interruptions: list[WorkflowInterruption] = []
        self._nodes: list[WorkflowNode] = []
        self._variable_mutations: deque[VariableMutation] = deque()
        self._last_node_condition: EdgeCondition | None = None
        self._retention_policy: Optional[ThreadRetentionPolicy] = None

        if workflow is None:
            raise ValueError("Workflow cannot be None.")

        self._workflow = workflow
        self._workflow._builders.append(self)

        self._validate_initializer(initializer)

        self.is_active = True
        self._last_node_name: str = self.add_node("entrypoint", EntrypointNode())

        initializer(self)
        node = self._last_node()
        if node.node_case != NodeCase.EXIT:
            self.add_node("exit", ExitNode())
        self.is_active = False

    def get_parent_workflow(self) -> "Workflow":
        """Returns the parent workflow of this ThreadSpec."""
        return self._workflow

    def spawn_thread(
        self,
        initializer: "ThreadInitializer",
        thread_name: str,
        input: Optional[dict[str, Any]] = None,
    ) -> SpawnedThread:
        """Adds a SPAWN_THREAD node to the ThreadSpec,
        which spawns a Child ThreadRun whose ThreadSpec
        is determined by the provided ThreadFunc.

        Args:
            initializer (ThreadInitializer): defines the logic for the
            child ThreadRun to execute.
            thread_name (str): is the name of the child thread spec.
            input (dict[str, Any], optional): is a dict of all of the
            input variables to set for the child ThreadRun. If
            you don't need to set any input variables, leave this
            null. Defaults to None.

        Returns:
            SpawnedThread: a handle to the resulting SpawnedThread,
            which can be used in wait_for_threads()
        """
        self._check_if_active()
        input = {} if input is None else input
        thread_name = self._workflow.add_sub_thread(thread_name, initializer)

        start_thread_node = StartThreadNode(
            thread_spec_name=thread_name,
            variables={
                key: to_variable_assignment(value) for key, value in input.items()
            },
        )

        node_name = self.add_node(thread_name, start_thread_node)
        thread_number = self.add_variable(node_name, VariableType.INT)
        self.mutate(thread_number, VariableMutationType.ASSIGN, NodeOutput(node_name))

        return SpawnedThread(thread_name, thread_number)

    def spawn_thread_for_each(
        self,
        arr_var: WfRunVariable,
        initializer: "ThreadInitializer",
        thread_name: str,
        input: Optional[dict[str, Any]] = None,
    ) -> SpawnedThreads:
        """Iterates over over each object inside a JSON_ARR variable
        and creates a Child ThreadRun for each item. Resulting object
        will be provided as a input variable to the child ThreadRun
        with name 'INPUT'

        Args:
            arr_var (WfRunVariable): WfRunVariable of type JSON_ARR
            that we iterate over.
            initializer (ThreadInitializer): Function that defnes the ThreadSpec.
            thread_name (str): Name to assign to the created ThreadSpec.
            input (Optional[dict[str, Any]], optional): Input variables to pass
            to each child
            ThreadRun in addition to the list item.. Defaults to None.

        Returns:
            SpawnedThreads: SpawnedThreads handle which we can use
            to wait for all child threads.
        """
        self._check_if_active()
        thread_name = self._workflow.add_sub_thread(thread_name, initializer)
        input = {} if input is None else input
        start_multiple_threads_node = StartMultipleThreadsNode(
            thread_spec_name=thread_name,
            variables={
                key: to_variable_assignment(value) for key, value in input.items()
            },
            iterable=to_variable_assignment(arr_var),
        )
        node_name = self.add_node(thread_name, start_multiple_threads_node)
        thread_number = self.add_variable(node_name, VariableType.JSON_ARR)
        self.mutate(thread_number, VariableMutationType.ASSIGN, NodeOutput(node_name))
        return SpawnedThreads(thread_number, None)

    def wait_for_threads(self, wait_for: SpawnedThreads) -> WaitForThreadsNodeOutput:
        """Adds a WAIT_FOR_THREAD node which waits for a Child ThreadRun to complete.

        Args:
            wait_for (SpawnedThreads): set of SpawnedThread objects returned
            one or more calls to spawnThread.

        Returns:
            NodeOutput: a NodeOutput that can be used for timeouts
            or exception handling.
        """
        self._check_if_active()
        node = wait_for.compile()
        node_name = self.add_node("threads", node)
        return WaitForThreadsNodeOutput(node_name, self)

    def sleep(self, seconds: Union[int, WfRunVariable]) -> None:
        """Adds a SLEEP node which makes the ThreadRun sleep
        for a specified number of seconds.

        Args:
            seconds (int): is either an integer representing the
            number of seconds to sleep for, or it is
            a WfRunVariable which evaluates to a
            VariableTypePb.INT specifying the number of seconds
            to sleep for.
        """
        self._check_if_active()
        if isinstance(seconds, WfRunVariable) and seconds.type is not VariableType.INT:
            raise ValueError("WfRunVariable must be VariableType.INT")

        if isinstance(seconds, int) and seconds <= 0:
            raise ValueError(f"Value '{seconds}' not allowed")
        self.add_node("sleep", SleepNode(raw_seconds=to_variable_assignment(seconds)))

    def sleep_until(self, timestamp: WfRunVariable) -> None:
        """Adds a SLEEP node which makes the ThreadRun sleep until
        a specified timestamp, provided as an
        INT WfRunVariable (note that INT in LH is a 64-bit integer).

        Args:
            timestamp (WfRunVariable): a WfRunVariable which evaluates
            to a VariableTypePb.INT specifying the epoch
            timestamp (in milliseconds) to wait for.
        """
        self._check_if_active()
        if (
            isinstance(timestamp, WfRunVariable)
            and timestamp.type is not VariableType.INT
        ):
            raise ValueError("WfRunVariable must be VariableType.INT")
        self.add_node("sleep", SleepNode(timestamp=to_variable_assignment(timestamp)))

    def with_retention_policy(self, policy: ThreadRetentionPolicy) -> None:
        """Sets the Retention Policy for the ThreadSpec created by this WorkflowThread.

        Args:
            policy (ThreadRetentionPolicy): the retention policy to set.
        """
        self._retention_policy = policy

    def add_interrupt_handler(self, name: str, handler: "ThreadInitializer") -> None:
        """Registers an Interrupt Handler, such that when an ExternalEvent
        arrives with the specified type, this ThreadRun is interrupted.

        Args:
            name (str): The name of the ExternalEventDef to listen for.
            handler (ThreadInitializer): A Thread Function defining a
            ThreadSpec to use to handle the Interrupt.
        """
        self._check_if_active()
        thread_name = self._workflow.add_sub_thread(f"interrupt-{name}", handler)
        self._wf_interruptions.append(WorkflowInterruption(name, thread_name))

    def register_external_event_def(self, node_output: ExternalEventNodeOutput) -> None:
        """
        Registers an external event definition for the parent workflow.

        Args:
            node_output (ExternalEventNodeOutput): The external event node output.
        """
        self._workflow.add_external_event_def_to_register(node_output)

    def register_workflow_event_def(self, node_output: ThrowEventNodeOutput) -> None:
        """
        Registers a workflow event definition for the parent workflow.

        Args:
            node_output (ThrowEventNodeOutput): The workflow event node output.
        """
        self._workflow.add_workflow_event_def_to_register(node_output)

    def _validate_initializer(self, initializer: "ThreadInitializer") -> None:
        if initializer is None:
            raise ValueError("ThreadInitializer cannot be None")

        if not inspect.isfunction(initializer) and not inspect.ismethod(initializer):
            raise TypeError("Object is not a ThreadInitializer")

    def compile(self) -> ThreadSpec:
        """Compile this into Protobuf Objects.

        Returns:
            ThreadSpec: Spec.
        """
        variable_defs = [variable.compile() for variable in self._wf_run_variables]
        nodes = {node.name: node.compile() for node in self._nodes}
        interruptions = [
            interruption.compile() for interruption in self._wf_interruptions
        ]
        return ThreadSpec(
            variable_defs=variable_defs,
            nodes=nodes,
            interrupt_defs=interruptions,
            retention_policy=self._retention_policy,
        )

    def __str__(self) -> str:
        return to_json(self.compile())

    def _check_if_active(self) -> None:
        if not self.is_active:
            raise ReferenceError("Using an inactive thread, check your workflow")

    def _last_node(self) -> WorkflowNode:
        return self._find_node(self._last_node_name)

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

    def _schedule_reminder_task_helper(
        self,
        user_task: UserTaskOutput,
        delay_in_seconds: VariableAssignment,
        task_def_name: str,
        hook: UTActionTrigger.UTHook,
        *args: Any,
    ) -> None:
        task_node = TaskNode(
            task_def_id=TaskDefId(name=task_def_name),
            variables=[to_variable_assignment(arg) for arg in args],
        )
        trigger: UTActionTrigger = UTActionTrigger(
            task=UTActionTrigger.UTATask(task=task_node),
            delay_seconds=delay_in_seconds,
            hook=hook,
        )
        cur_node = self._last_node()

        if cur_node.name != user_task.node_name:
            raise ValueError("Tried to reassign stale User Task!")

        ut_node: UserTaskNode = typing.cast(UserTaskNode, cur_node.sub_node)
        ut_node.actions.append(trigger)

    def execute(
        self,
        task_name: Union[str, LHFormatString, WfRunVariable],
        *args: Any,
        timeout_seconds: Optional[int] = None,
        retries: Optional[int] = None,
        exponential_backoff: Optional[ExponentialBackoffRetryPolicy] = None,
    ) -> NodeOutput:
        """Adds a TASK node to the ThreadSpec.

        Args:
            task_name (str): The name of the TaskDef to execute.
            *args (Any):  The input parameters to pass into the Task Run.
            If the type of an arg is a WfRunVariable, then that
            WfRunVariable is passed in as the argument; otherwise, the
            library will attempt to cast the provided argument to a
            LittleHorse VariableValue and pass that literal value in.
            retries(int): Retries in case an error
            exponential_backoff (ExponentialBackoffRetryPolicy): Retries policy in case of error.

        Returns:
            NodeOutput: A NodeOutput for that TASK node.
        """
        self._check_if_active()
        task_node: TaskNode
        readable_name: str

        if isinstance(task_name, str):
            readable_name = task_name
            task_node = TaskNode(
                task_def_id=TaskDefId(name=task_name),
                variables=[to_variable_assignment(arg) for arg in args],
                timeout_seconds=(
                    timeout_seconds
                    if timeout_seconds is not None
                    else self._default_timeout_seconds
                ),
                retries=retries if retries is not None else self._default_retries,
                exponential_backoff=(
                    exponential_backoff
                    if exponential_backoff is not None
                    else self._default_exponential_backoff
                ),
            )
        elif isinstance(task_name, LHFormatString):
            readable_name = task_name._format
            task_node = TaskNode(
                dynamic_task=to_variable_assignment(task_name),
                variables=[to_variable_assignment(arg) for arg in args],
                timeout_seconds=(
                    timeout_seconds
                    if timeout_seconds is not None
                    else self._default_timeout_seconds
                ),
                retries=retries if retries is not None else self._default_retries,
                exponential_backoff=(
                    exponential_backoff
                    if exponential_backoff is not None
                    else self._default_exponential_backoff
                ),
            )
        else:
            # WfRunVariable
            readable_name = task_name.name
            task_node = TaskNode(
                dynamic_task=to_variable_assignment(task_name),
                variables=[to_variable_assignment(arg) for arg in args],
                timeout_seconds=(
                    timeout_seconds
                    if timeout_seconds is not None
                    else self._default_timeout_seconds
                ),
                retries=retries if retries is not None else self._default_retries,
                exponential_backoff=(
                    exponential_backoff
                    if exponential_backoff is not None
                    else self._default_exponential_backoff
                ),
            )

        node_name = self.add_node(readable_name, task_node)
        return NodeOutput(node_name)

    def multiply(self, lhs: Any, rhs: Any) -> LHExpression:
        return LHExpression(lhs, VariableMutationType.MULTIPLY, rhs)

    def add(self, lhs: Any, rhs: Any) -> LHExpression:
        return LHExpression(lhs, VariableMutationType.ADD, rhs)

    def divide(self, lhs: Any, rhs: Any) -> LHExpression:
        return LHExpression(lhs, VariableMutationType.DIVIDE, rhs)

    def subtract(self, lhs: Any, rhs: Any) -> LHExpression:
        return LHExpression(lhs, VariableMutationType.SUBTRACT, rhs)

    def extend(self, lhs: Any, rhs: Any) -> LHExpression:
        return LHExpression(lhs, VariableMutationType.EXTEND, rhs)

    def remove_if_present(self, lhs: Any, rhs: Any) -> LHExpression:
        return LHExpression(lhs, VariableMutationType.REMOVE_IF_PRESENT, rhs)

    def remove_index(self, index: Optional[Union[int, Any]] = None) -> LHExpression:
        if index is None:
            raise ValueError("Expected 'index' to be set, but it was None.")
        return LHExpression(self, VariableMutationType.REMOVE_INDEX, index)

    def remove_key(self, lhs: Any, rhs: Any) -> LHExpression:
        return LHExpression(lhs, VariableMutationType.REMOVE_KEY, rhs)

    def declare_bool(self, name: str, default_value: Any = None) -> WfRunVariable:
        return self.add_variable(name, VariableType.BOOL, default_value=default_value)

    def declare_int(self, name: str, default_value: Any = None) -> WfRunVariable:
        return self.add_variable(name, VariableType.INT, default_value=default_value)

    def declare_str(self, name: str, default_value: Any = None) -> WfRunVariable:
        return self.add_variable(name, VariableType.STR, default_value=default_value)

    def declare_double(self, name: str, default_value: Any = None) -> WfRunVariable:
        return self.add_variable(name, VariableType.DOUBLE, default_value=default_value)

    def declare_bytes(self, name: str, default_value: Any = None) -> WfRunVariable:
        return self.add_variable(name, VariableType.BYTES, default_value=default_value)

    def declare_json_arr(self, name: str, default_value: Any = None) -> WfRunVariable:
        return self.add_variable(
            name, VariableType.JSON_ARR, default_value=default_value
        )

    def declare_json_obj(self, name: str, default_value: Any = None) -> WfRunVariable:
        return self.add_variable(
            name, VariableType.JSON_OBJ, default_value=default_value
        )

    def handle_any_failure(
        self, node: NodeOutput, initializer: "ThreadInitializer"
    ) -> None:
        """Attaches an Failure Handler to the specified NodeOutput,
        allowing it to manage any types of errors or exceptions.

        Args:
            node (NodeOutput): The NodeOutput instance to which
            the Failure Handler will be attached.
            initializer (ThreadInitializer): It specifies how to
            handle failures.
        """
        self._check_if_active()
        thread_name = f"exn-handler-{node.node_name}-any-failure"
        self._workflow.add_sub_thread(thread_name, initializer)
        failure_handler = FailureHandlerDef(handler_spec_name=thread_name)
        last_node = self._find_node(node.node_name)
        last_node.failure_handlers.append(failure_handler)

    def handle_exception(
        self,
        node: NodeOutput,
        initializer: "ThreadInitializer",
        exception_name: Optional[str] = None,
    ) -> None:
        """Attaches an Exception Handler to the specified
        NodeOutput, enabling it to handle specific
        types of exceptions as defined by the 'exception_name'
        parameter. If 'exception_name' is None,
        the handler will catch all exceptions.

        Args:
            node (NodeOutput): The NodeOutput instance to which
            the Exception Handler will be attached.
            initializer (ThreadInitializer): It specifies how to
            handle the exception.
            exception_name (Optional[str], optional): The name of the
            specific exception to handle. If set to null, the handler
            will catch all exceptions. Defaults to None.
        """
        self._check_if_active()
        thread_name = f"exn-handler-{node.node_name}" + (
            f"-{exception_name}" if exception_name is not None else ""
        )
        self._workflow.add_sub_thread(thread_name, initializer)

        failure_handler = FailureHandlerDef(
            handler_spec_name=thread_name,
            any_failure_of_type=(
                FailureHandlerDef.LHFailureType.FAILURE_TYPE_EXCEPTION
                if exception_name is None
                else None
            ),
            specific_failure=exception_name,
        )
        last_node = self._find_node(node.node_name)
        last_node.failure_handlers.append(failure_handler)

    def handle_error(
        self,
        node: NodeOutput,
        initializer: "ThreadInitializer",
        error_type: Optional[LHErrorType] = None,
    ) -> None:
        """Adds Error Handler to the specified NodeOutput,
        allowing it to manage specific types of errors. If
        error_type is None, the handler will catch all errors.

        Args:
            node (NodeOutput): The NodeOutput instance to which the
            Error Handler will be attached.
            initializer (ThreadInitializer): specifies how to handle the error.
            error_type (Optional[LHErrorType]): The type of error
            that the handler will manage.
        """
        self._check_if_active()
        any_error = FailureHandlerDef.LHFailureType.Name(
            FailureHandlerDef.FAILURE_TYPE_ERROR
        )
        failure_name = (
            LHErrorType.Name(error_type) if error_type is not None else any_error
        )
        thread_name = f"exn-handler-{node.node_name}-{failure_name}"
        self._workflow.add_sub_thread(thread_name, initializer)
        failure_handler = FailureHandlerDef(
            handler_spec_name=thread_name,
            any_failure_of_type=failure_name if not error_type else None,
            specific_failure=failure_name if error_type else None,
        )
        last_node = self._find_node(node.node_name)
        last_node.failure_handlers.append(failure_handler)

    def fail(
        self, failure_name: str, message: str, output: Optional[Any] = None
    ) -> None:
        """Adds an EXIT node with a Failure defined. This causes a
        ThreadRun to fail, and the resulting
        Failure has the specified value, name,
        and a human-readable message.

        Args:
            failure_name (str): The name of the failure to throw.
            message (str): A human-readable message.
            output (Optional[Any]):A literal value or a WfRunVariable.
            The assigned value is the payload of the resulting Failure,
            which can be accessed by any Failure Handler ThreadRuns.
        """
        self._check_if_active()

        if failure_name is None:
            raise ValueError("Failure name cannot be None")

        self.add_node(
            failure_name,
            ExitNode(
                failure_def=FailureDef(
                    failure_name=failure_name,
                    content=(
                        to_variable_assignment(output) if output is not None else None
                    ),
                    message=message,
                )
            ),
        )

    def complete(self) -> None:
        """Adds an Exit Node, returning from the WorkflowThread early"""
        self._check_if_active()
        self.add_node("complete", ExitNode())

    def assign_user_task(
        self,
        user_task_def_name: str,
        user_id: Optional[Union[str, WfRunVariable]] = None,
        user_group: Optional[Union[str, WfRunVariable]] = None,
    ) -> UserTaskOutput:
        """Adds a User Task Node, and assigns it to a specific user

        Args:
            user_task_def_name (str): is the UserTaskDef to assign.
            user_id (Optional[Union[str, WfRunVariable]], optional): is the
            user id to assign it to. Can be either String or WfRunVariable.
            Can be None if userGroup not None. Defaults to None.
            user_group (Optional[Union[str, WfRunVariable]], optional):
            user group to assign it to. Can be either String or WfRunvariable.
            Can be null if userId not null.
            Defaults to None.

        Raises:
            ValueError: _description_

        Returns:
            UserTaskOutput: _description_
        """
        self._check_if_active()
        if user_group is None and user_id is None:
            raise ValueError(
                "Must provide either user_id or user_group to assign_user_task()"
            )

        if user_id is not None and isinstance(user_id, str) and user_id.strip() == "":
            raise ValueError("UserId can't be empty to assign_user_task()")

        if (
            user_group is not None
            and isinstance(user_group, str)
            and user_group.strip() == ""
        ):
            raise ValueError("UserGroup can't be empty to assign_user_task()")

        ug = to_variable_assignment(user_group) if user_group else None
        ui = to_variable_assignment(user_id) if user_id else None
        ut_node = UserTaskNode(
            user_task_def_name=user_task_def_name,
            user_group=ug,
            user_id=ui,
        )

        return UserTaskOutput(
            self.add_node(user_task_def_name, ut_node),
            self,
            user_task_def_name,
            user_id=user_id,
            user_group=user_group,
        )

    def reassign_user_task_on_deadline(
        self,
        user_task: UserTaskOutput,
        deadline_seconds: Union[int, WfRunVariable],
        user_group: Optional[Union[str, WfRunVariable]] = None,
        user_id: Optional[Union[str, WfRunVariable]] = None,
    ) -> None:
        self._check_if_active()
        """Schedules the reassignment of a User Task to a
        specified userId and/or userGroup after a specified expiration.

        Args:
            user_task (UserTaskOutput): is the userTask to reschedule.
            deadline_seconds (Union[int, WfRunVariable]): is the expiration
            time after which the UserTask should be reassigned.
            Can be either WfRunVariable or int.
        """
        if self._last_node().name != user_task.node_name:
            raise ValueError("Tried to reassign stale user task node!")

        reassign = UTActionTrigger.UTAReassign(
            user_id=to_variable_assignment(user_id) if user_id is not None else None,
            user_group=(
                to_variable_assignment(user_group) if user_group is not None else None
            ),
        )

        ut_node: UserTaskNode = typing.cast(UserTaskNode, self._last_node().sub_node)
        ut_node.actions.append(
            UTActionTrigger(
                reassign=reassign,
                delay_seconds=to_variable_assignment(deadline_seconds),
            )
        )

    def release_to_group_on_deadline(
        self,
        user_task: UserTaskOutput,
        deadline_seconds: int,
    ) -> None:
        """Schedule Reassignment of a UserTask to a userGroup
        upon reaching the Deadline. This method is used to schedule
        the reassignment of a UserTask to a userGroup
        when the specified UserTask user assignment
        reaches its deadline in seconds.

        Args:
            user_task (UserTaskOutput): UserTask that is currently
            assigned to a UserGroup.
            deadline_seconds (int): Time in seconds after which
            the UserTask will be automatically reassigned to the UserGroup.
            Can be either String or WfRunVariable.
        """
        self._check_if_active()
        cur_node = self._last_node()

        if cur_node.name != user_task.node_name:
            raise ValueError("Tried to reassign stale User Task!")

        ut_node: UserTaskNode = typing.cast(UserTaskNode, cur_node.sub_node)
        if ut_node.user_group is None:
            raise ValueError("Cannot release node to group if user_group is None")
        if ut_node.user_id is None:
            raise ValueError("Cannot release node to group if user_id is none")

        trigger: UTActionTrigger = UTActionTrigger(
            reassign=UTActionTrigger.UTAReassign(user_group=ut_node.user_group),
            delay_seconds=to_variable_assignment(deadline_seconds),
            hook=UTActionTrigger.UTHook.ON_TASK_ASSIGNED,
        )
        ut_node.actions.append(trigger)

    def schedule_reminder_task(
        self,
        user_task: UserTaskOutput,
        delay_in_seconds: Union[int, WfRunVariable],
        task_def_name: str,
        *args: Any,
    ) -> None:
        delay_in_seconds_var = to_variable_assignment(delay_in_seconds)
        self._schedule_reminder_task_helper(
            user_task,
            delay_in_seconds_var,
            task_def_name,
            UTActionTrigger.ON_ARRIVAL,
            *args,
        )

    def schedule_reminder_task_on_assignment(
        self,
        user_task: UserTaskOutput,
        delay_in_seconds: Union[int, WfRunVariable],
        task_def_name: str,
        *args: Any,
    ) -> None:
        delay_in_seconds_var = to_variable_assignment(delay_in_seconds)
        self._schedule_reminder_task_helper(
            user_task,
            delay_in_seconds_var,
            task_def_name,
            UTActionTrigger.ON_TASK_ASSIGNED,
            args,
        )

    def wait_for_event(
        self,
        event_name: str,
        timeout: int = -1,
        correlation_id: Optional[Union[str, LHFormatString, WfRunVariable]] = None,
        mask_correlation_id: Optional[bool] = None,
        auto_register: Optional[bool] = False,
        return_type: Optional[type] = None,
        correlated_event_config: Optional[CorrelatedEventConfig] = None,
    ) -> ExternalEventNodeOutput:
        """Adds an EXTERNAL_EVENT node which blocks until an
        'ExternalEvent' of the specified type arrives.

        Args:
            event_name (str): The name of ExternalEvent to wait for
            timeout (int, optional): Timeout in seconds.
                If it is 0 or less it does not set a timeout. Defaults to -1.
            correlation_id (Union[str, LHFormatString, WfRunVariable]): the
                correlation id to be used for CorrelatedEvents.
            mask_correlation_id (Optional[bool]): Whether to mask the correlation ID.
            auto_register (Optional[bool]): If set, the External event will get registered
                together with the workflow.
            return_type (Optional[type]): The type of the payload to return by the external event.
            correlated_event_config (Optional[CorrelatedEventConfig]): Configuration for correlated
                events.

        Note:
            If any of auto_register, return_type, or correlated_event_config are set,
            the ExternalEventDef will be automatically registered with the workflow.

        Returns:
            ExternalEventNodeOutput: An ExternalEventNodeOutput for this event.
        """
        self._check_if_active()

        correlation_var_assn: Optional[VariableAssignment] = None
        if correlation_id is not None:
            correlation_var_assn = to_variable_assignment(correlation_id)
            if (
                isinstance(correlation_id, WfRunVariable)
                and mask_correlation_id is None
            ):
                mask_correlation_id = correlation_id._masked
        if mask_correlation_id is None:
            mask_correlation_id = False

        wait_node = ExternalEventNode(
            external_event_def_id=ExternalEventDefId(name=event_name),
            timeout_seconds=None if timeout <= 0 else to_variable_assignment(timeout),
            correlation_key=correlation_var_assn,
            mask_correlation_key=mask_correlation_id,
        )
        node_name = self.add_node(event_name, wait_node)

        output = ExternalEventNodeOutput(
            node_name=node_name,
            event_name=event_name,
            parent=self,
            payload_type=return_type,
            correlated_event_config=correlated_event_config,
        )
        if auto_register or return_type or correlated_event_config:
            self.register_external_event_def(output)

        return output

    def throw_event(
        self,
        workflow_event_name: str,
        content: Any,
        auto_register: Optional[bool] = False,
        return_type: Optional[type] = None,
    ) -> ThrowEventNodeOutput:
        """Adds a THROW_EVENT node which throws a WorkflowEvent.

        Args:
            workflow_event_name (str): The WorkflowEventDefId name of
                the WorkflowEvent to throw
            content (Any): the content of the WorkflowEvent to throw
            auto_register (Optional[bool]): If set, the WorkflowEventDef will be registered
                together with the workflow.
            return_type (Optional[type]): The type of the payload to return
                by the WorkflowEvent.

        Note:
            If auto_register or return_type are set, the WorkflowEventDef will be automatically
            registered with the workflow.

        Returns:
            NodeOutput: A NodeOutput for this event.
        """
        self._check_if_active()
        throw_node = ThrowEventNode(
            event_def_id=WorkflowEventDefId(name=workflow_event_name),
            content=to_variable_assignment(content),
        )
        self.add_node("throw-" + workflow_event_name, throw_node)

        output = ThrowEventNodeOutput(
            event_name=workflow_event_name, parent=self, payload_type=return_type
        )
        if auto_register or return_type:
            self.register_workflow_event_def(output)

        return output

    def mutate(
        self, left_hand: WfRunVariable, operation: VariableMutationType, right_hand: Any
    ) -> None:
        """Adds a VariableMutation to the last Node.

        Args:
            left_hand (WfRunVariable): It is a handle to the WfRunVariable to mutate.
            operation (VariableMutationType): It is the mutation type to use,
            for example, `VariableMutationType.ASSIGN`.
            right_hand (Any): It is either a literal value
            (which the Library casts to a Variable Value), a
            `WfRunVariable` which determines the right hand side
            of the expression, or a `NodeOutput` (which allows you to
            use the output of a Node Run to mutate variables).
        """
        self._check_if_active()

        if self._last_node().node_case == NodeCase.EXIT:
            raise TypeError(
                "You cannot mutate a variable in a given thread after the thread has completed."
            )

        node_output: Optional[VariableMutation.NodeOutputSource] = None
        literal_value: Optional[VariableValue] = None
        rhs_assignment = to_variable_assignment(right_hand)

        mutation = VariableMutation(
            lhs_name=left_hand.name,
            lhs_json_path=left_hand.json_path,
            operation=operation,
            node_output=node_output,
            rhs_assignment=rhs_assignment,
            literal_value=literal_value,
        )

        self._variable_mutations.append(mutation)

    def format(self, format: str, *args: Any) -> LHFormatString:
        """Generates a LHFormatString object that can be understood
        by the ThreadBuilder.

        Args:
            format (str): String format with variables with
            curly brackets {}.
            *args (Any): Arguments.

        Returns:
            LHFormatString: A LHFormatString.
        """
        return LHFormatString(format, *args)

    def add_variable(
        self,
        variable_name: str,
        variable_type: VariableType,
        access_level: Optional[Union[WfRunVariableAccessLevel, str]] = PRIVATE_VAR,
        default_value: Any = None,
    ) -> WfRunVariable:
        """Defines a Variable in the ThreadSpec and returns a handle to it.

        Args:
            variable_name (str): The name of the variable.
            variable_type (VariableType): The variable type.
            access_level (WfRunVariableAccessLevel): Sets the access level of a WfRunVariable.
            default_value (Any, optional): A default value. Defaults to None.

        Returns:
            WfRunVariable: A handle to the created WfRunVariable.
        """
        self._check_if_active()

        if len(self._nodes) > 0 and self._last_node().node_case == NodeCase.EXIT:
            raise TypeError(
                "You cannot add a variable in a given thread after the thread has completed."
            )

        for var in self._wf_run_variables:
            if var.name == variable_name:
                raise ValueError(f"Variable {variable_name} already added")

        new_var = WfRunVariable(
            variable_name, variable_type, self, default_value, access_level
        )
        self._wf_run_variables.append(new_var)
        return new_var

    def find_variable(self, variable_name: str) -> WfRunVariable:
        """Search for a variable.

        Args:
            variable_name (str): he name of the variable.

        Returns:
            WfRunVariable: Variable found.
        """

        # let's validate the special INPUT variable
        if variable_name == "INPUT":
            for var in self._wf_run_variables:
                if var.name == variable_name:
                    return var
            raise ValueError(f"Variable {variable_name} unaccessible")

        for builder in self._workflow._builders:
            for var in builder._wf_run_variables:
                if var.name == variable_name:
                    return var

        raise ValueError(f"Variable {variable_name} not found")

    def add_node(self, name: str, sub_node: NodeType) -> str:
        """Add a given node.

        Args:
            name (str): Name of the node.
            sub_node (NodeType): One of node: [TaskNode, EntrypointNode,
            ExitNode, ExternalEventNode, SleepNode, StartThreadNode,
            WaitForThreadsNode,  NopNode, UserTaskNode, ThrowEventNode]

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

            if last_node.node_case != NodeCase.EXIT:
                last_node.outgoing_edges.append(
                    Edge(
                        sink_node_name=next_node_name,
                        variable_mutations=self._collect_variable_mutations(),
                        condition=self._last_node_condition,
                    )
                )
                self._last_node_condition = None
            elif node_type != NodeCase.NOP:
                raise TypeError(
                    "You cannot add a Node in a given thread after the thread has completed."
                )

        self._nodes.append(WorkflowNode(next_node_name, node_type, sub_node))
        self._last_node_name = next_node_name

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

    def do_while(
        self, condition: WorkflowCondition, while_body: "ThreadInitializer"
    ) -> None:
        """Conditionally executes some workflow code; equivalent to
        an while() statement in programming.

        Args:
            condition (WorkflowCondition): is the WorkflowCondition to be satisfied.
            while_body (ThreadInitializer): is the block of ThreadFunc
            code to be executed while the provided
            WorkflowCondition is satisfied.
        """
        self._check_if_active()
        self._validate_initializer(while_body)

        # execute
        start_node_name = self.add_node("nop", NopNode())
        while_body(self)
        end_node_name = self.add_node("nop", NopNode())

        # get nop nodes
        start_node = self._find_node(start_node_name)
        end_node = self._find_node(end_node_name)

        # configure edges
        while_condition_node = self._find_next_node(start_node_name)
        while_edge = start_node._find_outgoing_edge(while_condition_node.name)
        while_edge.MergeFrom(
            Edge(
                condition=condition.compile(),
            )
        )

        start_node.outgoing_edges.append(
            Edge(sink_node_name=end_node_name, condition=condition.negate().compile())
        )

        end_node.outgoing_edges.append(
            Edge(sink_node_name=start_node_name, condition=condition.compile())
        )

    def do_if(
        self,
        condition: WorkflowCondition,
        if_body: "ThreadInitializer",
        else_body: Optional["ThreadInitializer"] = None,
    ) -> WorkflowIfStatement:
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

        # Adds new chain-able else if functionality
        if else_body is None:
            return self._do_if(condition, if_body)

        self._validate_initializer(else_body)
        self._do_if(condition, if_body).do_else(else_body)

        return WorkflowIfStatement(self, "", "")

    def organize_edges_for_else_if_execution(
        self,
        if_statement: WorkflowIfStatement,
        input_condition: Optional[WorkflowCondition],
        body: "ThreadInitializer",
    ) -> None:
        first_nop_node = self._find_node(if_statement.get_first_nop_node_name())
        else_edge = first_nop_node.outgoing_edges.pop()

        else_if_condition = input_condition.compile() if input_condition else None

        # Get the last node of the parent thread
        last_node_of_parent_thread_name = self._last_node_name
        last_node_of_parent_thread = self._last_node()

        # Execute the Else If body
        body(self)

        # Get the last node of the Else If body to reference later
        last_node_of_body = self._last_node()

        # If no nodes were added from body
        if last_node_of_parent_thread.name == last_node_of_body.name:
            # Add edge from nop 1 to nop 2 with variable mutations
            first_nop_node.outgoing_edges.append(
                Edge(
                    sink_node_name=if_statement.get_last_nop_node_name(),
                    variable_mutations=self._collect_variable_mutations(),
                    condition=else_if_condition,
                )
            )
        # Otherwise, move nodes that were added
        else:
            # Remove edge between last node of parent thread and first node of body
            last_outgoing_edge = last_node_of_parent_thread.outgoing_edges.pop()

            # Get the first node of the body
            first_node_of_body_name = last_outgoing_edge.sink_node_name

            # Add an edge from the first NOP node to the first node of the body
            first_nop_node.outgoing_edges.append(
                Edge(
                    sink_node_name=first_node_of_body_name,
                    variable_mutations=last_outgoing_edge.variable_mutations,
                    condition=else_if_condition,
                )
            )

            # Add edge from last node of the body to last NOP node
            if last_node_of_body.node_case != NodeCase.EXIT:
                last_node_of_body.outgoing_edges.append(
                    Edge(
                        sink_node_name=if_statement.get_last_nop_node_name(),
                        variable_mutations=self._collect_variable_mutations(),
                    )
                )

        # If else condition was not replaced, add it back
        if else_if_condition is not None:
            first_nop_node.outgoing_edges.append(else_edge)

        self._last_node_name = last_node_of_parent_thread_name

    def _do_if(
        self, condition: WorkflowCondition, body: "ThreadInitializer"
    ) -> WorkflowIfStatement:
        first_nop_node_name = self.add_node("nop", NopNode())
        self._last_node_condition = condition.compile()

        body(self)

        last_nop_node_name = self.add_node("nop", NopNode())

        # Add else edge, which should always be LAST
        first_nop_node = self._find_node(first_nop_node_name)
        first_nop_node.outgoing_edges.append(Edge(sink_node_name=last_nop_node_name))

        return WorkflowIfStatement(self, first_nop_node_name, last_nop_node_name)

    def _collect_variable_mutations(self) -> list[VariableMutation]:
        variables_from_if_block = []
        while len(self._variable_mutations) > 0:
            variables_from_if_block.append(self._variable_mutations.popleft())
        return variables_from_if_block

    def cancel_user_task_run_after(
        self, user_task: UserTaskOutput, delay_in_seconds: Union[int, WfRunVariable]
    ) -> None:
        """
        Cancels a User Task Run if it exceeds a specified deadline.
        Args:
            user_task: reference to the UserTaskNode that will be canceled after the deadline.
            delay_in_seconds: delay time after which the User Task Run should be canceled.
        """
        self._check_if_active()
        self._schedule_user_task_cancellation_after(
            user_task, delay_in_seconds, UTActionTrigger.UTHook.ON_ARRIVAL
        )

    def cancel_user_task_run_after_assignment(
        self, user_task: UserTaskOutput, delay_in_seconds: Union[int, WfRunVariable]
    ) -> None:
        """
        Cancels a User Task Run if it exceeds a specified deadline after it is assigned.
        Args:
            user_task: reference to the UserTaskNode that will be canceled after the deadline.
            delay_in_seconds: delay time after which the User Task Run should be canceled.
        """
        self._check_if_active()
        self._schedule_user_task_cancellation_after(
            user_task, delay_in_seconds, UTActionTrigger.UTHook.ON_TASK_ASSIGNED
        )

    def _schedule_user_task_cancellation_after(
        self,
        user_task: UserTaskOutput,
        delay_in_seconds: Union[int, WfRunVariable],
        hook: UTActionTrigger.UTHook,
    ) -> None:
        if self._last_node().name != user_task.node_name:
            raise ValueError("Tried to reassign stale user task node!")

        cancel = UTActionTrigger.UTACancel()

        ut_node: UserTaskNode = typing.cast(UserTaskNode, self._last_node().sub_node)
        ut_node.actions.append(
            UTActionTrigger(
                cancel=cancel,
                delay_seconds=to_variable_assignment(delay_in_seconds),
                hook=hook,
            )
        )


def python_type_to_return_type(py_type: type | None) -> ReturnType:
    """
    Maps a Python type to a ReturnType.

    Args:
        py_type (type): The Python type.

    Returns:
        ReturnType: The corresponding ReturnType.

    Raises:
        ValueError: If the type is unsupported.
    """
    if py_type is None:
        raise ValueError("Payload type must be set before generating the request.")
    type_def = TypeDefinition()
    if py_type is str:
        type_def.type = VariableType.STR
    elif py_type is int:
        type_def.type = VariableType.INT
    elif py_type is float:
        type_def.type = VariableType.DOUBLE
    elif py_type is bool:
        type_def.type = VariableType.BOOL
    elif issubclass(py_type, dict):
        type_def.type = VariableType.JSON_OBJ
    elif issubclass(py_type, list):
        type_def.type = VariableType.JSON_ARR
    else:
        raise ValueError("Unsupported payload type.")
    return ReturnType(return_type=type_def)


ThreadInitializer = Callable[[WorkflowThread], None]


class Workflow:
    def __init__(
        self,
        name: str,
        entrypoint: ThreadInitializer,
        parent_wf: Optional[str] = None,
    ) -> None:
        """Workflow.

        Args:
            name (str): Name of WfSpec.
            entrypoint (ThreadInitializer): Is the entrypoint thread function.
            parent_wf (Optional[str]): Defines the parent WfSpec associated to this Workflow
        """
        if name is None:
            raise ValueError("Name cannot be None")

        self.name = name
        self._entrypoint = entrypoint
        self._thread_initializers: list[tuple[str, ThreadInitializer]] = []
        self._builders: list[WorkflowThread] = []
        self._allowed_updates: Optional[AllowedUpdateType] = None
        self._parent_wf: Optional[WfSpec.ParentWfSpecReference] = None
        self._retention_policy: Optional[WorkflowRetentionPolicy] = None
        self._default_timeout_seconds: Optional[int] = None
        self._default_exponential_backoff: Optional[ExponentialBackoffRetryPolicy] = (
            None
        )
        self._default_retries: Optional[int] = None
        self._workflow_events_to_register: list[ThrowEventNodeOutput] = []
        self._external_events_to_register: list[ExternalEventNodeOutput] = []
        if parent_wf is not None:
            self._parent_wf = WfSpec.ParentWfSpecReference(wf_spec_name=parent_wf)

    def get_threads(self) -> list[WorkflowThread]:
        """Get the threads.

        Returns:
            list[WorkflowThread]: The threads.
        """
        return self._builders

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

    def with_update_type(self, update_type: AllowedUpdateType) -> Workflow:
        """Defines the type of update to perform when saving the WfSpec:

        Args:
            update_type(AllowedUpdateType):
                AllowedUpdateType.ALL_UPDATES (Default): Creates a new WfSpec with a different version
                (either major or revision).
                AllowedUpdateType.MINOR_REVISION_UPDATES: Creates a new WfSpec with a different revision
                if the change is a major version it fails.
                AllowedUpdateType.NO_UPDATES: Fail with the ALREADY_EXISTS response code.

        Returns: This instance.
        """
        self._allowed_updates = update_type
        return self

    def compile(self) -> PutWfSpecRequest:
        """Compile the workflow into Protobuf Objects.

        Returns:
            PutWfSpecRequest: Spec.
        """
        self.add_sub_thread(ENTRYPOINT, self._entrypoint)
        threads_iterator = iter(self._thread_initializers)
        thread_specs: dict[str, ThreadSpec] = {}

        for name, initializer in threads_iterator:
            builder = WorkflowThread(
                self,
                initializer,
                self._default_retries,
                self._default_exponential_backoff,
                self._default_timeout_seconds,
            )
            thread_specs[name] = builder.compile()

        self._thread_initializers = []
        self._builders = []

        return PutWfSpecRequest(
            name=self.name,
            entrypoint_thread_name=ENTRYPOINT,
            thread_specs=thread_specs,
            allowed_updates=self._allowed_updates,
            parent_wf_spec=self._parent_wf,
            retention_policy=self._retention_policy,
        )

    def with_retention_policy(self, policy: WorkflowRetentionPolicy) -> Workflow:
        """Sets the retention policy for all WfRun's created by this WfSpec.

        Args:
            policy(WorkflowRetentionPolicy): Workflow Retention Policy

        Returns: This instance.
        """
        self._retention_policy = policy
        return self

    def with_retry_policy(
        self,
        retries: Optional[int] = None,
        exponential_backoff: Optional[ExponentialBackoffRetryPolicy] = None,
    ) -> Workflow:
        """Configures the default retry behaviour of the tasks.

        Args:
            retries(Optional[int]): Number of retries.
            exponential_backoff(Optional[ExponentialBackoffRetryPolicy]): Tells the Workflow to configure (by default)
                the specified ExponentialBackoffRetryPolicy as the retry policy.

        Returns: This instance.
        """
        self._default_retries = retries
        self._default_exponential_backoff = exponential_backoff
        return self

    def with_task_timeout_seconds(
        self, timeout_seconds: Optional[int] = None
    ) -> Workflow:
        """Configures the default timeout length (seconds) of the tasks.

        Args:
            timeout_seconds(Optional[int]): Length of time before the TaskRun times out.

        Returns: This instance.
        """
        self._default_timeout_seconds = timeout_seconds
        return self

    def add_workflow_event_def_to_register(self, node: ThrowEventNodeOutput) -> None:
        """
        Adds a workflow event definition to the list for registration.

        Args:
            node (ThrowEventNodeOutput): The workflow event node to register.
        """
        self._workflow_events_to_register.append(node)

    def add_external_event_def_to_register(self, node: ExternalEventNodeOutput) -> None:
        """
        Adds an external event definition to the list for registration.

        Args:
            node (ExternalEventNodeOutput): The external event node to register.
        """
        self._external_events_to_register.append(node)

    @property
    def external_events_to_register(self) -> list[ExternalEventNodeOutput]:
        return self._external_events_to_register

    @property
    def workflow_events_to_register(self) -> list[ThrowEventNodeOutput]:
        return self._workflow_events_to_register


def create_workflow_spec(
    workflow: Workflow, config: LHConfig, timeout: Optional[int] = None
) -> None:
    """Creates a given workflow spec at the LH Server.

    Args:
        workflow (Workflow): The workflow.
        config (LHConfig): The configuration to get connected to the LH Server.
        timeout (Optional[int]): Timeout
    """
    stub = config.stub()
    request = workflow.compile()

    for external_node in workflow.external_events_to_register:
        external_event_request = external_node.to_put_external_event_def_request()
        external_event_response = stub.PutExternalEventDef(
            external_event_request, timeout=timeout
        )
        logging.info(
            f"Registered ExternalEventDef: \n{MessageToJson(external_event_response)}"
        )

    for workflow_node in workflow.workflow_events_to_register:
        workflow_event_request = workflow_node.to_put_workflow_event_def_request()
        workflow_event_response = stub.PutWorkflowEventDef(
            workflow_event_request, timeout=timeout
        )
        logging.info(
            f"Registered WorkflowEventDef: \n{MessageToJson(workflow_event_response)}"
        )

    logging.info(f"Creating a new version of {workflow.name}:\n{workflow}")
    stub.PutWfSpec(request, timeout=timeout)


def create_task_def(
    task: Callable[..., Any], name: str, config: LHConfig, timeout: Optional[int] = None
) -> None:
    """Creates a new TaskDef at the LH Server.

    Args:
        task (Callable[..., Any]): The task.
        name (str): Name of the task.
        config (LHConfig): The configuration to get connected to the LH Server.
        timeout (Optional[int]): Timeout
    """
    _create_task_def(task, name, config, timeout=timeout)


def create_external_event_def(
    name: str,
    config: LHConfig,
    timeout: Optional[int] = None,
    correlated_event_config: Optional[CorrelatedEventConfig] = None,
) -> None:
    """Creates a new ExternalEventDef at the LH Server.

    Args:
        name (str): Name of the external event.
        config (LHConfig): The configuration to get connected to the LH Server.
        timeout (Optional[int]): Timeout
    """
    stub = config.stub()
    request = PutExternalEventDefRequest(
        name=name, correlated_event_config=correlated_event_config
    )
    stub.PutExternalEventDef(request, timeout=timeout)
    logging.info(f"ExternalEventDef {name} was created:\n{to_json(request)}")


def create_workflow_event_def(
    name: str, type: VariableType, config: LHConfig, timeout: Optional[int] = None
) -> None:
    """Creates a new WorkflowEventDef at the LH Server.

    Args:
        name (str): Name of the workflow event.
        config (LHConfig): The configuration to get connected to the LH Server.
        timeout (Optional[int]): Timeout
    """
    stub = config.stub()
    request = PutWorkflowEventDefRequest(
        name=name,
        content_type=ReturnType(TypeDefinition(type=type)),
    )
    stub.PutWorkflowEventDef(request, timeout=timeout)
    logging.info(f"WorkflowEventDef {name} was created:\n{to_json(request)}")
