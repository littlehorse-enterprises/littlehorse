import importlib
from lh_sdk.compile import get_specs

from lh_lib.client import LHClient
from lh_test_harness.test_utils import get_test_module_name, inject_test_to_taskdef
from lh_sdk.thread_spec_builder import Workflow


def deploy_test(client: LHClient, test_name: str, docker_push_step: str) -> None:
    mod_name = get_test_module_name(test_name)
    module = importlib.import_module(mod_name)
    func = module.__dict__[test_name]
    wf = Workflow(func, module.__dict__)
    specs = get_specs(wf)

    # # This is now deprecated
    # for task_def in specs.task_def:
    #     inject_test_to_taskdef(task_def)

    client.deploy_specs(specs, docker_push_step=docker_push_step)
