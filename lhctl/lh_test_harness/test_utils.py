from inspect import getsourcefile, signature, Signature
from concurrent.futures import ThreadPoolExecutor
import json
import os
from typing import Optional
import uuid

from lh_lib.schema.wf_spec_schema import (
    TaskDefSchema,
)

from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from sqlalchemy.orm.session import sessionmaker


POSTGRES_URI = os.getenv(
    "DB_URI",
    "postgresql://postgres:postgres@localhost:5432/postgres"
)

_engine = None
_session_maker = None

def get_session() -> Session:
    global _engine
    global _session_maker

    if _engine is None:
        assert _session_maker is None
        _engine = create_engine(POSTGRES_URI)
        _session_maker = sessionmaker(bind=_engine)

    return _session_maker() # type: ignore

DEFAULT_API_URL = os.getenv("LHORSE_API_URL", "http://localhost:5000")
DOCKER_IMAGE = os.getenv("LHORSE_TEST_DOCKER_IMAGE", "little-horse-test:latest")


_executor: Optional[ThreadPoolExecutor] = None


def get_executor():
    global _executor
    if _executor is None:
        _executor = ThreadPoolExecutor()
    
    return _executor


def generate_guid() -> str:
    return uuid.uuid4().hex


def get_test_module_name(test_name: str) -> str:
    return f"lh_test_harness.tests.{test_name}"


def get_root_dir():
    this_file = getsourcefile(lambda: 0)
    assert this_file is not None
    dir_of_this_file = os.path.split(this_file)[0]
    return os.path.join(dir_of_this_file, '..')


def cleanup_case_name(case):
    if not case.endswith('.json'):
        case += '.json'

    this_file = getsourcefile(lambda: 0)
    assert this_file is not None

    dir_of_this_file = os.path.split(this_file)[0]
    test_dir = os.path.join(
        dir_of_this_file,
        "../tests/"
    )
    case = os.path.join(test_dir, os.path.split(case)[1])

    return case


def are_equal(var1, var2):
    if var1 is None and var2 is None:
        return True

    if var1 is not None and var2 is None:
        return False

    if var2 is not None and var1 is None:
        return False

    if (type(var1) == bool and type(var2) in [bool, int] or\
        type(var2) == bool and type(var1) in [bool, int]
    ):
        return bool(var1) == bool(var2)

    if type(var1) != type(var2):
        return False

    if type(var1) in [str, int, bool, float]:
        return var1 == var2

    if type(var1) == list:
        if len(var1) != len(var2):
            return False

        for i in range(len(var1)):
            if not are_equal(var1[i], var2[i]):
                return False
        return True

    assert type(var1) == dict

    if len(list(var1.keys())) != len(list(var2.keys())):
        return False

    for k in var1.keys():
        if k not in var2:
            return False
        if not are_equal(var1[k], var2[k]):
            return False
    return True


def inject_test_to_taskdef(task_def: TaskDefSchema):
    old_deploy_meta = json.loads(task_def.deploy_metadata or '{}')
    old_deploy_meta_meta = json.loads(old_deploy_meta['metadata'])
    old_bash_command = old_deploy_meta_meta['bashCommand']

    assert old_bash_command[0] == 'python'
    assert old_bash_command[1] == '-m'
    assert old_bash_command[2] == 'executor'
    assert old_bash_command[3] == task_def.name

    new_prefix = [
        "python",
        "-m",
        "lh_test_harness.test_executor",
        "---THREAD_RUN_ID---",
        "---TASK_RUN_NUMBER---",
        "---WF_RUN_ID---",
        task_def.name,
    ]

    old_deploy_meta_meta['bashCommand'] = new_prefix + old_bash_command[4:]

    old_deploy_meta['metadata'] = json.dumps(old_deploy_meta_meta)
    task_def.deploy_metadata = json.dumps(old_deploy_meta)
    return task_def
