from lh_lib.schema.wf_run_schema import LHExecutionStatusEnum, WFRunSchema
from lh_test_harness.test_client import TestClient
from lh_sdk.thread_spec_builder import ThreadSpecBuilder
from lh_test_harness.test_utils import are_equal
from lh_test_harness.tests.shared_tasks import OBI_GREETING, echo_task
from lh_test_harness.tests.var_assign_jsonpath_happy import big_blob_task


def var_adds(thread: ThreadSpecBuilder):
    counter = thread.add_variable("counter", int, default_val=-1)
    arr = thread.add_variable("arr", list, default_val=[])

    big_blob = thread.execute(big_blob_task)

    arr.extend(big_blob.jsonpath('$.some_list'))
    counter.add(big_blob.jsonpath('$.some_list[1]'))

    thread.execute(echo_task, OBI_GREETING)
    counter.add(1)
    arr.remove_if_present(4)


def launch_var_adds_1(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(
        wf_spec_id,
        check_var_adds_1,
    )
    print(f"Ran wf_run_id {wf_run_id} on var_adds case 1")


# This one checks the 
def check_var_adds_1(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1, "only one thread"
    assert wf_run.status == LHExecutionStatusEnum.COMPLETED, "wf run completed"

    vars = wf_run.thread_runs[0].variables
    assert vars is not None, "has variables"

    assert are_equal(vars['arr'], [1, 2, 3]), "arr"
    assert are_equal(vars['counter'], 2), "counter"


def launch_var_adds_2(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(
        wf_spec_id,
        check_var_adds_2,
        arr=[5, 4, 3, 2],
        counter=0,
    )
    print(f"Ran wf_run_id {wf_run_id} on var_adds case 2")


def check_var_adds_2(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1, "only one thread"
    assert wf_run.status == LHExecutionStatusEnum.COMPLETED, "wf run completed"

    vars = wf_run.thread_runs[0].variables
    assert vars is not None, "has variables"

    assert are_equal(vars['arr'], [5, 3, 2, 1, 2, 3]), "arr"
    assert are_equal(vars['counter'], 3), "counter"
