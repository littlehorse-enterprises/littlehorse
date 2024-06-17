import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, Comparator, VariableMutationType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import WorkflowThread, Workflow

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


def while_body(wf: WorkflowThread) -> None:
    donuts = wf.find_variable("number-of-donuts")
    wf.mutate(donuts, VariableMutationType.SUBTRACT, 1)
    wf.execute("eating-donut", donuts)


def entrypoint(wf: WorkflowThread) -> None:
    donuts = wf.add_variable("number-of-donuts", VariableType.INT)
    condition = wf.condition(donuts, Comparator.GREATER_THAN, 0)
    wf.do_while(condition, while_body)


def get_workflow() -> Workflow:
    return Workflow("example-conditionals-while", entrypoint)


async def main() -> None:
    config = get_config()

    littlehorse.create_task_def(eat_donut, "eating-donut", config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(LHTaskWorker(eat_donut, "eating-donut", config))


if __name__ == "__main__":
    asyncio.run(main())
