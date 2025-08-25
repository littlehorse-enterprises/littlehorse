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


def get_parent_wf() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        the_name = wf.add_variable("input-name", VariableType.STR)
        wf.execute("greet", the_name)

    return Workflow("parent-wf", my_entrypoint)


def get_child_wf() -> Workflow:
    def my_entry_point(wf: WorkflowThread) -> None:
        the_name = wf.add_variable(
            "input-name", VariableType.STR, access_level="INHERITED_VAR"
        )
        wf.execute("greet", the_name)

    return Workflow("child-wf", my_entry_point, "parent-wf")


async def greeting(name: str, ctx: WorkerContext) -> str:
    msg = f"Hello {name}!. WfRun {ctx.wf_run_id.id}"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg


async def main() -> None:
    config = get_config()
    parent_wf = get_parent_wf()
    child_wf = get_child_wf()

    littlehorse.create_task_def(greeting, "greet", config)
    littlehorse.create_workflow_spec(parent_wf, config)
    littlehorse.create_workflow_spec(child_wf, config)

    await littlehorse.start(LHTaskWorker(greeting, "greet", config))


if __name__ == "__main__":
    asyncio.run(main())
