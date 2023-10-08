import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_interrupt_handler(handler: WorkflowThread) -> None:
        handler.execute("some-task")

    def my_entrypoint(wf: WorkflowThread) -> None:
        wf.add_interrupt_handler("interruption-event", my_interrupt_handler)
        wf.sleep(30)
        wf.execute("my-task")

    return Workflow("example-interrupt-handler", my_entrypoint)


async def some_task() -> None:
    print("Executing some task")
    raise Exception("Workflow execution stopped")


async def my_task() -> str:
    msg = "Hello, there!"
    print(msg)
    return msg


async def main() -> None:
    config = get_config()

    littlehorse.create_external_event_def("interruption-event", config)
    littlehorse.create_task_def(my_task, "my-task", config)
    littlehorse.create_task_def(some_task, "some-task", config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(
        LHTaskWorker(my_task, "my-task", config),
        LHTaskWorker(some_task, "some-task", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
