"""Collections of utils intended to be use by the sdk user"""

import functools
from inspect import signature
import logging
import signal
from typing import Any, Callable
from grpc import RpcError, StatusCode
from littlehorse.config import LHConfig
from littlehorse.model.common_wfspec_pb2 import VariableDef
from littlehorse.model.object_id_pb2 import GetLatestWfSpecRequest
from littlehorse.model.service_pb2 import PutExternalEventDefRequest, PutTaskDefRequest
from littlehorse.proto_utils import (
    to_json,
    to_variable_type,
)
from littlehorse.worker import LHTaskWorker, WorkerContext

import asyncio

from littlehorse.workflow import Workflow


def shutdown_hook(*workers: LHTaskWorker) -> None:
    """Add a shutdown hook for multiples workers"""

    def stop_workers(*workers: LHTaskWorker) -> None:
        for worker in workers:
            worker.stop()

    loop = asyncio.get_running_loop()

    for sig in (signal.SIGHUP, signal.SIGTERM, signal.SIGINT):
        loop.add_signal_handler(sig, functools.partial(stop_workers, *workers))


async def start(*workers: LHTaskWorker) -> None:
    """Starts a list of workers"""
    shutdown_hook(*workers)
    tasks = [asyncio.create_task(worker.start()) for worker in workers]
    await asyncio.gather(*tasks)


def create_workflow_spec(
    workflow: Workflow, config: LHConfig, skip_if_already_exists: bool = True
) -> None:
    """Creates a given workflow spec at the LH Server.

    Args:
        workflow (Workflow): The workflow.
        config (LHConfig): The configuration to get connected to the LH Server.
        skip_if_already_exists (bool, optional): If the workflow exits and
        this is True, then it does not create a new version,
        else it creates a new version. Defaults to True.
    """
    stub = config.stub()

    if skip_if_already_exists:
        try:
            stub.GetLatestWfSpec(GetLatestWfSpecRequest(name=workflow.name))
            logging.info(f"Workflow {workflow.name} already exits, skipping")
            return
        except RpcError as e:
            if e.code() != StatusCode.NOT_FOUND:
                raise e

    request = workflow.compile()
    logging.info(f"Creating a new version of {workflow.name}:\n{workflow}")
    stub.PutWfSpec(request)


def create_task_def(
    task: Callable[..., Any],
    name: str,
    config: LHConfig,
    swallow_already_exists: bool = True,
) -> None:
    """Creates a new TaskDef at the LH Server.

    Args:
        task (Callable[..., Any]): The task.
        name (str): Name of the task.
        config (LHConfig): The config.
        swallow_already_exists (bool, optional): If already exists and this is True,
        it does not raise an exception, else it raise an exception with code
        StatusCode.ALREADY_EXISTS. Defaults to True.
    """
    stub = config.stub()
    try:
        task_signature = signature(task)
        input_vars = [
            VariableDef(name=param.name, type=to_variable_type(param.annotation))
            for param in task_signature.parameters.values()
            if param.annotation is not WorkerContext
        ]
        request = PutTaskDefRequest(name=name, input_vars=input_vars)
        stub.PutTaskDef(request)
        logging.info(f"TaskDef {name} was created:\n{to_json(request)}")
    except RpcError as e:
        if swallow_already_exists and e.code() == StatusCode.ALREADY_EXISTS:
            logging.info(f"TaskDef {name} already exits, skipping")
            return
        raise e


def create_external_event_def(
    name: str,
    config: LHConfig,
    retention_hours: int = -1,
    swallow_already_exists: bool = True,
) -> None:
    """Creates a new ExternalEventDef at the LH Server.

    Args:
        name (str): Name of the external event.
        config (LHConfig): _description_
        retention_hours (int, optional): _description_. Defaults to -1.
        swallow_already_exists (bool, optional): If already exists and this is True,
        it does not raise an exception, else it raise an exception with code
        StatusCode.ALREADY_EXISTS. Defaults to True.
    """
    stub = config.stub()
    try:
        request = PutExternalEventDefRequest(
            name=name,
            retention_hours=None if retention_hours <= 0 else retention_hours,
        )
        stub.PutExternalEventDef(request)
        logging.info(f"ExternalEventDef {name} was created:\n{to_json(request)}")
    except RpcError as e:
        if swallow_already_exists and e.code() == StatusCode.ALREADY_EXISTS:
            logging.info(f"ExternalEventDef {name} already exits, skipping")
            return
        raise e
