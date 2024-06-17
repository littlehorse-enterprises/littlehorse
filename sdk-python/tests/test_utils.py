from datetime import datetime
import json
from random import random
import unittest
from faker import Faker
from littlehorse.exceptions import SerdeException
from littlehorse.model import VariableType, VariableAssignment, VariableValue

from littlehorse.utils import (
    extract_value,
    to_type,
    to_variable_type,
    to_variable_value,
)
from littlehorse.workflow import (
    LHFormatString,
    NodeOutput,
    WfRunVariable,
    to_variable_assignment,
)


class TestProtoUtils(unittest.TestCase):
    faker = Faker()

    def test_extract_value(self):
        # STR
        value = self.faker.word()
        result = extract_value(VariableValue(str=value))
        self.assertEqual(result, value)

        # INT
        value = self.faker.random_int()
        result = extract_value(VariableValue(int=value))
        self.assertEqual(result, value)

        # DOUBLE
        value = random()
        result = extract_value(VariableValue(double=value))
        self.assertEqual(result, value)

        # BOOLEAN
        value = self.faker.boolean()
        result = extract_value(VariableValue(bool=value))
        self.assertEqual(result, value)

        # BYTES
        value = self.faker.binary()
        result = extract_value(VariableValue(bytes=value))
        self.assertEqual(result, value)

        # JSON_OBJ
        input_dict = {"name": self.faker.name(), "income": self.faker.random_int()}
        value = json.dumps(input_dict)
        result = extract_value(VariableValue(json_obj=value))
        self.assertEqual(result, input_dict)

        # JSON_ARR
        input_list = [
            {"name": self.faker.name(), "income": self.faker.random_int()},
            {"name": self.faker.name(), "income": self.faker.random_int()},
        ]
        value = json.dumps(input_list)
        result = extract_value(VariableValue(json_arr=value))
        self.assertEqual(result, input_list)

        # NULL
        result = extract_value(VariableValue())
        self.assertIsNone(result)

    def test_parse_value(self):
        # STR
        value = self.faker.word()
        result = to_variable_value(value)
        self.assertEqual(result, VariableValue(str=value))

        # INT
        value = self.faker.random_int()
        result = to_variable_value(value)
        self.assertEqual(result, VariableValue(int=value))

        value = 0
        result = to_variable_value(value)
        self.assertEqual(result, VariableValue(int=value))

        value = 1
        result = to_variable_value(value)
        self.assertEqual(result, VariableValue(int=value))

        # DOUBLE
        value = random()
        result = to_variable_value(value)
        self.assertEqual(result, VariableValue(double=value))

        # BOOLEAN
        value = self.faker.boolean()
        result = to_variable_value(value)
        self.assertEqual(result, VariableValue(bool=value))

        # BYTES
        value = self.faker.binary()
        result = to_variable_value(value)
        self.assertEqual(result, VariableValue(bytes=value))

        # NULL
        result = to_variable_value(None)
        self.assertEqual(result, VariableValue())

        # JSON_OBJ
        value = {"name": self.faker.name(), "income": self.faker.random_int()}
        result = to_variable_value(value)
        self.assertEqual(
            result,
            VariableValue(json_obj=json.dumps(value)),
        )

        # JSON_ARR
        value = [
            {"name": self.faker.name(), "income": self.faker.random_int()},
            {"name": self.faker.name(), "income": self.faker.random_int()},
        ]
        result = to_variable_value(value)
        self.assertEqual(
            result,
            VariableValue(json_arr=json.dumps(value)),
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
        result = to_variable_value(value)
        self.assertEqual(
            result,
            VariableValue(json_obj=json.dumps(vars(value))),
        )

        value = Shape(
            [
                Point(self.faker.random_int(), self.faker.random_int()),
                Point(self.faker.random_int(), self.faker.random_int()),
            ]
        )
        result = to_variable_value(value)
        self.assertEqual(
            result,
            VariableValue(
                json_obj=json.dumps({"points": [vars(p) for p in value.points]}),
            ),
        )

        # JSON_OBJ (class and dict)
        value = {"point": Point(self.faker.random_int(), self.faker.random_int())}
        result = to_variable_value(value)
        self.assertEqual(
            result,
            VariableValue(
                json_obj=json.dumps({k: vars(v) for k, v in value.items()}),
            ),
        )

        # JSON_ARR (class)
        value = [
            Point(self.faker.random_int(), self.faker.random_int()),
            Point(self.faker.random_int(), self.faker.random_int()),
        ]
        result = to_variable_value(value)
        self.assertEqual(
            result,
            VariableValue(
                json_arr=json.dumps([vars(v) for v in value]),
            ),
        )

    def test_serde_error_when_serializing(self):
        value = datetime.now()

        with self.assertRaises(SerdeException) as exception_context:
            to_variable_value(value)

        self.assertEqual(
            f"Error when serializing value: '{value}' of type '{type(value)}'",
            str(exception_context.exception),
        )

    def test_serde_error_when_deserializing(self):
        variable_value = VariableValue(json_obj='{"timestamp": 79797689')  # not closed

        with self.assertRaises(SerdeException) as exception_context:
            extract_value(variable_value)

        self.assertEqual(
            f"Error when deserializing {variable_value}",
            str(exception_context.exception),
        )

    def test_parse_assignment_variable(self):
        # a literal
        variable = to_variable_assignment(10)
        self.assertEqual(
            variable,
            VariableAssignment(literal_value=VariableValue(int=10)),
        )

        # a NodeOutput
        with self.assertRaises(ValueError) as exception_context:
            to_variable_assignment(NodeOutput(""))

        self.assertEqual(
            "Cannot use NodeOutput directly as input to task. "
            "First save to a WfRunVariable.",
            str(exception_context.exception),
        )

        # a WfRunVariable
        wf_run_variable = WfRunVariable(
            variable_name="my-var-name", variable_type=VariableType.STR
        )
        wf_run_variable.json_path = "$.myPath"
        variable = to_variable_assignment(wf_run_variable)
        self.assertEqual(
            variable,
            VariableAssignment(variable_name="my-var-name", json_path="$.myPath"),
        )

        # a FormatString
        variable = to_variable_assignment(LHFormatString("format {0}", "my-var"))
        self.assertEqual(
            variable,
            VariableAssignment(
                format_string=VariableAssignment.FormatString(
                    format=VariableAssignment(
                        literal_value=VariableValue(str="format {0}")
                    ),
                    args=[
                        VariableAssignment(literal_value=VariableValue(str="my-var"))
                    ],
                )
            ),
        )

    def test_raise_if_not_found_variable_type(self):
        class MyClass:
            pass

        with self.assertRaises(ValueError) as exception_context:
            to_type(MyClass)

        self.assertEqual(
            "VariableType not found",
            str(exception_context.exception),
        )

    def test_raise_if_not_found_type(self):
        class MyClass:
            pass

        with self.assertRaises(ValueError) as exception_context:
            to_variable_type(MyClass)

        self.assertIn(
            "not supported",
            str(exception_context.exception),
        )


if __name__ == "__main__":
    unittest.main()
