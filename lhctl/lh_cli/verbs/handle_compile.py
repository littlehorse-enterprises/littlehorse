from argparse import ArgumentParser, _SubParsersAction, Namespace
import importlib
from typing import Any

from lh_lib.client import LHClient
from lh_sdk.compile import get_specs
from lh_sdk.thread_spec_builder import Workflow, THREAD_FUNC


class COMPILEHandler():
    def __init__(self):
        pass

    def init_subparsers(self, base_subparsers: _SubParsersAction):
        parser: ArgumentParser = base_subparsers.add_parser(
            "compile",
            help="Compile a LH SDK workflow from python code into JSON Spec files."
        )
        parser.add_argument(
            "wf_func",
            help="Full python name of the workflow function, including module, eg "
                "'my_dir.my_file.my_func'.",
        )
        parser.set_defaults(func=self.compile)

    def compile(self, ns: Namespace, client: LHClient):
        # First, import the relevant module so that we have access to the functions.
        mod_name = '.'.join(ns.wf_func.split(".")[:-1])
        func_name = ns.wf_func.split('.')[-1]
        mod = importlib.import_module(mod_name)

        # This should work because we should have imported the function in the line
        # above
        wf_func: THREAD_FUNC = mod.__dict__[func_name]
        wf = Workflow(wf_func, mod.__dict__)
        specs = get_specs(wf)

        print(specs.json(by_alias=True))
