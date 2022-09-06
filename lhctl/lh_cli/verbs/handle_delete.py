from argparse import ArgumentParser, _SubParsersAction, Namespace

from lh_lib.client import LHClient
from lh_lib.schema import RESOURCE_TYPES


class DELETEHandler():
    def __init__(self):
        pass

    def init_subparsers(self, base_subparsers: _SubParsersAction):
        parser: ArgumentParser = base_subparsers.add_parser(
            "delete",
            help="Delete a resource"
        )
        parser.add_argument(
            "resource_type",
            choices=[k for k in RESOURCE_TYPES.keys()],
            help="Resource Type to Delete."
        )
        parser.add_argument(
            "resource_id",
            help="Specific Id of resource to delete (deletion-by-name not supported)."
        )
        parser.set_defaults(func=self.delete)

    def delete(self, ns: Namespace, client: LHClient):
        resource_type = RESOURCE_TYPES[ns.resource_type]
        client.delete_resource_by_id(resource_type, ns.resource_id)
        print(f"Successfully deleted {ns.resource_type} {ns.resource_id}.")
