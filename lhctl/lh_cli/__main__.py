import argparse
from lh_lib.client import LHClient

from lh_lib.config import DEFAULT_API_URL
from lh_cli.verbs import HANDLERS


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        "lhctl",
        description="LittleHorse CLI Utility"
    )

    parser.add_argument(
        "--api-url", "-u",
        help="URL for LittleHorse API.",
        default=DEFAULT_API_URL,
    )
    subparsers = parser.add_subparsers(dest="verb", required=True)

    for cmd in HANDLERS.keys():
        HANDLERS[cmd].init_subparsers(subparsers)

    ns = parser.parse_args()
    client = LHClient(ns.api_url)
    ns.func(ns, client)
