import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import LHErrorType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def entrypoint(wf: WorkflowThread) -> None:
    def exception_handler(handler: WorkflowThread) -> None:
        handler.execute("handler")

    node = wf.execute("fail")
    wf.handle_error(node, exception_handler, LHErrorType.TASK_ERROR)


async def fail() -> None:
    raise Exception("Yikes")


async def handler() -> None:
    print("hi from handler")


def get_workflow() -> Workflow:
    return Workflow("example-error-handling", entrypoint)


async def main() -> None:
    config = get_config()

    littlehorse.create_task_def(fail, "fail", config)
    littlehorse.create_task_def(handler, "handler", config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(
        LHTaskWorker(fail, "fail", config),
        LHTaskWorker(handler, "handler", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
