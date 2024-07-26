import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, VariableMutationType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)

ASK_FOR_NAME = "ask-for-name"
GREET = "greet"
EXT_EVENT = "name-event"


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        wf.execute(ASK_FOR_NAME)
        ext_event_output = wf.wait_for_event(EXT_EVENT, timeout=60)

        name = wf.add_variable("name", VariableType.STR)
        wf.mutate(name, VariableMutationType.ASSIGN, ext_event_output)
        wf.execute(GREET, name)

    return Workflow("example-external-event", my_entrypoint)


async def ask_for_name() -> str:
    msg = "Hi what's your name?"
    print(msg)
    return msg


async def greet(name: str) -> str:
    msg = f"Hello, {name}"
    print(msg)
    return msg


async def main() -> None:
    config = get_config()

    littlehorse.create_external_event_def(EXT_EVENT, config)
    littlehorse.create_task_def(ask_for_name, ASK_FOR_NAME, config)
    littlehorse.create_task_def(greet, GREET, config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(
        LHTaskWorker(ask_for_name, ASK_FOR_NAME, config),
        LHTaskWorker(greet, GREET, config),
    )


if __name__ == "__main__":
    asyncio.run(main())
