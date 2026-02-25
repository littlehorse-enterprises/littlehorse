import asyncio
import logging
import random
import littlehorse
from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker

from examples.python.casting.workflow import get_workflow

logging.basicConfig(level=logging.INFO)


async def string_method(value: str) -> str:
    logging.info(f"Executing string-method with value: {value}")
    await asyncio.sleep(random.uniform(0.1, 0.3))
    return value


async def int_method(value: int) -> int:
    result = value * 2
    logging.info(f"Executing int-method with value: {value}, doubling to: {result}")
    await asyncio.sleep(random.uniform(0.1, 0.3))
    return result


async def double_method(value: float) -> float:
    result = value * 0.9
    logging.info(f"Executing double-method with value: {value}, reducing to: {result}")
    await asyncio.sleep(random.uniform(0.1, 0.3))
    return result


async def bool_method(value: bool) -> bool:
    result = not value
    logging.info(f"Executing bool-method with value: {value}, toggling to: {result}")
    await asyncio.sleep(random.uniform(0.1, 0.3))
    return result


async def main() -> None:
    cfg = LHConfig()
    wf = get_workflow()

    littlehorse.create_task_def(string_method, "string-method", cfg)
    littlehorse.create_task_def(int_method, "int-method", cfg)
    littlehorse.create_task_def(double_method, "double-method", cfg)
    littlehorse.create_task_def(bool_method, "bool-method", cfg)

    littlehorse.create_workflow_spec(wf, cfg)

    await littlehorse.start(
        LHTaskWorker(string_method, "string-method", cfg),
        LHTaskWorker(int_method, "int-method", cfg),
        LHTaskWorker(double_method, "double-method", cfg),
        LHTaskWorker(bool_method, "bool-method", cfg),
    )


if __name__ == "__main__":
    asyncio.run(main())
