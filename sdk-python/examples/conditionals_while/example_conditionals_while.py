import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import Comparator, VariableMutationType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import ThreadBuilder, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


async def eat_donut(donuts_left: int) -> str:
    msg = f"Donuts left: {donuts_left}"
    print(msg)
    return msg


def while_body(thread: ThreadBuilder) -> None:
    donuts = thread.find_variable("number-of-donuts")
    thread.mutate(donuts, VariableMutationType.SUBTRACT, 1)
    thread.execute("eating-donut", donuts)


def entrypoint(thread: ThreadBuilder) -> None:
    donuts = thread.add_variable("number-of-donuts", VariableType.INT)
    condition = thread.condition(donuts, Comparator.GREATER_THAN, 0)
    thread.do_while(condition, while_body)


def get_workflow() -> Workflow:
    return Workflow("example-conditionals-while", entrypoint)


async def main() -> None:
    config = get_config()

    littlehorse.create_task_def(eat_donut, "eating-donut", config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(LHTaskWorker(eat_donut, "eating-donut", config))


if __name__ == "__main__":
    asyncio.run(main())
