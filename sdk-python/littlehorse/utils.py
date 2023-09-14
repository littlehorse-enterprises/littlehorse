"""Collections of utils for general proposes"""

import json
from pathlib import Path
import sys
from typing import Any, Union

from littlehorse.exceptions import SerdeException
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import Comparator
from littlehorse.model.variable_pb2 import VariableValue


def read_binary(file_path: Union[str, Path]) -> bytes:
    """Read a file to bytes.

    Args:
        file_path (Union[str, Path]): File location.

    Returns:
        bytes: File bytes.
    """
    with open(file_path, "rb") as file_input:
        return file_input.read()


def get_event_loop_is_deprecated() -> bool:
    """Verify if asyncio.get_event_loop()
    is deprecated for the current python version.

    https://docs.python.org/3/library/asyncio-eventloop.html#asyncio.get_event_loop.

    Use asyncio.run(main()).

    Returns:
        bool: True if python is greater or equal to 3.10.
    """
    return sys.version_info >= (3, 10, 0)


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


VARIABLE_TYPE_TO_TYPE_MAP = {
    VariableType.JSON_OBJ: dict[str, Any],
    VariableType.JSON_ARR: list[Any],
    VariableType.DOUBLE: float,
    VariableType.BOOL: bool,
    VariableType.STR: str,
    VariableType.INT: int,
    VariableType.BYTES: bytes,
}


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


TYPE_TO_VARIABLE_TYPE_MAP = {
    value: key for key, value in VARIABLE_TYPE_TO_TYPE_MAP.items()
}


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


NEGATE_COMPARATOR_MAP = {
    Comparator.LESS_THAN: Comparator.GREATER_THAN_EQ,
    Comparator.GREATER_THAN_EQ: Comparator.LESS_THAN,
    Comparator.GREATER_THAN: Comparator.LESS_THAN_EQ,
    Comparator.LESS_THAN_EQ: Comparator.GREATER_THAN,
    Comparator.IN: Comparator.NOT_IN,
    Comparator.NOT_IN: Comparator.IN,
    Comparator.EQUALS: Comparator.NOT_EQUALS,
    Comparator.NOT_EQUALS: Comparator.EQUALS,
}


def negate_comparator(comparator: Comparator) -> Comparator:
    """Negates a comparator:

    Comparator.LESS_THAN => Comparator.GREATER_THAN_EQ
    Comparator.GREATER_THAN_EQ => Comparator.LESS_THAN
    Comparator.GREATER_THAN => Comparator.LESS_THAN_EQ
    Comparator.LESS_THAN_EQ => Comparator.GREATER_THAN
    Comparator.IN => Comparator.NOT_IN
    Comparator.NOT_IN => Comparator.IN
    Comparator.EQUALS => Comparator.NOT_EQUALS
    Comparator.NOT_EQUALS => Comparator.EQUALS

    Args:
        comparator (Comparator):Comparator

    Returns:
        Comparator: Comparator.
    """
    negation = NEGATE_COMPARATOR_MAP.get(comparator)
    if negation is None:
        raise ValueError("Comparator not found")
    return negation
