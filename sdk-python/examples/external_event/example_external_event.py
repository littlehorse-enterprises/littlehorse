import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import VariableMutationType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import ThreadBuilder, Workflow

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
    def my_entrypoint(thread: ThreadBuilder) -> None:
        thread.execute(ASK_FOR_NAME)
        ext_event_output = thread.wait_for_event(EXT_EVENT, timeout=60)

        name = thread.add_variable("name", VariableType.STR)
        thread.mutate(name, VariableMutationType.ASSIGN, ext_event_output)
        thread.execute(GREET, name)

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
    wf = get_workflow()

    littlehorse.register_external_event(EXT_EVENT, config)
    littlehorse.register_task(ask_for_name, ASK_FOR_NAME, config)
    littlehorse.register_task(greet, GREET, config)
    littlehorse.register_workflow(wf, config)

    await littlehorse.start(
        LHTaskWorker(ask_for_name, ASK_FOR_NAME, config),
        LHTaskWorker(greet, GREET, config),
    )


if __name__ == "__main__":
    asyncio.run(main())
