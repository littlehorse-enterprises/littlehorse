from lh_lib.schema.wf_run_schema import LHExecutionStatusEnum, WFRunSchema
from lh_test_harness.test_client import TestClient
from lh_sdk.thread_spec_builder import ThreadSpecBuilder
from lh_test_harness.test_utils import are_equal
from lh_test_harness.tests.shared_tasks import echo_task


HELLO = "Hello, there!"

# This is the workflow function that we test.
def basic(thread: ThreadSpecBuilder):
    thread.execute(echo_task, HELLO)


# The "input" for test case 1. It's simple; we just launch the workflow.
def launch_basic_1(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_basic_1)
    print(f"Launched test {wf_run_id} on basic.py.")


# The "check" for test case 1. Verify that the tasks worked properly.
# Note that if we got this far, we already know that the records in the db match
# the WFRun itself, because that is automatically checked. So here we can just do
# some checking on the actual WFRun itself.
def check_basic_1(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.COMPLETED

    assert len(thr.task_runs) == 1

    task_run = thr.task_runs[0]
    assert task_run.status == LHExecutionStatusEnum.COMPLETED
    assert are_equal(task_run.stdout, HELLO)
