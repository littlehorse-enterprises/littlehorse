from datetime import datetime
from enum import Enum
from typing import Any, List, Mapping, Optional, Union

from pydantic import Field

from lh_sdk.utils import LHBaseModel


class LHDeployStatus(Enum):
    STARTING = 'STARTING'
    RUNNING = 'RUNNING'
    COMPLETED = 'COMPLETED'
    STOPPING = 'STOPPING'
    STOPPED = 'STOPPED'
    DESIRED_REDEPLOY = 'DESIRED_REDEPLOY'
    ERROR = 'ERROR'


class WFRunVariableTypeEnum(Enum):
    OBJECT = 'OBJECT'
    ARRAY = 'ARRAY'
    INT = 'INT'
    FLOAT = 'FLOAT'
    BOOLEAN = 'BOOLEAN'
    STRING = 'STRING'


class WFRunVariableDefSchema(LHBaseModel):
    type: WFRunVariableTypeEnum
    default_value: Optional[Any] = None


class InterruptDefSchema(LHBaseModel):
    handler_thread_name: str


class WFRunMetadataEnum(Enum):
    THREAD_GUID = 'THREAD_GUID'
    THREAD_ID = 'THREAD_ID'
    WF_RUN_GUID = 'WF_RUN_GUID'
    WF_SPEC_GUID = 'WF_SPEC_GUID'
    WF_SPEC_NAME = 'WF_SPEC_NAME'


class VariableAssignmentSchema(LHBaseModel):
    wf_run_variable_name: Optional[str] = None
    literal_value: Optional[Any] = None
    wf_run_metadata: Optional[WFRunMetadataEnum] = None

    json_path: Optional[str] = None
    default_value: Optional[Any] = None


class NodeType(Enum):
    TASK = 'TASK'
    EXTERNAL_EVENT = 'EXTERNAL_EVENT'
    SPAWN_THREAD = 'SPAWN_THREAD'
    WAIT_FOR_THREAD = 'WAIT_FOR_THREAD'
    SLEEP = 'SLEEP'
    NOP = 'NOP'
    THROW_EXCEPTION = 'THROW_EXCEPTION'


class LHComparisonEnum(Enum):
    LESS_THAN = 'LESS_THAN'
    GREATER_THAN = 'GREATER_THAN'
    LESS_THAN_EQ = 'LESS_THAN_EQ'
    GREATER_THAN_EQ = 'GREATER_THAN_EQ'
    EQUALS = 'EQUALS'
    NOT_EQUALS = 'NOT_EQUALS'
    IN = 'IN'
    NOT_IN = 'NOT_IN'


CONDITION_INVERSES = {
    LHComparisonEnum.LESS_THAN: LHComparisonEnum.GREATER_THAN_EQ,
    LHComparisonEnum.GREATER_THAN: LHComparisonEnum.LESS_THAN_EQ,
    LHComparisonEnum.LESS_THAN_EQ: LHComparisonEnum.GREATER_THAN,
    LHComparisonEnum.GREATER_THAN_EQ: LHComparisonEnum.LESS_THAN,
    LHComparisonEnum.EQUALS: LHComparisonEnum.NOT_EQUALS,
    LHComparisonEnum.NOT_EQUALS: LHComparisonEnum.EQUALS,
    LHComparisonEnum.IN: LHComparisonEnum.NOT_IN,
    LHComparisonEnum.NOT_IN: LHComparisonEnum.IN,
}


ACCEPTABLE_TYPES = Union[str, list, dict, int, bool, float]
ACCEPTABLE_TYPES_LIST = [str, list, dict, int, bool, float]
TYPE_TO_ENUM: Mapping[type[ACCEPTABLE_TYPES], WFRunVariableTypeEnum]= {
    str: WFRunVariableTypeEnum.STRING,
    list: WFRunVariableTypeEnum.ARRAY,
    dict: WFRunVariableTypeEnum.OBJECT,
    bool: WFRunVariableTypeEnum.BOOLEAN,
    float: WFRunVariableTypeEnum.FLOAT,
    int: WFRunVariableTypeEnum.INT,
}


class EdgeConditionSchema(LHBaseModel):
    left_side: VariableAssignmentSchema
    right_side: VariableAssignmentSchema
    comparator: LHComparisonEnum


class EdgeSchema(LHBaseModel):
    source_node_name: Optional[str]
    sink_node_name: str
    condition: Optional[EdgeConditionSchema] = None


class VariableMutationOperation(Enum):
    ASSIGN = 'ASSIGN'
    ADD = 'ADD'
    EXTEND = 'EXTEND'
    SUBTRACT = 'SUBTRACT'
    MULTIPLY = 'MULTIPLY'
    DIVIDE = 'DIVIDE'
    REMOVE_IF_PRESENT = 'REMOVE_IF_PRESENT'
    REMOVE_INDEX = 'REMOVE_INDEX'
    REMOVE_KEY = 'REMOVE_KEY'


class VariableMutationSchema(LHBaseModel):
    operation: VariableMutationOperation
    json_path: Optional[str] = None
    literal_value: Optional[Any] = None
    source_variable: Optional[VariableAssignmentSchema] = None


class ExceptionHandlerSpecSchema(LHBaseModel):
    handler_thread_spec_name: str


class NodeSchema(LHBaseModel):
    timeout_seconds: Optional[VariableAssignmentSchema] = None
    num_retries: int = 0
    node_type: NodeType = NodeType.TASK
    outgoing_edges: List[EdgeSchema] = Field(default_factory=lambda: list([]))

    variables: Optional[dict[str, VariableAssignmentSchema]] = None
    external_event_def_name: Optional[str] = None
    thread_wait_thread_id: Optional[VariableAssignmentSchema] = None
    thread_spawn_thread_spec_name: Optional[str] = None

    variable_mutations: dict[str, VariableMutationSchema] = Field(
        default_factory=lambda: dict([])
    )
    task_def_name: Optional[str] = None

    exception_to_throw: Optional[str] = None
    base_exceptionhandler: Optional[ExceptionHandlerSpecSchema] = None
    custom_exception_handlers: Optional[
        dict[str, ExceptionHandlerSpecSchema]
    ] = None


class ThreadSpecSchema(LHBaseModel):
    name: str
    entrypoint_node_name: Optional[str] = None

    variable_defs: dict[str, WFRunVariableDefSchema] = Field(
        default_factory=lambda: dict({})
    )
    interrupt_defs: Optional[dict[str, InterruptDefSchema]] = None

    nodes: dict[str, NodeSchema] = Field(default_factory=lambda: dict({}))
    edges: list[EdgeSchema] = Field(default_factory=lambda: list([]))


class WFSpecSchema(LHBaseModel):
    created: Optional[datetime] = None
    name: str
    status: LHDeployStatus = LHDeployStatus.STOPPED
    status_message: Optional[str] = None
    desired_status: LHDeployStatus = LHDeployStatus.RUNNING

    thread_specs: dict[str, ThreadSpecSchema] = Field(default_factory=dict)
    interrupt_events: Optional[List[str]] = None

    entrypoint_thread_name: str
    wf_deployer_class_name: Optional[str] = None
    deploy_metadata: Optional[str] = None


class TaskDefSchema(LHBaseModel):
    created: Optional[datetime] = None
    version_number: int = Field(default_factory=lambda: 0)
    required_vars: Optional[Mapping[str, WFRunVariableDefSchema]] = None
    partitions: int = 3
    name: str
    status: LHDeployStatus = LHDeployStatus.RUNNING
    status_message: Optional[str] = None

    task_deployer_class_name: Optional[str] = None
    deploy_metadata: Optional[str] = None

    @property
    def kafka_topic(self) -> str:
        return self.name

    @property
    def id(self) -> str:
        return self.name


class ExternalEventDefSchema(LHBaseModel):
    created: Optional[datetime] = None
    name: str
    status: LHDeployStatus = LHDeployStatus.RUNNING

    @property
    def id(self) -> str:
        return self.name


class TaskImplTypeEnum(Enum):
    PYTHON = 'PYTHON'
    JAVA = 'JAVA'


class DockerTaskDeployMetadata(LHBaseModel):
    docker_image: str
    metadata: str = Field(default_factory=lambda: "")
    customValidatorClassName: Optional[str] = None
    taskExecutorClassName: Optional[str] = None
    task_type: TaskImplTypeEnum
    env: Mapping[str, str] = Field(default_factory=lambda: dict({}))

    python_module: Optional[str] = None
    python_function: Optional[str] = None
