from cProfile import run
import time
from tkinter import E

from lh_lib.schema.wf_run_schema import (
    LHExecutionStatusEnum,
    LHFailureReasonEnum,
    NodeTypeEnum,
    WFRunSchema,
)
from lh_test_harness.test_client import TestClient
from lh_sdk.thread_spec_builder import ThreadSpecBuilder
from lh_test_harness.tests.shared_tasks import echo_task


UNDER_TEN = "Its under 10!"

TEN_OR_MORE = "Its 10 or more!"

MERGER = "This task should execute on all WFRun's."


# This is the workflow function that we test.
def conditionals_edge_1(thread: ThreadSpecBuilder):
    my_int = thread.add_variable("my_int", int)

    # thread.execute(echo_task, "")
    greater_than_ten = my_int.greater_than(10)

    with greater_than_ten.is_true():
        thread.execute(echo_task, TEN_OR_MORE)

    thread.execute(echo_task, MERGER)


# The "input" for test case 1. It's simple; we just launch the workflow.
def launch_conditionals_edge_1(client: TestClient, wf_spec_id: str):
    for i in [9, 11]:
        wf_run_id = client.run_wf(
            wf_spec_id,
            check_conditionals_edge_1,
            my_int=i,
        )
        print(f"Launched test {wf_run_id} on conditionals_edge_1.")

# The "check" for test case 1. Verify that the tasks worked properly.
# Note that if we got this far, we already know that the records in the db match
# the WFRun itself, because that is automatically checked. So here we can just do
# some checking on the actual WFRun itself.
def check_conditionals_edge_1(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1, "WFRun should have one thread"
    thr = wf_run.thread_runs[0]

    assert wf_run.status == LHExecutionStatusEnum.COMPLETED, "WFRun should finish"

    assert thr.variables is not None, "Thread should have variables"

    my_int = thr.variables['my_int']
    assert isinstance(my_int, int), "orig input should be int"

    tasks = [t for t in thr.task_runs if t.node_type == NodeTypeEnum.TASK]

    if my_int > 10:
        assert len(tasks) == 2, "Should have two tasks"
        assert tasks[0].stdout == TEN_OR_MORE, "Ten or more task"
        assert tasks[1].stdout == MERGER, "Merger task"
    else:
        assert len(tasks) == 1, "Should have one task"
        assert tasks[0].stdout == MERGER, "merger task"


def launch_conditionals_edge_2(client: TestClient, wf_spec_id: str):
    # Use this as a chance to verify that when we pass in bad input, we get a usable
    # error message
    wf_run_id = client.run_wf(
        wf_spec_id, check_conditionals_edge_2,
        my_int="not an int"
    )


def check_conditionals_edge_2(wf_run: WFRunSchema):
    # Need to make sure the wfRun actually exists
    assert wf_run.status == LHExecutionStatusEnum.HALTED, "Shouldve failed"
    thr = wf_run.thread_runs[0]
    assert len(thr.task_runs) == 1, "Should have one task"

    tr = thr.task_runs[0]
    assert tr.status == LHExecutionStatusEnum.HALTED, "task should fail"
    assert tr.node_name == NodeTypeEnum.NOP, "should be nop node"
    assert tr.failure_reason == LHFailureReasonEnum.VARIABLE_LOOKUP_ERROR, \
        "Should be variable lookup error"
