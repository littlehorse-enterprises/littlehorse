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

def get_child() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        child_input_name = wf.declare_str("child-input-name")
        wf.execute("greet", child_input_name)

    return Workflow("some-other-wfspec", my_entrypoint)

def get_parent() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        the_name = wf.declare_str("input-name")
        child_output = wf.declare_str("child-output")

        child = wf.run_wf("some-other-wfspec", {"child-input-name": the_name})
        wf.execute("greet", "hi from parent")

        child_output.assign(wf.wait_for_child_wf(child))

    return Workflow("my-parent", my_entrypoint)


async def greeting(name: str, ctx: WorkerContext) -> str:
    msg = f"Hello {name}!. WfRun {ctx.wf_run_id.id}"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg

async def main() -> None:
    config = get_config()
    parent = get_parent()
    child = get_child()

    littlehorse.create_task_def(greeting, "greet", config)

    littlehorse.create_workflow_spec(child, config)
    littlehorse.create_workflow_spec(parent, config)

    await littlehorse.start(LHTaskWorker(greeting, "greet", config))


if __name__ == "__main__":
    asyncio.run(main())
