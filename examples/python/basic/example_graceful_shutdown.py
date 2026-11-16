"""Optional application-owned signals; example_basic keeps SDK-managed signals."""

import asyncio
import logging
from pathlib import Path
import signal

import littlehorse
from littlehorse.worker import LHTaskWorker, WorkerContext
from example_basic import get_config, get_workflow, greeting

logger = logging.getLogger(__name__)


async def main() -> None:
    config = get_config()
    # This application resource must remain open through task execution/draining.
    with Path("greetings.log").open("a") as application_log:

        async def logged_greeting(name: str, ctx: WorkerContext) -> str:
            result = await greeting(name, ctx)
            application_log.write(result + "\n")
            application_log.flush()
            return result

        littlehorse.create_task_def(logged_greeting, "greet", config)
        littlehorse.create_workflow_spec(get_workflow(), config)
        worker = LHTaskWorker(logged_greeting, "greet", config, shutdown_timeout=20.0)
        loop = asyncio.get_running_loop()
        for sig in (signal.SIGTERM, signal.SIGINT):
            loop.add_signal_handler(sig, worker.stop)
        try:
            await littlehorse.start(worker, handle_signals=False)
            logger.info("Executions and result acknowledgements drained")
        finally:
            for sig in (signal.SIGTERM, signal.SIGINT):
                loop.remove_signal_handler(sig)
    # Errors propagate after SDK cleanup; the application log is now closed.


if __name__ == "__main__":
    asyncio.run(main())
