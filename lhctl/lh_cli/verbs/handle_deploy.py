from argparse import ArgumentParser, _SubParsersAction, Namespace
import importlib
import json
from typing import Any, Callable

from lh_lib.client import LHClient
from lh_sdk.compile import SpecsResult, get_specs
from lh_sdk.thread_spec_builder import ThreadSpecBuilder, Workflow


class DEPLOYHandler():
    def __init__(self):
        pass

    def init_subparsers(self, base_subparsers: _SubParsersAction):
        parser: ArgumentParser = base_subparsers.add_parser(
            "deploy",
            help="[Prototype] Deploy specs file created by `lhctl compile`."
        )
        source = parser.add_mutually_exclusive_group(required=True)
        source.add_argument(
            "--file",
            help="Spec file containing output of `lhctl compile`."
        )
        source.add_argument(
            "--wf-func",
            help="Module path and function name to workflow function."
        )
        parser.add_argument(
            "--skip-build", '-s',
            action='store_true',
            help='Deploy without building docker images first.'
        )
        parser.add_argument(
            "--docker_push_step",
            help="Executable file to run on each docker image to push it to cluster."
        )
        parser.set_defaults(func=self.deploy)

    def deploy(self, ns: Namespace, client: LHClient):
        if ns.file is not None:
            with open(ns.file, 'r') as f:
                specs = SpecsResult(**json.loads(f.read()))
        else:
            assert ns.wf_func is not None
            mod_name, func_name = ns.wf_func.rsplit('.', 1)
            module = importlib.import_module(mod_name)
            func = module.__dict__[func_name]
            wf = Workflow(func, module.__dict__)
            specs = get_specs(wf)

        client.deploy_specs(
            specs,
            skip_build=ns.skip_build,
            docker_push_step=ns.docker_push_step
        )
