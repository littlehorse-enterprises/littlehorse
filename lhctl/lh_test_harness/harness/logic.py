from contextlib import closing
import importlib
import os
import sys
from types import ModuleType
from typing import Callable, Iterable, List, Optional, Tuple

from sqlalchemy import func

from lh_lib.schema.wf_run_schema import (
    LHExecutionStatusEnum,
    NodeTypeEnum,
    WFRunSchema,
    TaskRunSchema,
)

from lh_test_harness.test_client import TestClient
from lh_test_harness.test_utils import (
    are_equal,
    get_executor,
    get_session,
    get_test_module_name,
)
from lh_test_harness.db_schema import (
    WFRun,
    TaskRun,
    TestStatus,
)


def get_launch_funcs(
    test_name: str,
    mod: ModuleType
) -> List[Callable[[TestClient, str], None]]:
    out = []

    for key in mod.__dict__.keys():
        if key.startswith(f'launch_{test_name}'):
            out.append(mod.__dict__[key])

    return out


def launch_test(test_name: str, client: TestClient, num_requests: int):
    test_module_name = get_test_module_name(test_name)
    mod: ModuleType = importlib.import_module(test_module_name)

    launch_funcs = get_launch_funcs(test_name, mod)
    executor = get_executor()

    futures = [
        executor.submit(f, client, test_name)
        for f in launch_funcs
        for _ in range(num_requests)
    ]

    for f in futures:
        f.result()


def iter_all_task_runs(wf_run: WFRunSchema) -> Iterable[Tuple[int, TaskRunSchema]]:
    for i, thread_run in enumerate(wf_run.thread_runs):
        for task_run in thread_run.task_runs:
            if task_run.node_type != NodeTypeEnum.TASK:
                continue
            yield i, task_run


def find_task_run(wf_run_orm, thr_num, tr_num) -> Optional[TaskRun]:
    task_runs: List[TaskRun] = wf_run_orm.task_runs

    for task_run in task_runs:
        if task_run.thread_run_id == thr_num and task_run.task_run_number == tr_num:
            return task_run

    return None


def check_for_consistency(wf_run_orm: WFRun, wf_run: WFRunSchema):
    orphans = []
    mis_reports = []
    for thr_num, task_run in iter_all_task_runs(wf_run):
        # make sure that each task_run:
        # 1. Actually got executed and recorded in the db
        # 2. Matches the output in the db
        assert thr_num == task_run.thread_id
        tr_orm = find_task_run(
            wf_run_orm, task_run.thread_id, task_run.position
        )

        if tr_orm == None:
            if task_run.status == LHExecutionStatusEnum.HALTED:
                # This means that the task got scheduled but the worker was down.
                orphans.append(task_run)
                continue
            else:
                # This means that LittleHorse hallucinated about the task and thought
                # it got done but the task never did get done (either that, or the
                # database decided to drop a record, which shouldn't be possible)
                return (
                    TestStatus.FAILED_UNACCEPTABLE,
                    "Phantom task run that wasn't found in database!"
                )

        if not are_equal(tr_orm.stdout, task_run.stdout):
            if task_run.status == LHExecutionStatusEnum.HALTED:
                mis_reports.append(task_run)
            else:
                return (
                    TestStatus.FAILED_UNACCEPTABLE,
                    "DB and LH show different stdouts!"
                )

    wf_run_orm.num_mis_reported = len(mis_reports)
    wf_run_orm.num_orphans = len(orphans)

    if len(orphans) == 0 and len(mis_reports) == 0:
        return None, None

    return TestStatus.FAILED_ACCEPTABLE, "Had some minor reporting errors"


def check_all_tests(test_name: str, client: TestClient):
    # First, verify that the LittleHorse API reporting and the test db reporting
    # match.
    with closing(get_session()) as ses:
        for wf_run_orm, wf_run in client.iter_test_runs(test_name, ses):
            # # We've deprecated the check_for_consistency check now.
            # new_status, message = check_for_consistency(wf_run_orm, wf_run)

            # if new_status is not None and new_status != TestStatus.FAILED_ACCEPTABLE:
            #     wf_run_orm.status = new_status
            #     wf_run_orm.message = message
            #     ses.merge(wf_run_orm)
            #     ses.commit()
            #     continue

            mod = importlib.import_module(wf_run_orm.check_func_module)
            func = mod.__dict__[wf_run_orm.check_func_name]
            try:
                func(wf_run)
                wf_run_orm.status = TestStatus.SUCCEEDED
            except AssertionError as exn:
                if wf_run_orm.status != TestStatus.FAILED_ACCEPTABLE:
                    wf_run_orm.status = TestStatus.FAILED_UNACCEPTABLE
                    wf_run_orm.message = exn.args[0] if len(exn.args) else 'Failed.'
                    _, _, exc_tb = sys.exc_info()
                    assert exc_tb is not None
                    fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
                    assert wf_run_orm.message is not None
                    wf_run_orm.message += f"line: {exc_tb.tb_lineno}, file: {fname}"

            ses.merge(wf_run_orm)
            ses.commit()


def get_and_print_summary():
    with closing(get_session()) as session:
        result = session.query(
            WFRun.status, func.count(WFRun.status) # type: ignore
        ).group_by(
            WFRun.status
        )

        for row in result.all():
            print(row[0], "---", row[1])

        printed = False
        result = session.query(
            WFRun.message, func.count(WFRun.message) # type: ignore
        ).group_by(
            WFRun.message
        )
        for row in result:
            if row[0] != None:
                if not printed:
                    printed = True
                    print("\n\nError Messages:")
                print(row[0], "---", row[1])
