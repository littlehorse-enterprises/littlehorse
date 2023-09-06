import asyncio
import functools
import json
from pathlib import Path
import signal
import sys
from typing import Any, Optional, Union

from google.protobuf.timestamp_pb2 import Timestamp
from google.protobuf.message import Message
from google.protobuf.json_format import MessageToJson

from littlehorse.exceptions import SerdeException
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import VariableAssignment
from littlehorse.model.variable_pb2 import VariableValue

VARIABLE_TYPE_MAP = {
    VariableType.JSON_OBJ: dict[str, Any],
    VariableType.JSON_ARR: list[Any],
    VariableType.DOUBLE: float,
    VariableType.BOOL: bool,
    VariableType.STR: str,
    VariableType.INT: int,
    VariableType.BYTES: bytes,
}

TYPE_VARIABLE_MAP = {value: key for key, value in VARIABLE_TYPE_MAP.items()}


def timestamp_now() -> Timestamp:
    """Return a Timestamp protobuf object.

    Returns:
        Timestamp: Timestamp protobuf object.
    """
    current_time = Timestamp()
    current_time.GetCurrentTime()
    return current_time


def proto_to_json(proto: Message) -> str:
    """Convert a proto object to json.

    Args:
        proto (Message): A proto object.

    Returns:
        str: JSON format.
    """
    return MessageToJson(proto)


def parse_value(value: Any) -> VariableValue:
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


def parse_type(lh_type: VariableType) -> Any:
    """Receives a LH type and return a python type.

    Args:
        lh_type (VariableType): LH Type.

    Returns:
        Any: Python type.
    """
    return VARIABLE_TYPE_MAP[lh_type]


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


def shutdown_hook(*workers: "LHTaskWorker") -> None:
    """Add a shutdown hook for multiples workers"""

    def stop_workers(*workers: "LHTaskWorker") -> None:
        for worker in workers:
            worker.stop()

    loop = asyncio.get_running_loop()

    for sig in (signal.SIGHUP, signal.SIGTERM, signal.SIGINT):
        loop.add_signal_handler(sig, functools.partial(stop_workers, *workers))


async def start_workers(*workers: "LHTaskWorker") -> None:
    """Starts a list of workers"""
    shutdown_hook(*workers)
    tasks = [asyncio.create_task(worker.start()) for worker in workers]
    await asyncio.gather(*tasks)


def parse_variable_assignment(value: Any) -> VariableAssignment:
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
                format=parse_variable_assignment(value.format),
                args=[parse_variable_assignment(arg) for arg in value.args],
            )
        )

        return new_var

    return VariableAssignment(
        literal_value=parse_value(value),
    )


# import circular import at the end
from littlehorse.workflow import FormatString, NodeOutput, WfRunVariable  # noqa: E402
from littlehorse.worker import LHTaskWorker  # noqa: E402
