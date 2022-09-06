import json
import time

from lh_lib.schema.wf_run_schema import LHExecutionStatusEnum, WFRunSchema
from lh_sdk.thread_spec_builder import ThreadSpecBuilder
from lh_lib.schema.wf_spec_schema import WFRunVariableTypeEnum
from lh_test_harness.test_client import TestClient
from lh_test_harness.test_utils import are_equal
from lh_test_harness.tests.shared_tasks import hello_there


OBI = "General Kenobi"
R2 = "R2-D2"
AHSOKA = "Padawan"


# Handler for some-event
def some_event_handler(thread: ThreadSpecBuilder):
    thread.execute(hello_there, OBI)


# Handler for another-event
def another_event_handler(thread: ThreadSpecBuilder):
    thread.execute(hello_there, AHSOKA)


# Workflow with two registered interrupts!
def interrupts_multiple(thread: ThreadSpecBuilder):
    thread.handle_interrupt("some-event", some_event_handler)
    thread.handle_interrupt("another-event", another_event_handler)

    thread.execute(hello_there, R2)
    thread.sleep_for(5)


# Check that when we send an interrupt, things actually happen
def launch_interrupts_multiple_0(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_interrupts_multiple_0)
    print(f"Launched test {wf_run_id} on {__file__.split('/')[-1]}")
    time.sleep(1)
    client.send_event("some-event", wf_run_id, "hola")
    time.sleep(4)


def check_interrupts_multiple_0(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 2
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.COMPLETED

    assert len(thr.task_runs) == 2
    assert are_equal(thr.task_runs[0].stdout, hello_there(R2))

    for task_run in thr.task_runs:
        assert task_run.status == LHExecutionStatusEnum.COMPLETED

    thr = wf_run.thread_runs[1]
    assert thr.status == LHExecutionStatusEnum.COMPLETED
    assert len(thr.task_runs) == 1
    tr = thr.task_runs[0]
    assert tr.status == LHExecutionStatusEnum.COMPLETED
    assert are_equal(tr.stdout, hello_there(OBI))


# Check that when we send an interrupt, things actually happen
def launch_interrupts_multiple_1(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_interrupts_multiple_1)
    print(f"Launched test {wf_run_id} on {__file__.split('/')[-1]}")
    time.sleep(1)
    client.send_event("another-event", wf_run_id, "hola")
    time.sleep(4)


def check_interrupts_multiple_1(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 2
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.COMPLETED

    assert len(thr.task_runs) == 2
    assert are_equal(thr.task_runs[0].stdout, hello_there(R2))

    for task_run in thr.task_runs:
        assert task_run.status == LHExecutionStatusEnum.COMPLETED

    thr = wf_run.thread_runs[1]
    assert thr.status == LHExecutionStatusEnum.COMPLETED
    assert len(thr.task_runs) == 1
    tr = thr.task_runs[0]
    assert tr.status == LHExecutionStatusEnum.COMPLETED
    assert are_equal(tr.stdout, hello_there(AHSOKA))


# # TODO: This test doesn't pass because we can't stack interrupts yet.
# # Check that when we send two interrupts, things work
# def launch_interrupts_multiple_2(client: TestClient, wf_spec_id: str):
#     wf_run_id = client.run_wf(wf_spec_id, check_interrupts_multiple_2)
#     print(f"Launched test {wf_run_id} on {__file__.split('/')[-1]}")
#     time.sleep(1)
#     client.send_event("another-event", wf_run_id, "hola")
#     time.sleep(2)
#     client.send_event("some-event", wf_run_id, "hola")
#     time.sleep(2)


# def check_interrupts_multiple_2(wf_run: WFRunSchema):
#     assert len(wf_run.thread_runs) == 3, "should have 3 thread runs"
#     thr = wf_run.thread_runs[0]

#     assert wf_run.status == LHExecutionStatusEnum.COMPLETED

#     assert len(thr.task_runs) == 2
#     assert are_equal(thr.task_runs[0].stdout, hello_there(R2))

#     for task_run in thr.task_runs:
#         assert task_run.status == LHExecutionStatusEnum.COMPLETED

#     thr = wf_run.thread_runs[1]
#     assert thr.status == LHExecutionStatusEnum.COMPLETED
#     assert len(thr.task_runs) == 1
#     tr = thr.task_runs[0]
#     assert tr.status == LHExecutionStatusEnum.COMPLETED
#     assert are_equal(tr.stdout, hello_there(AHSOKA))

#     thr = wf_run.thread_runs[2]
#     assert thr.status == LHExecutionStatusEnum.COMPLETED
#     assert len(thr.task_runs) == 1
#     tr = thr.task_runs[0]
#     assert tr.status == LHExecutionStatusEnum.COMPLETED
#     assert are_equal(tr.stdout, hello_there(OBI))
