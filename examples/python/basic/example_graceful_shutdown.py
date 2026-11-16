"""Optional application-owned signals; example_basic keeps SDK-managed signals."""

import asyncio
import logging
import signal

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker
from example_basic import get_workflow, greeting

logger = logging.getLogger(__name__)


async def main() -> None:
    config = LHConfig()
    littlehorse.create_task_def(greeting, "greet", config)
    littlehorse.create_workflow_spec(get_workflow(), config)
    worker = LHTaskWorker(greeting, "greet", config, shutdown_timeout=20.0)
    loop = asyncio.get_running_loop()
    for sig in (signal.SIGTERM, signal.SIGINT):
        loop.add_signal_handler(sig, worker.stop)
    try:
        await littlehorse.start(worker, handle_signals=False)
        logger.info("Executions and result acknowledgements drained")
    finally:
        for sig in (signal.SIGTERM, signal.SIGINT):
            loop.remove_signal_handler(sig)


if __name__ == "__main__":
    asyncio.run(main())
