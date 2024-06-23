import asyncio
import logging
from datetime import datetime
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, ExponentialBackoffRetryPolicy

from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import WorkflowThread, Workflow

TIME_FORMAT = "%H:%M:%S"

INPUT_NAME = "start-timestamp"
WF_NAME = "example-retries"
TASK_NAME = "retries"

RETRY_POLICY = ExponentialBackoffRetryPolicy(
    base_interval_ms=2000, multiplier=2, max_delay_ms=5000
)
MAX_RETRIES = 5

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        wf.execute(
            TASK_NAME,
            wf.add_variable(INPUT_NAME, VariableType.INT),
            retries=MAX_RETRIES,
        )

    return Workflow(WF_NAME, my_entrypoint).with_retry_policy(
        exponential_backoff=RETRY_POLICY
    )


async def retry_task(start_timestamp: int, ctx: WorkerContext) -> None:
    execution_time = datetime.now()
    schedule_time = datetime.fromtimestamp(start_timestamp)
    msg = (
        f"\nWfRun: {ctx.wf_run_id.id}\n"
        f"Attempt: {ctx.attempt_number} {'(Retry)' if ctx.attempt_number > 0 else ''}\n"
        f"Wf Schedule Time: {schedule_time.strftime(TIME_FORMAT)}\n"
        f"Execution Time: {execution_time.strftime(TIME_FORMAT)}\n"
        f"Delay: {int((execution_time - schedule_time).total_seconds())}s"
    )
    print(msg)
    if ctx.attempt_number < MAX_RETRIES:
        raise Exception(f"This attempt fails. Attempt: {ctx.attempt_number}")


async def main() -> None:
    config = get_config()
    wf = get_workflow()
    worker = LHTaskWorker(retry_task, TASK_NAME, config)

    littlehorse.create_task_def(retry_task, TASK_NAME, config)
    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(worker)


if __name__ == "__main__":
    asyncio.run(main())
