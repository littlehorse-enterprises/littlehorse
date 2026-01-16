import asyncio
import logging
from pathlib import Path
import random

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, Comparator
from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import WorkflowThread, Workflow, WfRunVariable

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:


    def my_entrypoint(wf: WorkflowThread) -> None:
        def substract(handler: WorkflowThread) -> None:
            handler.subtract(counter, 1)
        counter = wf.declare_int("counter", VariableType.INT).with_default(2)
        wf.wait_for_condition(wf.condition(counter, Comparator.EQUALS, 0))
        wf.add_interrupt_handler("subtract", substract)

    return Workflow("example-basic", my_entrypoint)


async def greeting(name: str, ctx: WorkerContext) -> str:
    msg = f"Hello {name}!. WfRun {ctx.wf_run_id.id}"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg


async def main() -> None:
    config = get_config()
    wf = get_workflow()

    littlehorse.create_external_event_def("subtract", config)
    littlehorse.create_task_def(greeting, "greet", config)
    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(LHTaskWorker(greeting, "greet", config))


if __name__ == "__main__":
    asyncio.run(main())
