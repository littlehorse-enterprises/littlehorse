import re
import unittest
from importlib.metadata import requires
from pathlib import Path

from littlehorse.model import service_pb2_grpc

MODEL_DIR = Path(__file__).resolve().parent.parent / "littlehorse" / "model"
PROTOBUF_GENCODE = re.compile(
    r"ValidateProtobufRuntimeVersion\(\s*_runtime_version\.Domain\.PUBLIC,"
    r"\s*(\d+),\s*(\d+),\s*(\d+),"
)


def as_tuple(version: str) -> tuple[int, ...]:
    return tuple(int(part) for part in version.split("."))


def declared_floor(package: str) -> tuple[int, ...]:
    for requirement in requires("littlehorse-client") or []:
        name = re.match(r"[A-Za-z0-9_.-]+", requirement)
        if name and name.group(0).lower() == package:
            floor = re.search(r"(?:>=|==)\s*([0-9][0-9.]*)", requirement)
            if floor:
                return as_tuple(floor.group(1).rstrip("."))
    raise AssertionError(f"littlehorse-client declares no minimum for {package}")


class TestDependencyFloors(unittest.TestCase):
    def test_grpcio_floor_covers_the_generated_stubs(self):
        generated = as_tuple(service_pb2_grpc.GRPC_GENERATED_VERSION)
        self.assertGreaterEqual(declared_floor("grpcio"), generated)

    def test_protobuf_floor_covers_the_generated_modules(self):
        gencode = [
            tuple(int(part) for part in match.groups())
            for path in MODEL_DIR.glob("*_pb2.py")
            for match in [PROTOBUF_GENCODE.search(path.read_text())]
            if match
        ]
        self.assertTrue(gencode, "no generated protobuf modules found")
        self.assertGreaterEqual(declared_floor("protobuf"), max(gencode))
