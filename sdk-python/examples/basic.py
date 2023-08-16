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


def greeting(name: str, ctx2: LHWorkerContext) -> str:
    greeting = f"Hello {name}!"
    print(greeting)
    return greeting


config = get_config()
worker = LHTaskWorker(greeting, "greet", config)

loop = asyncio.get_event_loop()
loop.create_task(worker.start())

try:
    loop.run_forever()
except KeyboardInterrupt:
    pass
