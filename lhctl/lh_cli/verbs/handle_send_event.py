from argparse import _SubParsersAction, ArgumentParser, Namespace
import json
from typing import Any, Generic, Mapping, TypeVar

from lh_lib.client import LHClient
from lh_lib.schema import RESOURCE_TYPES
from lh_lib.schema.lh_rpc_response_schema import LHRPCResponseSchema
from lh_lib.schema.wf_run_schema import ThreadRunSchema, WFRunSchema
from lh_lib.schema.wf_spec_schema import (
    WFSpecSchema, 
    ExternalEventDefSchema,
    TaskDefSchema,
)
from lh_lib.utils.printer import Printer


class SENDEVENTHandler:
    def __init__(self):
        pass

    def init_subparsers(self, base_subparsers: _SubParsersAction):
        parser: ArgumentParser = base_subparsers.add_parser(
            "send-event",
            help="Send an ExternalEvent to a running WFRun."
        )

        parser.add_argument(
            "wf_run_id",
            help="Id of the WFRun to which the ExternalEvent is sent.",
        )
        parser.add_argument(
            "external_event_def_name",
            help="Id or Name of ExternalEventDef for the ExternalEvent to send.",
        )
        parser.add_argument(
            "payload",
            help="Raw payload in string form to send as the ExternalEvent payload.",
        )
        parser.set_defaults(func=self.send_event)

    def send_event(self, ns: Namespace, client: LHClient):
        the_id = client.send_event_by_name_or_id(
            ns.external_event_def_name,
            ns.wf_run_id,
            ns.payload,
        )
        print(f"Successfully sent ExternalEvent with ExternalEventDefId {the_id}.")