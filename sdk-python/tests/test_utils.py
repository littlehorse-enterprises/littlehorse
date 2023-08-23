import json
from random import random
import unittest
from faker import Faker
from littlehorse.model.service_pb2 import VariableTypePb, VariableValuePb

from littlehorse.utils import extract_value, parse_value


class TestUtils(unittest.TestCase):
    faker = Faker()

    def test_extract_value(self):
        # STR
        value = self.faker.word()
        result = extract_value(VariableValuePb(type=VariableTypePb.STR, str=value))
        self.assertEqual(result, value)

        # INT
        value = self.faker.random_int()
        result = extract_value(VariableValuePb(type=VariableTypePb.INT, int=value))
        self.assertEqual(result, value)

        # DOUBLE
        value = random()
        result = extract_value(
            VariableValuePb(type=VariableTypePb.DOUBLE, double=value)
        )
        self.assertEqual(result, value)

        # BOOLEAN
        value = self.faker.boolean()
        result = extract_value(VariableValuePb(type=VariableTypePb.BOOL, bool=value))
        self.assertEqual(result, value)

        # BYTES
        value = self.faker.binary()
        result = extract_value(VariableValuePb(type=VariableTypePb.BYTES, bytes=value))
        self.assertEqual(result, value)

        # JSON_OBJ
        input_dict = {"name": self.faker.name(), "income": self.faker.random_int()}
        value = json.dumps(input_dict)
        result = extract_value(
            VariableValuePb(type=VariableTypePb.JSON_OBJ, json_obj=value)
        )
        self.assertEqual(result, input_dict)

        # JSON_ARR
        input_list = [
            {"name": self.faker.name(), "income": self.faker.random_int()},
            {"name": self.faker.name(), "income": self.faker.random_int()},
        ]
        value = json.dumps(input_list)
        result = extract_value(
            VariableValuePb(type=VariableTypePb.JSON_ARR, json_arr=value)
        )
        self.assertEqual(result, input_list)

        # NULL
        result = extract_value(VariableValuePb(type=VariableTypePb.NULL))
        self.assertIsNone(result)

    def test_parse_value(self):
        # STR
        value = self.faker.word()
        result = parse_value(value)
        self.assertEqual(result, VariableValuePb(type=VariableTypePb.STR, str=value))

        # INT
        value = self.faker.random_int()
        result = parse_value(value)
        self.assertEqual(result, VariableValuePb(type=VariableTypePb.INT, int=value))

        value = 0
        result = parse_value(value)
        self.assertEqual(result, VariableValuePb(type=VariableTypePb.INT, int=value))

        value = 1
        result = parse_value(value)
        self.assertEqual(result, VariableValuePb(type=VariableTypePb.INT, int=value))

        # DOUBLE
        value = random()
        result = parse_value(value)
        self.assertEqual(
            result, VariableValuePb(type=VariableTypePb.DOUBLE, double=value)
        )

        # BOOLEAN
        value = self.faker.boolean()
        result = parse_value(value)
        self.assertEqual(result, VariableValuePb(type=VariableTypePb.BOOL, bool=value))

        # BYTES
        value = self.faker.binary()
        result = parse_value(value)
        self.assertEqual(
            result, VariableValuePb(type=VariableTypePb.BYTES, bytes=value)
        )

        # NULL
        result = parse_value(None)
        self.assertEqual(result, VariableValuePb(type=VariableTypePb.NULL))

        # JSON_OBJ
        value = {"name": self.faker.name(), "income": self.faker.random_int()}
        result = parse_value(value)
        self.assertEqual(
            result,
            VariableValuePb(type=VariableTypePb.JSON_OBJ, json_obj=json.dumps(value)),
        )

        # JSON_ARR
        value = [
            {"name": self.faker.name(), "income": self.faker.random_int()},
            {"name": self.faker.name(), "income": self.faker.random_int()},
        ]
        result = parse_value(value)
        self.assertEqual(
            result,
            VariableValuePb(type=VariableTypePb.JSON_ARR, json_arr=json.dumps(value)),
        )

        # JSON_OBJ (class)
        class Shape:
            def __init__(self, points):
                self.points = points

        class Point:
            def __init__(self, x, y):
                self.x = x
                self.y = y

        value = Point(self.faker.random_int(), self.faker.random_int())
        result = parse_value(value)
        self.assertEqual(
            result,
            VariableValuePb(
                type=VariableTypePb.JSON_OBJ, json_obj=json.dumps(vars(value))
            ),
        )

        value = Shape(
            [
                Point(self.faker.random_int(), self.faker.random_int()),
                Point(self.faker.random_int(), self.faker.random_int()),
            ]
        )
        result = parse_value(value)
        self.assertEqual(
            result,
            VariableValuePb(
                type=VariableTypePb.JSON_OBJ,
                json_obj=json.dumps({"points": [vars(p) for p in value.points]}),
            ),
        )

        # JSON_OBJ (class and dict)
        value = {"point": Point(self.faker.random_int(), self.faker.random_int())}
        result = parse_value(value)
        self.assertEqual(
            result,
            VariableValuePb(
                type=VariableTypePb.JSON_OBJ,
                json_obj=json.dumps({k: vars(v) for k, v in value.items()}),
            ),
        )

        # JSON_ARR (class)
        value = [
            Point(self.faker.random_int(), self.faker.random_int()),
            Point(self.faker.random_int(), self.faker.random_int()),
        ]
        result = parse_value(value)
        self.assertEqual(
            result,
            VariableValuePb(
                type=VariableTypePb.JSON_ARR,
                json_arr=json.dumps([vars(v) for v in value]),
            ),
        )


if __name__ == "__main__":
    unittest.main()
