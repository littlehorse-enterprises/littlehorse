import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import ThreadBuilder, Workflow

logging.basicConfig(level=logging.INFO)

TASK_NAME = "task-1"


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def thread_1(thread: ThreadBuilder) -> None:
        thread.execute(TASK_NAME)

    def my_entrypoint(thread: ThreadBuilder) -> None:
        # it receives a name
        input = thread.add_variable("my-input", VariableType.JSON_ARR)
        vars: dict[str, str] = {}
        vars["var1"] = "mycontent"
        spawned_thread_1 = thread.spawn_thread_for_each(
            input, thread_1, "my-thread-1", vars
        )
        thread.wait_for_threads(spawned_thread_1)
        thread.execute(TASK_NAME)

    return Workflow("example-wait-for-threads-v4", my_entrypoint)


async def task() -> None:
    print("This is an example")


async def daily_bugle(news: str) -> None:
    print(news)


async def main() -> None:
    config = get_config()
    wf = get_workflow()

    littlehorse.create_task_def(task, TASK_NAME, config)
    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(
        LHTaskWorker(task, TASK_NAME, config),
    )


if __name__ == "__main__":
    asyncio.run(main())
