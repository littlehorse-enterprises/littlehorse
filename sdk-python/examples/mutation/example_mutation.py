import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, VariableMutationType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)

SPIDER_BITE = "spider-bite"
DAILY_BUGLE = "daily-bugle"


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        # it receives a name
        name = wf.add_variable("name", VariableType.STR).masked()

        # it sends the name to a task worker
        output = wf.execute(SPIDER_BITE, name)

        # it assigns the result to the variable name
        wf.mutate(name, VariableMutationType.ASSIGN, output)

        # it pass the variable to another task
        wf.execute(DAILY_BUGLE, name)

    return Workflow("example-mutation", my_entrypoint)


async def spider_bite(name: str) -> str:
    if name in ["Peter", "Miles", "Gwen"]:
        msg = f"{name} transforms into Spider-Man!."
    else:
        msg = f"{name} remains the same."
    print("The spider has bitten")
    return msg


async def daily_bugle(news: str) -> None:
    print(news)


async def main() -> None:
    config = get_config()
    wf = get_workflow()

    littlehorse.create_task_def(spider_bite, SPIDER_BITE, config)
    littlehorse.create_task_def(daily_bugle, DAILY_BUGLE, config)
    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(
        LHTaskWorker(spider_bite, SPIDER_BITE, config),
        LHTaskWorker(daily_bugle, DAILY_BUGLE, config),
    )


if __name__ == "__main__":
    asyncio.run(main())
