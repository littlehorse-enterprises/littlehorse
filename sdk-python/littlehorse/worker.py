import asyncio
from datetime import datetime
import logging
from typing import Any, Callable, Optional
from littlehorse.config import LHConfig
from littlehorse.model.service_pb2 import (
    RegisterTaskWorkerPb,
)
from littlehorse.model.service_pb2_grpc import LHPublicApiStub


class LHTaskWorker:
    _log = logging.getLogger("LHTaskWorker")

    def __init__(
        self, callable: Callable[..., Any], task_def_name: str, config: LHConfig
    ) -> None:
        self.executable = callable
        self.task_def_name = task_def_name
        self.config = config
        self.running = False

        self._heartbeat_task: Optional[asyncio.Task[None]] = None

    async def _heartbeat(self) -> None:
        async with self.config.establish_channel(async_channel=True) as channel:
            stub = LHPublicApiStub(channel)
            while self.running:
                self._log.debug("Sending heart beat at %s", datetime.now())

                request = RegisterTaskWorkerPb(
                    client_id=self.config.client_id(),
                    listener_name=self.config.server_listener(),
                    task_def_name=self.task_def_name,
                )
                await stub.RegisterTaskWorker(request)
                # HERE: CHECK THE OPENED CONNECTIONS,
                # DEPENDING ON "YOUR HOSTS" KEEP THEM OR KILL THEM
                await asyncio.sleep(5)

    async def start(self) -> None:
        self.running = True
        self._heartbeat_task = asyncio.create_task(self._heartbeat())

    async def stop(self) -> None:
        self._log.debug("Stopping worker")

        if self._heartbeat_task is None or not self.running:
            return

        self._log.debug("Cancelling heartbeat task")
        self._heartbeat_task.cancel()
        self.running = False


if __name__ == "__main__":
    from pathlib import Path

    logging.basicConfig(level=logging.DEBUG)

    config_path = Path.home().joinpath(".config", "littlehorse.config")

    def my_callable() -> None:
        print("Hello world!")

    config = LHConfig()
    config.load(config_path)
    worker = LHTaskWorker(my_callable, "greet", config)

    loop = asyncio.get_event_loop()

    async def wait_until_stop() -> None:  # this is not needed in production
        await asyncio.sleep(60)
        await worker.stop()
        loop.stop()

    try:
        loop.create_task(worker.start())
        loop.create_task(wait_until_stop())  # this is not needed in production
        loop.run_forever()
    except KeyboardInterrupt:
        pass
