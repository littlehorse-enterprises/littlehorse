import asyncio
import functools
import json
from pathlib import Path
import signal
import sys
from typing import TYPE_CHECKING, Any, Union

from littlehorse.model.service_pb2 import VariableTypePb, VariableValuePb
from google.protobuf.timestamp_pb2 import Timestamp

if TYPE_CHECKING:
    from littlehorse.worker import LHTaskWorker

VARIABLE_TYPE_MAP = {
    VariableTypePb.JSON_OBJ: dict[str, Any],
    VariableTypePb.JSON_ARR: list[Any],
    VariableTypePb.DOUBLE: float,
    VariableTypePb.BOOL: bool,
    VariableTypePb.STR: str,
    VariableTypePb.INT: int,
    VariableTypePb.BYTES: bytes,
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


def parse_value(value: Any) -> VariableValuePb:
    """Receives a python variable and return a VariableValuePb.

    Args:
        value (Any): Any value returned by a method.

    Returns:
        VariableValuePb: LH Variable.
    """

    def json_encoder(value: Any) -> Any:
        if hasattr(value, "__dict__"):
            return vars(value)
        return value

    if value is None:
        return VariableValuePb(type=VariableTypePb.NULL)
    if isinstance(value, bool):
        return VariableValuePb(type=VariableTypePb.BOOL, bool=value)
    if isinstance(value, str):
        return VariableValuePb(type=VariableTypePb.STR, str=value)
    if isinstance(value, int):
        return VariableValuePb(type=VariableTypePb.INT, int=value)
    if isinstance(value, float):
        return VariableValuePb(type=VariableTypePb.DOUBLE, double=value)
    if isinstance(value, bytes):
        return VariableValuePb(type=VariableTypePb.BYTES, bytes=value)
    if isinstance(value, dict):
        return VariableValuePb(
            type=VariableTypePb.JSON_OBJ,
            json_obj=json.dumps(value, default=json_encoder),
        )
    if isinstance(value, list):
        return VariableValuePb(
            type=VariableTypePb.JSON_ARR,
            json_arr=json.dumps(value, default=json_encoder),
        )

    return VariableValuePb(
        type=VariableTypePb.JSON_OBJ, json_obj=json.dumps(value, default=json_encoder)
    )


def parse_type(lh_type: VariableTypePb) -> Any:
    """Receives a LH type and return a python type.

    Args:
        lh_type (VariableTypePb): LH Type.

    Returns:
        Any: Python type.
    """
    return VARIABLE_TYPE_MAP[lh_type]


def extract_value(lh_value: VariableValuePb) -> Any:
    """Receives a LH value and maps it to a python object.

    Args:
        lh_value (VariableValuePb): LH Value.

    Returns:
        Any: Python value.
    """
    if lh_value.type == VariableTypePb.STR:
        return lh_value.str
    if lh_value.type == VariableTypePb.INT:
        return lh_value.int
    if lh_value.type == VariableTypePb.DOUBLE:
        return lh_value.double
    if lh_value.type == VariableTypePb.BYTES:
        return lh_value.bytes
    if lh_value.type == VariableTypePb.BOOL:
        return lh_value.bool
    if lh_value.type == VariableTypePb.JSON_OBJ:
        return json.loads(lh_value.json_obj)
    if lh_value.type == VariableTypePb.JSON_ARR:
        return json.loads(lh_value.json_arr)

    # VariableTypePb.NULL
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
