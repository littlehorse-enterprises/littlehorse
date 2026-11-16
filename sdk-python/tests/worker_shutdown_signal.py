"""Small subprocess fixture: actual signals and worker lifecycle, fake RPCs."""

import asyncio
import io
import signal
import sys
from unittest.mock import patch

from littlehorse.model import ReportTaskRun
from littlehorse.worker import LHTaskWorker, WorkerContext, start
from tests.test_worker_shutdown import FakeRPC, FakeServer


async def main() -> None:
    rpc = FakeRPC()
    executing = asyncio.Event()
    resource = io.StringIO()

    async def task(ctx: WorkerContext) -> str:
        executing.set()
        await asyncio.to_thread(sys.stdin.readline)
        resource.write(ctx.task_run_id.task_guid)
        return "finished"

    class ReportingServer(FakeServer):
        async def ReportTask(self, report: ReportTaskRun, timeout: float) -> None:
            assert not resource.closed
            print("REPORTING", flush=True)
            await asyncio.to_thread(sys.stdin.readline)
            await super().ReportTask(report, timeout)
            print("ACKNOWLEDGED", flush=True)

    server = ReportingServer(rpc)
    rpc.servers["one:2023"] = server
    worker = LHTaskWorker(task, "shutdown-task", rpc.config, shutdown_timeout=3.0)
    managed = sys.argv[1] == "managed"
    loop = asyncio.get_running_loop()
    application_signal_received = asyncio.Event()

    def request_shutdown() -> None:
        application_signal_received.set()
        worker.stop()

    if not managed:
        for sig in (signal.SIGTERM, signal.SIGINT):
            loop.add_signal_handler(sig, request_shutdown)
    try:
        with patch.object(rpc.config, "stub", rpc.stub):
            running = asyncio.create_task(start(worker, handle_signals=managed))
            poll = await server.polls.get()
            await poll.deliver("signal-task")
            await executing.wait()
            print("READY", flush=True)
            await running
            assert managed or application_signal_received.is_set()
            assert len(rpc.acknowledged) == 1
            print("SDK_DRAINED", flush=True)
    finally:
        resource.close()
        for sig in (signal.SIGTERM, signal.SIGINT):
            loop.remove_signal_handler(sig)
    print("RESOURCE_CLOSED", flush=True)


if __name__ == "__main__":
    asyncio.run(main())
