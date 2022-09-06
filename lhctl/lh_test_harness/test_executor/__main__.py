import argparse
from contextlib import closing
import importlib
import json
import os
from statistics import variance
from typing import Callable

from sqlalchemy import values
from lh_test_harness.db_schema import TaskRun

from lh_sdk.utils import cast_all_args, parse_task_def_name
from lh_test_harness.test_utils import get_session

# # Make all the task implementation functions available for the voodoo we do.
# from lh_harness.task_implementations import *


def execute_task(thread_run_id, task_run_number, wf_run_id, task_def_name, *args):
    with closing(get_session()) as ses:
        et_helper(
            thread_run_id, task_run_number, wf_run_id, task_def_name,
            ses, *args
        )

def et_helper(thread_run_id, task_run_number, wf_run_id, task_def_name, ses, *args):
    module_name, func_name = parse_task_def_name(task_def_name)
    module = importlib.import_module(module_name)
    func: Callable = module.__dict__[func_name]

    new_args = cast_all_args(func, *args)

    result = None
    stderr = None
    exn_to_raise = None
    try:
        result = func(*list(new_args.values()))
        if isinstance(result, list) or isinstance(result, dict):
            result = json.dumps(result)
    except Exception as exn:
        exn_to_raise = exn
        import traceback
        stderr = traceback.format_exc()

    task_run = TaskRun(
        variables=new_args,
        wf_run_id=wf_run_id,
        thread_run_id=thread_run_id,
        task_run_number=task_run_number,
        stdout=result,
        stderr=stderr,
        task_def=task_def_name,
    )
    ses.add(task_run)
    ses.commit()

    print(result, end='')
    if exn_to_raise is not None:
        raise exn_to_raise


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("thread_run_id")
    parser.add_argument("task_run_number")
    parser.add_argument("wf_run_id")
    parser.add_argument("task_def_name")

    ns, unknown = parser.parse_known_args()

    execute_task(
        ns.thread_run_id, ns.task_run_number, ns.wf_run_id,
        ns.task_def_name, *unknown
    )

