import time

from lh_test_harness.test_client import TestClient
from lh_sdk.thread_spec_builder import ThreadSpecBuilder
from lh_lib.schema.wf_run_schema import LHExecutionStatusEnum, WFRunSchema
from lh_test_harness.tests.shared_tasks import increment


def subthread(st: ThreadSpecBuilder):
    parent_var = st.get_parent_var("parent_var")
    parent_var.add(st.execute(increment, 12))


def bigger_subthread(st: ThreadSpecBuilder):
    st.execute(increment, 123456)
    parent_var = st.get_parent_var("parent_var")
    parent_var.subtract(st.execute(increment, 4))


def threads_basic(thread: ThreadSpecBuilder):
    input_var = thread.add_variable("input_var", int)
    parent_var = thread.add_variable("parent_var", int)

    # Add 1 to the input
    parent_var.assign(thread.execute(increment, input_var))

    # Add 13 to the input twice
    child1 = thread.spawn_thread(subthread)
    child2 = thread.spawn_thread(subthread)

    # Subtract 5 from the input
    child3 = thread.spawn_thread(bigger_subthread)

    thread.wait_for_thread(child1)
    thread.wait_for_thread(child2)
    thread.wait_for_thread(child3)
    # At this point, parent_var = (2 * input) + 21

    # After the last task, the parent_var should now be ((2 * input) + 21) / 3
    parent_var.divide(thread.execute(increment, 2))


def launch_threads_basic(client: TestClient, wf_spec_id: str):
    for i in range(-20, 20):
        wf_run_id = client.run_wf(wf_spec_id, check_threads_basic, input_var=i)
        print(f"Launched test {wf_run_id} on {__file__}")
    time.sleep(10)


def check_threads_basic(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 4, "should have 4 threads"
    for thread_run in wf_run.thread_runs:
        assert thread_run.status == LHExecutionStatusEnum.COMPLETED, "Should complete"

    # Need to make sure that the variable locking worked properly.
    thr = wf_run.thread_runs[0]
    assert thr.variables is not None, "Should have variables"
    input_var = thr.variables['input_var']
    parent_var = thr.variables['parent_var']

    assert isinstance(input_var, int), "input var should be int"
    assert isinstance(parent_var, int), "parent var should be int"

    answer = 1 + input_var
    answer += 26
    answer -= 5

    answer = (int) (answer / 3)
    assert parent_var == answer, "Got the wrong answer!"
