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
PAYLOAD = {"Jedi": "Obi-Wan", "Sith": "Vader"}


# This is the interrupt handler thread!
def interrupt_handler_thread(thread: ThreadSpecBuilder):
    thread.add_variable("Sith", str)
    jedi = thread.add_variable("Jedi", str)

    thread.execute(hello_there, jedi)


# This is a Workflow Function. It is used by the `lhctl` interpreter to automagically
# generate WFSpec, TaskDef, and Dockerfile specs needed to deploy a workflow.
def interrupts_with_payload(thread: ThreadSpecBuilder):
    thread.handle_interrupt("some-event", interrupt_handler_thread)

    thread.execute(hello_there, OBI)
    thread.sleep_for(5)
    thread.execute(hello_there, R2)


# Check that wf_run completes properly with no shenanigans
def launch_interrupts_with_payload_0(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_interrupts_with_payload_0)
    print(f"Launched test {wf_run_id} on interupts_with_payload.py.")
    time.sleep(5)


def check_interrupts_with_payload_0(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.COMPLETED

    assert len(thr.task_runs) == 3
    assert are_equal(thr.task_runs[0].stdout, hello_there(OBI))
    assert are_equal(thr.task_runs[2].stdout, hello_there(R2))

    for task_run in thr.task_runs:
        assert task_run.status == LHExecutionStatusEnum.COMPLETED


# Check that when we send an interrupt, things actually happen
def launch_interrupts_with_payload_1(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_interrupts_with_payload_1)
    print(f"Launched test {wf_run_id} on interupts_with_payload.py.")
    time.sleep(1)
    client.send_event("some-event", wf_run_id, json.dumps(PAYLOAD))
    time.sleep(4)


def check_interrupts_with_payload_1(wf_run: WFRunSchema):
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
    assert are_equal(tr.stdout, hello_there(PAYLOAD['Jedi']))

    assert thr.variables is not None, "Should have variables"
    assert are_equal(thr.variables, PAYLOAD), "Variables shouldbe set"
