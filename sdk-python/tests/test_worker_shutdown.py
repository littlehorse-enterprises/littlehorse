import asyncio
from collections.abc import AsyncIterator, Callable
import os
import signal
import sys
from types import SimpleNamespace
from typing import Any
import unittest
from unittest.mock import patch

from littlehorse.config import LHConfig
from littlehorse.exceptions import LHTaskException, TaskResultReportingException
from littlehorse.model import (
    PollTaskRequest,
    PollTaskResponse,
    RegisterTaskWorkerResponse,
    ReportTaskRun,
    ScheduledTask,
    TaskDef,
    TaskDefId,
    TaskRunId,
    TaskStatus,
    VariableValue,
    VarNameAndVal,
    WfRunId,
)
from littlehorse.worker import LHTaskWorker, WorkerContext, start


async def event_loop_turn() -> None:
    """Yield to ready callbacks without a wall-clock sleep."""
    loop = asyncio.get_running_loop()
    ready = loop.create_future()
    loop.call_soon(ready.set_result, None)
    await ready


class FakePoll:
    def __init__(self, requests: AsyncIterator[PollTaskRequest]) -> None:
        self.requests = requests
        self.replies: asyncio.Queue[ScheduledTask] = asyncio.Queue()
        self.delivered: asyncio.Queue[ScheduledTask] = asyncio.Queue()
        self.requested = asyncio.Event()
        self.closed = asyncio.Event()
        self.before_delivery: Callable[[], None] = lambda: None

    async def __aiter__(self) -> AsyncIterator[PollTaskResponse]:
        try:
            async for request in self.requests:
                assert request.task_def_id.name == "shutdown-task"
                self.requested.set()
                task = await self.replies.get()
                self.before_delivery()
                self.delivered.put_nowait(task)
                yield PollTaskResponse(result=task)
        finally:
            self.closed.set()
            await self.requests.aclose()

    async def deliver(self, task_guid: str) -> None:
        await self.replies.put(
            ScheduledTask(
                task_def_id=TaskDefId(name="shutdown-task"),
                task_run_id=TaskRunId(
                    task_guid=task_guid, wf_run_id=WfRunId(id="shutdown-workflow")
                ),
            )
        )
        await asyncio.wait_for(self.delivered.get(), 1)


class FakeServer:
    def __init__(self, rpc: "FakeRPC") -> None:
        self.rpc = rpc
        self.polls: asyncio.Queue[FakePoll] = asyncio.Queue()

    def PollTask(self, requests: AsyncIterator[PollTaskRequest]) -> FakePoll:
        poll = FakePoll(requests)
        self.polls.put_nowait(poll)
        return poll

    async def ReportTask(self, report: ReportTaskRun, timeout: float) -> None:
        self.rpc.attempts.append(report)
        self.rpc.reporting.put_nowait(report)
        try:
            await self.rpc.allow_ack.wait()
            if self.rpc.report_failures:
                self.rpc.report_failures -= 1
                raise ConnectionError("report unavailable")
            self.rpc.acknowledged.append(report)
        except asyncio.CancelledError:
            self.rpc.report_cancelled.set()
            raise


class FakeRPC:
    def __init__(self) -> None:
        self.config = LHConfig()
        self.config.load({"LHW_NUM_WORKER_THREADS": "1"})
        self.servers = {
            "one:2023": FakeServer(self),
            "two:2023": FakeServer(self),
        }
        self.hosts = ["one"]
        self.registered: asyncio.Queue[None] = asyncio.Queue()
        self.reporting: asyncio.Queue[ReportTaskRun] = asyncio.Queue()
        self.attempts: list[ReportTaskRun] = []
        self.acknowledged: list[ReportTaskRun] = []
        self.allow_ack = asyncio.Event()
        self.allow_ack.set()
        self.report_cancelled = asyncio.Event()
        self.report_failures = 0
        self.task_def = TaskDef(id=TaskDefId(name="shutdown-task"))

    def stub(self, server: str | None = None, **kwargs: Any) -> Any:
        return self.servers[server] if server else self

    def GetTaskDef(self, request: TaskDefId, timeout: float) -> TaskDef:
        return self.task_def

    async def RegisterTaskWorker(
        self, request: Any, timeout: float
    ) -> RegisterTaskWorkerResponse:
        self.registered.put_nowait(None)
        return RegisterTaskWorkerResponse(
            your_hosts=[{"host": host, "port": 2023} for host in self.hosts]
        )


class TestWorkerShutdown(unittest.IsolatedAsyncioTestCase):
    async def asyncSetUp(self) -> None:
        self.rpc = FakeRPC()
        self.workers: list[LHTaskWorker] = []
        self.runs: list[asyncio.Task[None]] = []
        self.release_execution = asyncio.Event()
        self.executing: asyncio.Queue[str] = asyncio.Queue()
        self.execution_cancelled = asyncio.Event()
        self.heartbeat_idle = asyncio.Event()
        self.heartbeat_ticks: asyncio.Queue[None] = asyncio.Queue()
        self.retry_waiting = asyncio.Event()
        self.allow_retry = asyncio.Event()
        self.allow_retry.set()

        async def controlled_sleep(delay: float) -> None:
            if delay == 60:
                self.heartbeat_idle.set()
                await self.heartbeat_ticks.get()
            elif delay == 2:
                self.retry_waiting.set()
                await self.allow_retry.wait()
            else:
                raise AssertionError(f"Unexpected SDK sleep: {delay}")

        for patcher in (
            patch("littlehorse.worker.asyncio.sleep", controlled_sleep),
            patch.object(self.rpc.config, "stub", self.rpc.stub),
            patch("littlehorse.worker.shutdown_hook"),
        ):
            patcher.start()
            self.addCleanup(patcher.stop)

    async def asyncTearDown(self) -> None:
        self.release_execution.set()
        self.rpc.allow_ack.set()
        self.allow_retry.set()
        self.stop_workers()
        for run in self.runs:
            if not run.done():
                run.cancel()
        await asyncio.gather(*self.runs, return_exceptions=True)

    async def task(self, ctx: WorkerContext) -> str:
        self.executing.put_nowait(ctx.task_run_id.task_guid)
        try:
            await self.release_execution.wait()
        except asyncio.CancelledError:
            self.execution_cancelled.set()
            raise
        return "finished"

    async def launch(
        self,
        task: Callable[..., Any] | None = None,
        *,
        hosts: list[str] | None = None,
        **kwargs: Any,
    ) -> tuple[LHTaskWorker, asyncio.Task[None], list[FakePoll]]:
        if hosts is not None:
            self.rpc.hosts = hosts
        worker = LHTaskWorker(
            task or self.task, "shutdown-task", self.rpc.config, **kwargs
        )
        self.workers.append(worker)
        run = asyncio.create_task(start(worker))
        self.runs.append(run)
        polls = [
            await asyncio.wait_for(self.rpc.servers[f"{host}:2023"].polls.get(), 1)
            for host in self.rpc.hosts
        ]
        for poll in polls:
            await asyncio.wait_for(poll.requested.wait(), 1)
        return worker, run, polls

    def stop_workers(self) -> None:
        for worker in self.workers:
            worker.stop()
            # Release the controlled heartbeat boundary even on the old SDK.
            self.heartbeat_ticks.put_nowait(None)

    async def finish(self, run: asyncio.Task[None]) -> None:
        await asyncio.wait_for(asyncio.shield(run), 1)

    async def test_idle_shutdown_wakes_heartbeat_and_polling(self) -> None:
        worker, run, polls = await self.launch()
        await self.heartbeat_idle.wait()
        with self.assertLogs("LHTaskWorker", level="INFO") as logs:
            worker.stop()
        self.assertIn("waiting up to 75.0 seconds for 0 accepted tasks", logs.output[0])
        await self.finish(run)
        self.assertTrue(polls[0].closed.is_set())

    async def test_shutdown_log_counts_queued_running_and_retired_reporting_work(
        self,
    ) -> None:
        async def mixed_task(ctx: WorkerContext) -> str:
            if ctx.task_run_id.task_guid == "reporting":
                return "finished"
            return await self.task(ctx)

        self.rpc.allow_ack.clear()
        worker, run, polls = await self.launch(
            mixed_task, hosts=["one", "two"], shutdown_timeout=90.0
        )
        await polls[0].deliver("reporting")
        await self.rpc.reporting.get()
        await polls[0].deliver("running")
        await self.executing.get()
        await polls[0].deliver("queued")
        self.rpc.hosts = ["two"]
        self.heartbeat_ticks.put_nowait(None)
        await polls[0].closed.wait()
        self.assertNotIn("one:2023", worker._connections)
        await polls[1].deliver("current")
        await self.executing.get()
        with self.assertLogs("LHTaskWorker", level="INFO") as logs:
            worker.stop()
            worker.stop()
        self.assertEqual(len(logs.records), 1)
        self.assertIn("waiting up to 90.0 seconds for 4 accepted tasks", logs.output[0])
        self.assertEqual(logs.records[0].shutdown_timeout, 90.0)
        self.assertEqual(logs.records[0].pending_tasks, 4)
        self.release_execution.set()
        self.rpc.allow_ack.set()
        await self.finish(run)
        self.assertEqual(len(self.rpc.acknowledged), 4)

    async def test_shutdown_during_execution_waits_without_cancelling(self) -> None:
        worker, run, polls = await self.launch()
        await polls[0].deliver("executing")
        self.assertEqual(await self.executing.get(), "executing")
        self.stop_workers()
        await event_loop_turn()
        await event_loop_turn()
        self.assertFalse(run.done(), "start() returned before execution finished")
        self.assertFalse(self.execution_cancelled.is_set())
        self.release_execution.set()
        await self.finish(run)
        self.assertEqual(len(self.rpc.acknowledged), 1)

    async def test_shutdown_waits_for_result_acknowledgement(self) -> None:
        self.rpc.allow_ack.clear()
        self.release_execution.set()
        worker, run, polls = await self.launch()
        await polls[0].deliver("reporting")
        await self.rpc.reporting.get()
        self.stop_workers()
        await event_loop_turn()
        await event_loop_turn()
        self.assertFalse(run.done(), "start() returned with an unacknowledged result")
        self.rpc.allow_ack.set()
        await self.finish(run)
        self.assertEqual(self.rpc.acknowledged[0].output.str, "finished")

    async def test_accepted_work_waiting_for_execution_capacity_is_drained(
        self,
    ) -> None:
        worker, run, polls = await self.launch()
        await polls[0].deliver("first")
        await self.executing.get()
        await polls[0].deliver("queued")
        self.stop_workers()
        await event_loop_turn()
        self.assertFalse(run.done())
        self.release_execution.set()
        await self.finish(run)
        self.assertEqual(
            {report.task_run_id.task_guid for report in self.rpc.acknowledged},
            {"first", "queued"},
        )

    async def test_reply_delivered_after_stop_is_not_admitted(self) -> None:
        worker, run, polls = await self.launch()
        polls[0].before_delivery = worker.stop
        await polls[0].deliver("too-late")
        self.heartbeat_ticks.put_nowait(None)
        await self.finish(run)
        self.assertTrue(self.executing.empty())
        self.assertEqual(self.rpc.attempts, [])

    async def test_repeated_stop_does_not_cancel_execution_or_reporting(self) -> None:
        self.rpc.allow_ack.clear()
        worker, run, polls = await self.launch()
        await polls[0].deliver("repeated")
        await self.executing.get()
        for _ in range(3):
            self.stop_workers()
        self.release_execution.set()
        await self.rpc.reporting.get()
        for _ in range(3):
            self.stop_workers()
        await event_loop_turn()
        self.assertFalse(run.done())
        self.assertFalse(self.rpc.report_cancelled.is_set())
        self.rpc.allow_ack.set()
        await self.finish(run)
        self.assertEqual(len(self.rpc.acknowledged), 1)

    async def test_result_reporting_retries_are_drained(self) -> None:
        self.release_execution.set()
        self.rpc.report_failures = 1
        self.allow_retry.clear()
        worker, run, polls = await self.launch()
        await polls[0].deliver("retry")
        await self.retry_waiting.wait()
        self.stop_workers()
        await event_loop_turn()
        await event_loop_turn()
        self.assertFalse(run.done(), "retry wait escaped draining")
        self.allow_retry.set()
        await self.finish(run)
        self.assertEqual(len(self.rpc.attempts), 2)
        self.assertEqual(len(self.rpc.acknowledged), 1)

    async def test_exhausted_reporting_retries_are_not_graceful_success(self) -> None:
        self.release_execution.set()
        self.rpc.report_failures = 100
        worker, run, polls = await self.launch()
        await polls[0].deliver("unreportable")
        await self.rpc.reporting.get()
        self.stop_workers()
        with self.assertRaisesRegex(TaskResultReportingException, "[Rr]eport"):
            await self.finish(run)
        self.assertEqual(len(self.rpc.attempts), 15)
        self.assertEqual(self.rpc.acknowledged, [])

    async def test_deadline_cancels_execution_and_raises_timeout(self) -> None:
        worker, run, polls = await self.launch(shutdown_timeout=0.01)
        await polls[0].deliver("timeout")
        await self.executing.get()
        self.stop_workers()
        with self.assertRaises(TimeoutError):
            await self.finish(run)
        self.assertTrue(self.execution_cancelled.is_set())
        self.assertEqual(self.rpc.attempts, [])
        self.assertTrue(polls[0].closed.is_set())

    async def test_deadline_cancels_pending_report(self) -> None:
        self.rpc.allow_ack.clear()
        self.release_execution.set()
        worker, run, polls = await self.launch(shutdown_timeout=0.01)
        await polls[0].deliver("timeout-report")
        await self.rpc.reporting.get()
        self.stop_workers()
        with self.assertRaises(TimeoutError):
            await self.finish(run)
        self.assertTrue(self.rpc.report_cancelled.is_set())
        self.assertEqual(self.rpc.acknowledged, [])

    async def test_cancelling_start_propagates_and_joins_owned_work(self) -> None:
        worker, run, polls = await self.launch()
        await polls[0].deliver("cancelled")
        await self.executing.get()
        run.cancel()
        with self.assertRaises(asyncio.CancelledError):
            await run
        self.assertTrue(self.execution_cancelled.is_set())
        self.assertTrue(polls[0].closed.is_set())
        self.assertEqual(self.rpc.attempts, [])

    async def test_reporting_does_not_hold_execution_capacity(self) -> None:
        self.rpc.allow_ack.clear()
        self.release_execution.set()
        worker, run, polls = await self.launch()
        await polls[0].deliver("first")
        await self.rpc.reporting.get()
        await polls[0].deliver("second")
        second = await asyncio.wait_for(self.rpc.reporting.get(), 1)
        self.assertEqual(second.task_run_id.task_guid, "second")
        self.stop_workers()
        self.rpc.allow_ack.set()
        await self.finish(run)
        self.assertEqual(len(self.rpc.acknowledged), 2)

    async def test_multiple_workers_and_connections_are_all_drained(self) -> None:
        self.rpc.allow_ack.clear()
        self.release_execution.set()
        _, first, first_polls = await self.launch(hosts=["one", "two"])
        _, second, second_polls = await self.launch()
        for index, poll in enumerate(first_polls + second_polls):
            await poll.deliver(str(index))
            await self.rpc.reporting.get()
        self.stop_workers()
        await event_loop_turn()
        await event_loop_turn()
        self.assertFalse(first.done())
        self.assertFalse(second.done())
        self.rpc.allow_ack.set()
        await self.finish(first)
        await self.finish(second)
        self.assertEqual(len(self.rpc.acknowledged), 4)

    async def test_removed_connection_keeps_ownership_of_pending_report(self) -> None:
        self.rpc.allow_ack.clear()
        self.release_execution.set()
        worker, run, polls = await self.launch()
        await polls[0].deliver("retired")
        await self.rpc.reporting.get()
        self.rpc.hosts = ["two"]
        self.heartbeat_ticks.put_nowait(None)
        new_poll = await asyncio.wait_for(self.rpc.servers["two:2023"].polls.get(), 1)
        await new_poll.requested.wait()
        self.assertNotIn("one:2023", worker._connections)
        self.stop_workers()
        await event_loop_turn()
        await event_loop_turn()
        self.assertFalse(run.done(), "retired connection's report was forgotten")
        self.rpc.allow_ack.set()
        await self.finish(run)
        self.assertEqual(len(self.rpc.acknowledged), 1)
        self.assertTrue(polls[0].closed.is_set())

    async def test_completed_work_is_retired_from_tracking(self) -> None:
        self.release_execution.set()
        worker, run, polls = await self.launch()
        connection = worker._connections["one:2023"]
        for index in range(50):
            await polls[0].deliver(str(index))
            await self.rpc.reporting.get()
            await event_loop_turn()
        await event_loop_turn()
        self.assertEqual(len(self.rpc.acknowledged), 50)
        self.assertEqual(len(connection._tasks), 0)
        self.stop_workers()
        await self.finish(run)
        self.assertEqual(len(worker._connection_tasks), 0)

    async def test_task_failure_serialization_is_preserved(self) -> None:
        async def failing() -> None:
            raise LHTaskException(
                "business-error", "expected", VariableValue(str="data")
            )

        worker, run, polls = await self.launch(failing)
        await polls[0].deliver("failed")
        await self.rpc.reporting.get()
        self.stop_workers()
        await self.finish(run)
        report = self.rpc.acknowledged[0]
        self.assertEqual(report.status, TaskStatus.TASK_EXCEPTION)
        self.assertEqual(report.exception.name, "business-error")
        self.assertEqual(report.exception.message, "expected")
        self.assertEqual(report.exception.content.str, "data")

    async def test_deserialization_failure_releases_capacity_and_is_reported(
        self,
    ) -> None:
        self.release_execution.set()
        worker, run, polls = await self.launch()
        malformed = ScheduledTask(
            task_def_id=TaskDefId(name="shutdown-task"),
            task_run_id=TaskRunId(task_guid="malformed"),
            variables=[
                VarNameAndVal(var_name="bad", value=VariableValue(json_obj="{"))
            ],
        )
        await polls[0].replies.put(malformed)
        await polls[0].delivered.get()
        first = await asyncio.wait_for(self.rpc.reporting.get(), 1)
        self.assertEqual(first.status, TaskStatus.TASK_FAILED)
        await polls[0].deliver("valid")
        second = await asyncio.wait_for(self.rpc.reporting.get(), 1)
        self.assertEqual(second.status, TaskStatus.TASK_SUCCESS)
        self.stop_workers()
        await self.finish(run)

    async def test_stop_before_start_is_safe_and_does_not_poll(self) -> None:
        worker = LHTaskWorker(self.task, "shutdown-task", self.rpc.config)
        self.workers.append(worker)
        worker.stop()
        worker.stop()
        await asyncio.wait_for(worker.start(), 1)
        self.assertTrue(self.rpc.servers["one:2023"].polls.empty())

    async def test_disabling_sdk_signals_leaves_application_handlers_untouched(
        self,
    ) -> None:
        worker = LHTaskWorker(self.task, "shutdown-task", self.rpc.config)
        self.workers.append(worker)
        loop = asyncio.get_running_loop()
        original = signal.getsignal(signal.SIGTERM)
        with (
            patch.object(loop, "add_signal_handler") as add_handler,
            patch("littlehorse.worker.signal.signal") as set_handler,
            patch("littlehorse.worker.shutdown_hook") as install_hooks,
        ):
            worker.stop()
            await start(worker, handle_signals=False)
        install_hooks.assert_not_called()
        add_handler.assert_not_called()
        set_handler.assert_not_called()
        self.assertEqual(signal.getsignal(signal.SIGTERM), original)

    async def test_unrequested_heartbeat_cancellation_is_not_success(self) -> None:
        async def cancelled_registration(request: Any, timeout: float) -> None:
            raise asyncio.CancelledError

        worker = LHTaskWorker(self.task, "shutdown-task", self.rpc.config)
        self.workers.append(worker)
        with patch.object(self.rpc, "RegisterTaskWorker", cancelled_registration):
            with self.assertRaises(asyncio.CancelledError):
                await worker.start()

    async def test_completion_after_deadline_is_not_success(self) -> None:
        clock = [100.0]
        self.rpc.allow_ack.clear()
        self.release_execution.set()
        worker, run, polls = await self.launch()
        await polls[0].deliver("late-ack")
        await self.rpc.reporting.get()
        draining = asyncio.Event()
        original_drain = worker._drain_connections

        async def drain() -> None:
            draining.set()
            await original_drain()

        with (
            patch(
                "littlehorse.worker.time", SimpleNamespace(monotonic=lambda: clock[0])
            ),
            patch.object(worker, "_drain_connections", drain),
        ):
            self.stop_workers()
            await draining.wait()
            clock[0] = 176.0
            self.rpc.allow_ack.set()
            with self.assertRaises(TimeoutError):
                await self.finish(run)

    async def test_default_drain_budget_allows_task_timeout_plus_reporting(
        self,
    ) -> None:
        clock = [100.0]
        self.rpc.allow_ack.clear()
        worker, run, polls = await self.launch()
        await polls[0].deliver("full-duration")
        await self.executing.get()
        draining = asyncio.Event()
        original_drain = worker._drain_connections

        async def drain() -> None:
            draining.set()
            await original_drain()

        with (
            patch(
                "littlehorse.worker.time", SimpleNamespace(monotonic=lambda: clock[0])
            ),
            patch.object(worker, "_drain_connections", drain),
        ):
            worker.stop()
            await draining.wait()
            clock[0] = 160.0
            self.release_execution.set()
            await self.rpc.reporting.get()
            clock[0] = 174.0
            self.rpc.allow_ack.set()
            await self.finish(run)
        self.assertEqual(len(self.rpc.acknowledged), 1)

    async def test_worker_failure_does_not_abandon_a_sibling_execution(self) -> None:
        async def fast() -> str:
            return "fast"

        failing = LHTaskWorker(fast, "shutdown-task", self.rpc.config)
        sibling = LHTaskWorker(self.task, "shutdown-task", self.rpc.config)
        self.workers.extend([failing, sibling])
        run = asyncio.create_task(start(failing, sibling))
        self.runs.append(run)
        first_poll = await self.rpc.servers["one:2023"].polls.get()
        second_poll = await self.rpc.servers["one:2023"].polls.get()
        await second_poll.deliver("sibling")
        await self.executing.get()
        self.rpc.report_failures = 15
        await first_poll.deliver("unreportable")
        await self.rpc.reporting.get()
        failing.stop()
        await second_poll.closed.wait()
        self.assertFalse(run.done())
        self.assertFalse(self.execution_cancelled.is_set())
        self.release_execution.set()
        with self.assertRaises(TaskResultReportingException):
            await self.finish(run)
        self.assertEqual(
            [report.task_run_id.task_guid for report in self.rpc.acknowledged],
            ["sibling"],
        )

    async def test_stop_interrupts_pending_registration(self) -> None:
        registering = asyncio.Event()

        async def blocked_registration(request: Any, timeout: float) -> None:
            registering.set()
            await asyncio.Event().wait()

        worker = LHTaskWorker(self.task, "shutdown-task", self.rpc.config)
        self.workers.append(worker)
        with patch.object(self.rpc, "RegisterTaskWorker", blocked_registration):
            run = asyncio.create_task(worker.start())
            self.runs.append(run)
            await registering.wait()
            worker.stop()
            await self.finish(run)
        self.assertTrue(self.rpc.servers["one:2023"].polls.empty())

    async def test_retired_completed_connections_are_not_retained(self) -> None:
        worker, run, polls = await self.launch()
        for host in ["two", "one", "two", "one"]:
            retiring = list(worker._connection_tasks)
            self.rpc.hosts = [host]
            self.heartbeat_ticks.put_nowait(None)
            poll = await asyncio.wait_for(
                self.rpc.servers[f"{host}:2023"].polls.get(), 1
            )
            await poll.requested.wait()
            await asyncio.gather(*retiring, return_exceptions=True)
            await event_loop_turn()
            self.assertLessEqual(len(worker._connection_tasks), 1)
        self.stop_workers()
        await self.finish(run)
        self.assertEqual(len(worker._connection_tasks), 0)

    async def test_invalid_shutdown_deadline_is_rejected(self) -> None:
        for timeout in [0.0, -1.0, float("inf"), float("nan")]:
            with self.subTest(timeout=timeout):
                with self.assertRaises(ValueError):
                    LHTaskWorker(
                        self.task,
                        "shutdown-task",
                        self.rpc.config,
                        shutdown_timeout=timeout,
                    )

    async def test_shutdown_does_not_cancel_unrelated_application_tasks(self) -> None:
        application_task = asyncio.create_task(asyncio.Event().wait())
        try:
            worker, run, polls = await self.launch()
            self.stop_workers()
            await self.finish(run)
            self.assertFalse(application_task.done())
        finally:
            application_task.cancel()
            await asyncio.gather(application_task, return_exceptions=True)

    async def test_task_cancellation_releases_capacity_but_is_not_graceful(
        self,
    ) -> None:
        async def cancelled_once(ctx: WorkerContext) -> str:
            if ctx.task_run_id.task_guid == "cancel":
                raise asyncio.CancelledError
            return "finished"

        worker, run, polls = await self.launch(cancelled_once)
        await polls[0].deliver("cancel")
        await polls[0].deliver("following")
        report = await asyncio.wait_for(self.rpc.reporting.get(), 1)
        self.assertEqual(report.task_run_id.task_guid, "following")
        self.stop_workers()
        with self.assertRaises(asyncio.CancelledError):
            await run
        self.assertEqual(len(self.rpc.acknowledged), 1)

    async def test_cancellation_waits_for_execution_cleanup(self) -> None:
        cleaning = asyncio.Event()
        allow_cleanup = asyncio.Event()
        cleaned = asyncio.Event()

        async def task_with_cleanup() -> None:
            self.executing.put_nowait("cleanup")
            try:
                await asyncio.Event().wait()
            finally:
                cleaning.set()
                await allow_cleanup.wait()
                cleaned.set()

        worker, run, polls = await self.launch(task_with_cleanup)
        try:
            await polls[0].deliver("cleanup")
            await self.executing.get()
            run.cancel()
            await cleaning.wait()
            self.assertFalse(run.done())
            allow_cleanup.set()
            with self.assertRaises(asyncio.CancelledError):
                await run
            self.assertTrue(cleaned.is_set())
        finally:
            allow_cleanup.set()


@unittest.skipIf(os.name == "nt", "POSIX process signals")
class TestShutdownSignals(unittest.IsolatedAsyncioTestCase):
    async def check_signal(self, managed: bool) -> None:
        process = await asyncio.create_subprocess_exec(
            sys.executable,
            "-m",
            "tests.worker_shutdown_signal",
            "managed" if managed else "application",
            stdin=asyncio.subprocess.PIPE,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE,
        )
        try:
            self.assertEqual(
                await asyncio.wait_for(process.stdout.readline(), 5), b"READY\n"
            )
            process.send_signal(signal.SIGTERM)
            process.stdin.write(b"finish execution\n")
            await process.stdin.drain()
            self.assertEqual(
                await asyncio.wait_for(process.stdout.readline(), 5), b"REPORTING\n"
            )
            process.stdin.write(b"acknowledge result\n")
            await process.stdin.drain()
            stdout, stderr = await asyncio.wait_for(process.communicate(), 5)
            self.assertEqual(process.returncode, 0, stderr.decode())
            self.assertEqual(stdout, b"ACKNOWLEDGED\nSDK_DRAINED\nRESOURCE_CLOSED\n")
        finally:
            if process.returncode is None:
                process.kill()
                await process.communicate()

    async def test_application_owned_sigterm(self) -> None:
        await self.check_signal(managed=False)

    async def test_sdk_managed_sigterm(self) -> None:
        await self.check_signal(managed=True)


if __name__ == "__main__":
    unittest.main()
