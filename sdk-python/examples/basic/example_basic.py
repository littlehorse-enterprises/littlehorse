import asyncio
import logging
from pathlib import Path
import random

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType
from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        the_name = wf.add_variable("input-name", VariableType.STR)
        wf.execute("greet", the_name)

    return Workflow("example-basic", my_entrypoint)


async def greeting(name: str, ctx: WorkerContext) -> str:
    msg = f"Hello {name}!. WfRun {ctx.wf_run_id.id}"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg


async def main() -> None:
    config = get_config()
    wf = get_workflow()

    littlehorse.create_task_def(greeting, "greet", config)
    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(LHTaskWorker(greeting, "greet", config))


if __name__ == "__main__":
    asyncio.run(main())
