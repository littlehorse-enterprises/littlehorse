import time

from lh_lib.schema.wf_run_schema import (
    LHExecutionStatusEnum,
    LHFailureReasonEnum,
    WFRunSchema,
)
from lh_sdk.thread_spec_builder import ThreadSpecBuilder
from lh_test_harness.test_client import TestClient
from lh_test_harness.tests.shared_tasks import hello_there


# This is the interrupt handler thread!
def interrupt_handler_thread(thread: ThreadSpecBuilder):
    thread.execute(hello_there, "from the interrupt thread")


PAYLOAD = "GENERAL KENOBI"

def external_event_basic(thread: ThreadSpecBuilder):
    my_name = thread.add_variable("my_name", str)
    event = thread.wait_for_event("my-name").with_timeout(3)
    my_name.assign(event)

    thread.execute(hello_there, my_name)


# Check that the thing dies when there's no event that comes in.
def launch_external_event_basic_0(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_external_event_basic_0)
    print(f"Launched test {wf_run_id} on interupts.py.")
    time.sleep(3)


def check_external_event_basic_0(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.HALTED
    task = thr.task_runs[0]
    assert task.status == LHExecutionStatusEnum.HALTED
    assert task.failure_reason == LHFailureReasonEnum.TIMEOUT


# Check that when we send an interrupt, things actually happen
def launch_external_event_basic_1(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_external_event_basic_1)
    print(f"Launched test {wf_run_id} on interupts.py.")
    time.sleep(1)
    client.send_event("my-name", wf_run_id, PAYLOAD)
    time.sleep(4)


def check_external_event_basic_1(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.COMPLETED
    task = thr.task_runs[0]
    assert task.status == LHExecutionStatusEnum.COMPLETED
    assert task.stdout == PAYLOAD

    vars = thr.variables
    assert vars is not None
    assert vars["my_name"] == PAYLOAD
