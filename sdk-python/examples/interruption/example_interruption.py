import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import ThreadBuilder, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_interrupt_handler(thread: ThreadBuilder) -> None:
        thread.execute("interrupt-handler")

    def my_entrypoint(thread: ThreadBuilder) -> None:
        thread.add_interrupt_handler("interruption-event", my_interrupt_handler)
        thread.sleep(30)
        thread.execute("my-task")

    return Workflow("example-interrupt-handler", my_entrypoint)


async def interrupt_handler() -> None:
    print("Executing interrupt-handler")
    raise Exception("Workflow execution stopped")


async def my_task() -> str:
    msg = "Hello, there!"
    print(msg)
    return msg


async def main() -> None:
    config = get_config()

    littlehorse.create_external_event_def("interruption-event", config)
    littlehorse.create_task_def(my_task, "my-task", config)
    littlehorse.create_task_def(interrupt_handler, "interrupt-handler", config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(
        LHTaskWorker(my_task, "my-task", config),
        LHTaskWorker(interrupt_handler, "interrupt-handler", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
