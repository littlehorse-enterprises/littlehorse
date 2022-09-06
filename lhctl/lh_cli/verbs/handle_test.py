'''
TODO: This probably shouldn't be a part of the production `lhctl` binary.
'''

from argparse import ArgumentParser, _SubParsersAction, Namespace
import json
import os
import sys
import time

from lh_lib.client import LHClient
from lh_test_harness.harness.deploy_test import deploy_test
from lh_test_harness.harness.logic import (
    check_all_tests,
    get_and_print_summary,
    launch_test,
)
from lh_test_harness.test_client import TestClient


class TESTHandler:
    def __init__(self):
        pass

    def init_subparsers(self, base_subparsers: _SubParsersAction):
        parser: ArgumentParser = base_subparsers.add_parser(
            "test",
            help="[Prototype] Run LittleHorse integration tests."
        )
        parser.add_argument(
            "--deploy",
            action="store_true",
            help="Whether to deploy the specified test workflows."
        )
        parser.add_argument(
            "--docker_push_step",
            help="Executable file to run on each docker image to push it to cluster."
        )
        parser.add_argument(
            "--requests", "-r",
            help="Number of requests to run for each test case.",
            type=int,
            default=1,
        )
        parser.add_argument(
            "cases", nargs='?',
            action='append',
            help="Names of test cases to run. If left blank, default to all cases.",
        )
        parser.set_defaults(func=self.handle_test)

    def handle_test(self, ns: Namespace, client: LHClient):
        if ns.cases is None or len(ns.cases) == 0 or (ns.cases[0] is None):
            cases = [
                file[:-3] for file in os.listdir(
                    '/' + os.path.join(
                        *(__file__.split('/')[:-1]),
                        "../../lh_test_harness/tests/"
                    )
                ) if '__' not in file and file.endswith('.py') and
                file != 'shared_tasks.py'
            ]
        else:
            cases = ns.cases

        test_client = TestClient(client)

        if ns.deploy:
            for case in cases:
                deploy_test(client, case, ns.docker_push_step)

            time.sleep(16)

        for case in cases:
            launch_test(case, test_client, ns.requests)

        time.sleep(3)

        for case in cases:
            check_all_tests(case, test_client)

        get_and_print_summary()
