import asyncio
import logging
from pathlib import Path
import random

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, Comparator, VariableMutationType
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
        counter = wf.declare_int("counter").with_default(2)
        def subtract(handler: WorkflowThread) -> None:
            handler.mutate(counter, VariableMutationType.SUBTRACT, 1)
        wf.wait_for_condition(wf.condition(counter, Comparator.EQUALS, 0))
        wf.add_interrupt_handler("subtract", subtract)

    return Workflow("example-wait-for-condition", my_entrypoint)


async def main() -> None:
    config = get_config()
    wf = get_workflow()

    littlehorse.create_external_event_def("subtract", config)
    littlehorse.create_workflow_spec(wf, config)


if __name__ == "__main__":
    asyncio.run(main())
