import asyncio
import functools
import json
from pathlib import Path
import signal
import sys
from typing import TYPE_CHECKING, Any, Union

from littlehorse.model.service_pb2 import VariableTypePb, VariableValuePb

if TYPE_CHECKING:
    from littlehorse.worker import LHTaskWorker

VARIABLE_TYPES_MAP = {
    VariableTypePb.JSON_OBJ: dict[str, Any],
    VariableTypePb.JSON_ARR: list[Any],
    VariableTypePb.DOUBLE: float,
    VariableTypePb.BOOL: bool,
    VariableTypePb.STR: str,
    VariableTypePb.INT: int,
    VariableTypePb.BYTES: bytes,
}

VALUE_TYPES_MAP = {
    VariableTypePb.JSON_OBJ: lambda v: json.loads(v.json_obj),
    VariableTypePb.JSON_ARR: lambda v: json.loads(v.json_arr),
    VariableTypePb.DOUBLE: lambda v: v.float,
    VariableTypePb.BOOL: lambda v: v.bool,
    VariableTypePb.STR: lambda v: v.str,
    VariableTypePb.INT: lambda v: v.int,
    VariableTypePb.BYTES: lambda v: v.bytes,
}


def parse_type(lh_type: VariableTypePb) -> Any:
    """Receives a LH type and return a python type.

    Args:
        lh_type (VariableTypePb): LH Type.

    Returns:
        Any: Python type.
    """
    return VARIABLE_TYPES_MAP[lh_type]


def parse_value(lh_value: VariableValuePb) -> Any:
    """Receives a LH value and maps it to a python object.

    Args:
        lh_value (VariableValuePb): LH Value.

    Returns:
        Any: Python value.
    """
    return VALUE_TYPES_MAP[lh_value.type](lh_value)


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
