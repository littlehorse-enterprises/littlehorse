import asyncio
import logging
from pathlib import Path
import random
from typing import Any
import littlehorse

from littlehorse.config import LHConfig
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import ThreadBuilder, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_entrypoint(thread: ThreadBuilder) -> None:
        person = thread.add_variable("person", VariableType.JSON_OBJ)
        thread.execute("greet", person.with_json_path("$.name"))
        thread.execute("describe-car", person.with_json_path("$.car"))

    return Workflow("example-json", my_entrypoint)


async def greeting(name: str, ctx: WorkerContext) -> str:
    msg = f"Hello {name}!. WfRun {ctx.wf_run_id}"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg


async def describe_car(car: dict[str, Any]) -> str:
    msg = f"You drive a {car['brand']} model {car['model']}"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg


async def main() -> None:
    config = get_config()
    wf = get_workflow()

    littlehorse.register_task(greeting, "greet", config)
    littlehorse.register_task(describe_car, "describe-car", config)
    littlehorse.register_workflow(wf, config)

    await littlehorse.start(
        LHTaskWorker(greeting, "greet", config),
        LHTaskWorker(describe_car, "describe-car", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
