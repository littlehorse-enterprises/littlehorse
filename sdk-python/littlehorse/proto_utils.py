"""Collections of utils for managing protobuf objects. With circular import"""

import json
from typing import Any, Optional

from google.protobuf.timestamp_pb2 import Timestamp
from google.protobuf.message import Message
from google.protobuf.json_format import MessageToJson

from littlehorse.exceptions import SerdeException
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import VariableAssignment
from littlehorse.model.variable_pb2 import VariableValue


VARIABLE_TYPE_TO_TYPE_MAP = {
    VariableType.JSON_OBJ: dict[str, Any],
    VariableType.JSON_ARR: list[Any],
    VariableType.DOUBLE: float,
    VariableType.BOOL: bool,
    VariableType.STR: str,
    VariableType.INT: int,
    VariableType.BYTES: bytes,
}

TYPE_TO_VARIABLE_TYPE_MAP = {
    value: key for key, value in VARIABLE_TYPE_TO_TYPE_MAP.items()
}


def timestamp_now() -> Timestamp:
    """Return a Timestamp protobuf object.

    Returns:
        Timestamp: Timestamp protobuf object.
    """
    current_time = Timestamp()
    current_time.GetCurrentTime()
    return current_time


def to_json(proto: Message) -> str:
    """Convert a proto object to json.

    Args:
        proto (Message): A proto object.

    Returns:
        str: JSON format.
    """
    return MessageToJson(proto)


def to_variable_value(value: Any) -> VariableValue:
    """Receives a python variable and return a VariableValue.

    Args:
        value (Any): Any value returned by a method.

    Returns:
        VariableValue: LH Variable.
    """

    def json_encoder(value: Any) -> Any:
        if hasattr(value, "__dict__"):
            return vars(value)
        return value

    if value is None:
        return VariableValue(type=VariableType.NULL)
    if isinstance(value, bool):
        return VariableValue(type=VariableType.BOOL, bool=value)
    if isinstance(value, str):
        return VariableValue(type=VariableType.STR, str=value)
    if isinstance(value, int):
        return VariableValue(type=VariableType.INT, int=value)
    if isinstance(value, float):
        return VariableValue(type=VariableType.DOUBLE, double=value)
    if isinstance(value, bytes):
        return VariableValue(type=VariableType.BYTES, bytes=value)

    try:
        if isinstance(value, dict):
            return VariableValue(
                type=VariableType.JSON_OBJ,
                json_obj=json.dumps(value, default=json_encoder),
            )
        if isinstance(value, list):
            return VariableValue(
                type=VariableType.JSON_ARR,
                json_arr=json.dumps(value, default=json_encoder),
            )

        return VariableValue(
            type=VariableType.JSON_OBJ, json_obj=json.dumps(value, default=json_encoder)
        )
    except Exception as e:
        raise SerdeException(
            f"Error when serializing value: '{value}' of type '{type(value)}'"
        ) from e


def to_type(lh_type: VariableType) -> type:
    """Receives a LH type and return a python type.

    Args:
        lh_type (VariableType): LH Type.

    Returns:
        Any: Python type.
    """
    type_to_return = VARIABLE_TYPE_TO_TYPE_MAP.get(lh_type)

    if type_to_return is None:
        raise ValueError("VariableType not found")

    return type_to_return


def to_variable_type(python_type: type) -> VariableType:
    """Receives a python type and return a LH Type.

    Args:
        python_type (type): Type.

    Returns:
        VariableType: LH TYpe.
    """
    type_to_return = TYPE_TO_VARIABLE_TYPE_MAP.get(python_type)

    if type_to_return is None:
        raise ValueError(f"Type {python_type} not supported")

    return type_to_return


def extract_value(lh_value: VariableValue) -> Any:
    """Receives a LH value and maps it to a python object.

    Args:
        lh_value (VariableValue): LH Value.

    Returns:
        Any: Python value.
    """
    if lh_value.type == VariableType.STR:
        return lh_value.str
    if lh_value.type == VariableType.INT:
        return lh_value.int
    if lh_value.type == VariableType.DOUBLE:
        return lh_value.double
    if lh_value.type == VariableType.BYTES:
        return lh_value.bytes
    if lh_value.type == VariableType.BOOL:
        return lh_value.bool

    try:
        if lh_value.type == VariableType.JSON_OBJ:
            return json.loads(lh_value.json_obj)
        if lh_value.type == VariableType.JSON_ARR:
            return json.loads(lh_value.json_arr)
    except Exception as e:
        raise SerdeException(f"Error when deserializing {lh_value}") from e

    # VariableType.NULL
    return None


def to_variable_assignment(value: Any) -> VariableAssignment:
    """Receives a value and return a Protobuf VariableAssignment.

    Args:
        value (Any): Any value.

    Returns:
        VariableAssignment: Protobuf.
    """
    if isinstance(value, NodeOutput):
        raise ValueError(
            "Cannot use NodeOutput directly as input to task. "
            "First save to a WfRunVariable."
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

    if isinstance(value, FormatString):
        new_var = VariableAssignment(
            format_string=VariableAssignment.FormatString(
                format=to_variable_assignment(value.format),
                args=[to_variable_assignment(arg) for arg in value.args],
            )
        )

        return new_var

    return VariableAssignment(
        literal_value=to_variable_value(value),
    )


# circular import at the end
from littlehorse.workflow import (  # noqa: E402
    FormatString,
    NodeOutput,
    WfRunVariable,
)
