from lh_lib.schema.wf_run_schema import LHExecutionStatusEnum, WFRunSchema
from lh_test_harness.test_client import TestClient
from lh_sdk.thread_spec_builder import ThreadSpecBuilder
from lh_test_harness.test_utils import are_equal
from lh_test_harness.tests.shared_tasks import big_blob_task, echo_task


str_result = "this is some str"


def var_assign_jsonpath_happy(thread: ThreadSpecBuilder):
    my_int = thread.add_variable("my_int", int)
    my_bool = thread.add_variable("my_bool", bool)
    my_list = thread.add_variable("my_list", list)
    my_str = thread.add_variable("my_str", str)
    my_float = thread.add_variable("my_float", float)
    my_obj = thread.add_variable("my_obj", dict)
    my_second_obj = thread.add_variable("my_second_obj", dict)

    big_blob = thread.execute(big_blob_task)

    my_list.assign(big_blob.jsonpath("$.some_list"))
    my_int.assign(big_blob.jsonpath("$.some_blob.some_int"))
    my_bool.assign(big_blob.jsonpath("$.some_blob.some_bool"))
    my_obj.assign(big_blob)
    my_second_obj.assign(big_blob.jsonpath("$.some_blob"))
    my_str.assign(str_result)

    thread.execute(echo_task, "hello there!")
    my_float.assign(my_second_obj.jsonpath("$.some_float"))



def launch_var_assign_jsonpath_happy(client: TestClient, wf_spec_id: str):
    wf_run_id = client.run_wf(wf_spec_id, check_launch_var_assign_jsonpath_happy_1)
    print(f"Launched test {wf_run_id} on var_assign_jsonpath_happy.py.")


def check_launch_var_assign_jsonpath_happy_1(wf_run: WFRunSchema):
    assert len(wf_run.thread_runs) == 1, "len thread runs"
    thr = wf_run.thread_runs[0]
    assert thr.status == LHExecutionStatusEnum.COMPLETED, "successful wfrun"

    assert len(thr.task_runs) == 2, "len task runs"
    assert all(
        [t.status == LHExecutionStatusEnum.COMPLETED for t in thr.task_runs]
    ), "task runs completed"

    big_blob = big_blob_task()

    vars = thr.variables
    assert vars is not None
    assert are_equal(vars['my_list'], big_blob['some_list']), "My list"
    assert are_equal(vars['my_int'], 1), "my int"
    assert are_equal(vars['my_bool'], False), "my bool"
    assert are_equal(vars['my_obj'], big_blob), "my obj"
    assert are_equal(vars['my_second_obj'], big_blob["some_blob"]), "my second obj"

    assert are_equal(vars['my_str'], str_result), "my str"
    assert are_equal(vars['my_float'], 2.5), "my float"
