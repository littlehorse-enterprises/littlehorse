import asyncio
import logging
from pathlib import Path

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
    return greeting


async def stop_after() -> None:
    await asyncio.sleep(60 * 5)
    await worker.stop()


async def main() -> None:
    start_task = asyncio.create_task(worker.start())
    # stop_task = asyncio.create_task(stop_after())
    await start_task
    # await stop_task


if __name__ == "__main__":
    config = get_config()
    worker = LHTaskWorker(greeting, "greet", config)
    asyncio.run(main())
