import asyncio
import logging
from pathlib import Path
import random
from typing import Any

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker, LHWorkerContext

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


async def greeting(name: str, ctx: LHWorkerContext) -> str:
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
    await littlehorse.start(
        LHTaskWorker(greeting, "greet", config),
        LHTaskWorker(describe_car, "describe-car", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
