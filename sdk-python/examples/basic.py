import asyncio
import logging
from pathlib import Path
import random
from typing import Any

from littlehorse.config import LHConfig
from littlehorse.utils import start_workers
from littlehorse.worker import LHTaskWorker, LHWorkerContext

logging.basicConfig(level=logging.DEBUG)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


async def greeting(name: str, ctx: LHWorkerContext) -> str:
    greeting = f"Hello {name}!. Context {ctx}"
    print(greeting)
    await asyncio.sleep(random.uniform(0.5, 2.0))
    return greeting


async def describe_car(car: dict[str, Any], ctx: LHWorkerContext) -> None:
    print(f"You drive a {car['brand']} model {car['model']}")


async def main() -> None:
    config = get_config()
    greet_worker = LHTaskWorker(greeting, "greet", config)
    car_worker = LHTaskWorker(describe_car, "describe-car", config)
    await start_workers(greet_worker, car_worker)


if __name__ == "__main__":
    asyncio.run(main())
