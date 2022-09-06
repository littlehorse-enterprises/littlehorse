import time

from lh_lib.schema.wf_run_schema import LHExecutionStatusEnum, LHFailureReasonEnum, WFRunSchema
from lh_test_harness.test_client import TestClient
from lh_sdk.thread_spec_builder import ThreadSpecBuilder
from lh_test_harness.test_utils import are_equal
from lh_test_harness.tests.shared_tasks import slow_task


def retries_task_timeout(thread: ThreadSpecBuilder):
    thread.execute(slow_task).with_timeout(1).with_retries(1)


def launch_retries_task_timeout(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_retries_task_timeout)
    print(f"Launched test {wf_run_id} on basic.py.")
    time.sleep(8)


def check_retries_task_timeout(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.HALTED, "WFRun should halt"
    task = thr.task_runs[0]
    assert task.status == LHExecutionStatusEnum.HALTED, "Taskrun shouldve failed"
    assert task.failure_reason == LHFailureReasonEnum.TIMEOUT, "should be timeout"

    assert len(thr.task_runs) == 2, "Should have retried it"

    task = thr.task_runs[1]
    assert task.status == LHExecutionStatusEnum.HALTED, "Taskrun shouldve failed"
    assert task.failure_reason == LHFailureReasonEnum.TIMEOUT, "should be timeout"
