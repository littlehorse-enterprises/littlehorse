from argparse import _SubParsersAction, ArgumentParser, Namespace
import json
from typing import Any, Generic, Mapping, TypeVar

from lh_lib.client import RangeQueryResultSchema, LHClient
from lh_lib.schema import RESOURCE_TYPES
from lh_lib.schema.lh_rpc_response_schema import LHRPCResponseSchema


class SEARCHHandler:
    def __init__(self):
        pass

    def init_subparsers(self, base_subparsers: _SubParsersAction):
        parser: ArgumentParser = base_subparsers.add_parser(
            "search",
            help="Search for Resources based on Label Keys and Values."
        )

        parser.add_argument(
            "resource_type",
            choices=[k for k in RESOURCE_TYPES.keys()],
            help="Resource Type to Search."
        )

        st_subparsers = parser.add_subparsers(
            dest='search_type',
            required=False,
            help="Type of search. If blank, iterates through all resources.",
        )

        # key-value lookup
        key_val_subp = st_subparsers.add_parser(
            "key-val", help="Search for specific key-value pair."
        )
        key_val_subp.add_argument(
            "key",
            help="Key to search for."
        )
        key_val_subp.add_argument(
            "value",
            help="Value to search for.",
        )
        key_val_subp.set_defaults(func=self.key_value_search)

        # lookup based on created time
        created_sp = st_subparsers.add_parser(
            "seconds-ago", help="Lookup based on created time, in seconds ago."
        )
        created_sp.add_argument(
            "--since", '-s',
            help="Number of seconds since which to begin search."
        )
        created_sp.add_argument(
            "--until", '-u',
            help="Number of seconds until which to continue search.",
        )
        created_sp.set_defaults(func=self.time_search)

        # key-value range lookup
        range_subp = st_subparsers.add_parser(
            "key-val-range", help="Search for range."
        )
        range_subp.add_argument(
            "--key", '-k', required=True,
            help="Key to search for."
        )
        range_subp.add_argument(
            "--start", "-s",
            help="Value to start search from. If blank, accepts all values <= end."
        )
        range_subp.add_argument(
            "--end", "-e",
            help="Value to end search at. If blank, accepts all values >= start."
        )
        range_subp.set_defaults(func=self.range_search)

        # Utility
        parser.add_argument(
            "--limit", "-l", type=int, default=10,
            help="Limit number of records for this page."
        )
        parser.add_argument(
            "--token", "-t",
            help="Iterator token for paginated searches."
        )
        parser.add_argument(
            "--raw-json", "-r", action="store_true",
            help="Print raw json result"
        )

        parser.set_defaults(func=self.list)

    def _print_resp(
        self,
        r: LHRPCResponseSchema[RangeQueryResultSchema],
        raw_json: bool
    ):
        if r.result is None:
            r.result = RangeQueryResultSchema(object_ids=[])

        if raw_json:
            print(r.result.json(by_alias=True))
        else:
            if r.result.object_ids is None or len(r.result.object_ids) == 0:
                print("No objects found.")
            else:
                for obj_id in r.result.object_ids:
                    print(obj_id)

    def list(self, ns: Namespace, client: LHClient):
        rt_schema = RESOURCE_TYPES[ns.resource_type]

        r: LHRPCResponseSchema[RangeQueryResultSchema] = client.list_resource(
            rt_schema,
            token=ns.token,
            limit=ns.limit,
        )
        self._print_resp(r, ns.raw_json)

    def range_search(self, ns: Namespace, client: LHClient):
        rt_schema = RESOURCE_TYPES[ns.resource_type]

        r: LHRPCResponseSchema[RangeQueryResultSchema] = client.range_search(
            rt_schema,
            key=ns.key,
            start=ns.start,
            end=ns.end,
            token=ns.token,
            limit=ns.limit,
        )
        self._print_resp(r, ns.raw_json)

    def time_search(self, ns: Namespace, client: LHClient):
        rt_schema = RESOURCE_TYPES[ns.resource_type]

        r: LHRPCResponseSchema[RangeQueryResultSchema] = client.time_search(
            rt_schema,
            start=int(ns.since) if ns.since is not None else None,
            end=int(ns.until) if ns.until is not None else None,
            token=ns.token,
            limit=ns.limit,
        )
        self._print_resp(r, ns.raw_json)

    def key_value_search(self, ns: Namespace, client: LHClient):
        rt_schema = RESOURCE_TYPES[ns.resource_type]

        r: LHRPCResponseSchema[RangeQueryResultSchema] = client.key_value_lookup(
            rt_schema,
            ns.key,
            ns.value,
            token=ns.token,
            limit=ns.limit,
        )
        self._print_resp(r, ns.raw_json)