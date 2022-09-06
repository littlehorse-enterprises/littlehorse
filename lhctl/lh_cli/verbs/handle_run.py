from argparse import ArgumentParser, _SubParsersAction, Namespace
import json
import sys

from lh_lib.client import LHClient


class RUNHandler:
    def __init__(self):
        pass

    def init_subparsers(self, base_subparsers: _SubParsersAction):
        parser: ArgumentParser = base_subparsers.add_parser(
            "run",
            help="Create a WFRun by running an instance of a WFSpec."
        )
        parser.add_argument(
            "wf_spec",
            help="Name or Id of WFSpec to run."
        )
        parser.add_argument(
            "--variables", "-d",
            help="Optional json-dumped dictionary of input variables to the WFRun.",
            default=None,
        )
        parser.add_argument(
            "--wf-run-id", "-id",
            help="Specify Id for the WFRun. If the Id is already taken, the " +
                "request will fail. Can be used for idempotency",
            default=None,
        )
        parser.set_defaults(func=self.run_wf)

    def run_wf(self, ns: Namespace, client: LHClient):
        try:
            vars: dict = json.loads(ns.variables or '{}')
        except Exception as exn:
            print(
                "If providing variables, please format them with proper json:",
                file=sys.stderr
            )
            print(f'\t{str(exn)}', file=sys.stderr)
            exit(1)

        run_wf_response = client.run_wf(
            ns.wf_spec,
            vars=vars,
            wf_run_id=ns.wf_run_id
        )

        print(run_wf_response.json())


