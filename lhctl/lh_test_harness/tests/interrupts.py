import time

from lh_lib.schema.wf_run_schema import LHExecutionStatusEnum, WFRunSchema
from lh_sdk.thread_spec_builder import ThreadSpecBuilder
from lh_lib.schema.wf_spec_schema import WFRunVariableTypeEnum
from lh_test_harness.test_client import TestClient
from lh_test_harness.test_utils import are_equal
from lh_test_harness.tests.shared_tasks import hello_there


# This is the interrupt handler thread!
def interrupt_handler_thread(thread: ThreadSpecBuilder):
    thread.execute(hello_there, "from the interrupt thread")


OBI = "General Kenobi"
R2 = "R2-D2"
PAYLOAD = "YOU ARE A BOLD ONE!"


# This is a Workflow Function. It is used by the `lhctl` interpreter to automagically
# generate WFSpec, TaskDef, and Dockerfile specs needed to deploy a workflow.
def interrupts(thread: ThreadSpecBuilder):
    thread.handle_interrupt("some-event", interrupt_handler_thread)

    thread.execute(hello_there, OBI)
    thread.sleep_for(5)
    thread.execute(hello_there, R2)


# Check that wf_run completes properly with no shenanigans
def launch_interrupts_0(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_interrupts_0)
    print(f"Launched test {wf_run_id} on interupts.py.")
    time.sleep(5)


def check_interrupts_0(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.COMPLETED

    assert len(thr.task_runs) == 3
    assert are_equal(thr.task_runs[0].stdout, hello_there(OBI))
    assert are_equal(thr.task_runs[2].stdout, hello_there(R2))

    for task_run in thr.task_runs:
        assert task_run.status == LHExecutionStatusEnum.COMPLETED


# Check that when we send an interrupt, things actually happen
def launch_interrupts_1(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_interrupts_1)
    print(f"Launched test {wf_run_id} on interupts.py.")
    time.sleep(1)
    client.send_event("some-event", wf_run_id, PAYLOAD)
    time.sleep(4)


def check_interrupts_1(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 2
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.COMPLETED

    assert len(thr.task_runs) == 3
    assert are_equal(thr.task_runs[0].stdout, hello_there(OBI))
    assert are_equal(thr.task_runs[2].stdout, hello_there(R2))

    for task_run in thr.task_runs:
        assert task_run.status == LHExecutionStatusEnum.COMPLETED

    thr = wf_run.thread_runs[1]
    assert thr.status == LHExecutionStatusEnum.COMPLETED
    assert len(thr.task_runs) == 1
    tr = thr.task_runs[0]
    assert tr.status == LHExecutionStatusEnum.COMPLETED
    assert are_equal(tr.stdout, hello_there("from the interrupt thread"))

# TODO: Andrew: This test fails. Can you figure out why?
# # Check that when we send multiple interrupts, things don't break
# def launch_interrupts_2(client: TestClient, wf_spec_id: str):
#     wf_run_id = client.run_wf(wf_spec_id, check_interrupts_2)
#     print(f"Launched test {wf_run_id} on interupts.py.")
#     time.sleep(1)
#     client.send_event("some-event", wf_run_id, PAYLOAD)
#     client.send_event("some-event", wf_run_id, PAYLOAD)
#     time.sleep(4)


# def check_interrupts_2(wf_run: WFRunSchema):
#     assert len(wf_run.thread_runs) == 3
#     thr = wf_run.thread_runs[0]

#     assert wf_run.status == LHExecutionStatusEnum.COMPLETED

#     assert len(thr.task_runs) == 3
#     assert are_equal(thr.task_runs[0].stdout, hello_there(OBI))
#     assert are_equal(thr.task_runs[2].stdout, hello_there(R2))

#     for task_run in thr.task_runs:
#         assert task_run.status == LHExecutionStatusEnum.COMPLETED

#     thr = wf_run.thread_runs[1]
#     assert thr.status == LHExecutionStatusEnum.COMPLETED
#     assert len(thr.task_runs) == 1
#     tr = thr.task_runs[0]
#     assert tr.status == LHExecutionStatusEnum.COMPLETED
#     assert are_equal(tr.stdout, hello_there("from the interrupt thread"))

#     thr = wf_run.thread_runs[2]
#     assert thr.status == LHExecutionStatusEnum.COMPLETED
#     assert len(thr.task_runs) == 1
#     tr = thr.task_runs[0]
#     assert tr.status == LHExecutionStatusEnum.COMPLETED
#     assert are_equal(tr.stdout, hello_there("from the interrupt thread"))
