import asyncio
import logging
from pathlib import Path
import random

from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker, LHWorkerContext

logging.basicConfig(level=logging.DEBUG)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


async def greeting(name: str, ctx: LHWorkerContext) -> str:
    greeting = f"Hello {name}!"
    print(greeting)
    await asyncio.sleep(random.uniform(2.0, 5.0))
    return greeting


async def main() -> None:
    worker = LHTaskWorker(greeting, "greet", get_config())
    await worker.start()


if __name__ == "__main__":
    asyncio.run(main())
