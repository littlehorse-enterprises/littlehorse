import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import ThreadBuilder, Workflow

logging.basicConfig(level=logging.INFO)

TASK_NAME = "task-with-vars"


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def thread_1(thread: ThreadBuilder) -> None:
        input_var = thread.add_variable("INPUT", VariableType.JSON_OBJ)
        thread.execute(TASK_NAME, input_var.with_json_path("$.user"))

    def my_entrypoint(thread: ThreadBuilder) -> None:
        # it receives a name
        input = thread.add_variable("my-input", VariableType.JSON_ARR)
        spawned_thread_1 = thread.spawn_thread_for_each(input, thread_1, "my-thread-1")
        thread.wait_for_threads(spawned_thread_1)
        thread.execute(TASK_NAME, "var")

    return Workflow("example-wait-for-threads-v9", my_entrypoint)


async def task(arg1: str) -> None:
    print("This is an example var " + arg1)


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
