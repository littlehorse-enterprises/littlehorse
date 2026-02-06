from collections import deque
from datetime import datetime
import json
from inspect import signature
from random import random
import unittest
from typing import Annotated

from faker import Faker
from littlehorse.exceptions import SerdeException
from littlehorse.model import VariableType, VariableAssignment, VariableValue, WfRunId

from littlehorse.utils import (
    extract_value,
    to_type,
    to_variable_type,
    to_variable_value,
)
from littlehorse.worker import LHType
from littlehorse.workflow import (
    LHFormatString,
    NodeOutput,
    WfRunVariable,
    to_variable_assignment,
    WorkflowThread,
    Workflow,
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

        value = datetime.now()
        result = extract_value(VariableValue(utc_timestamp=value))
        self.assertEqual(value, result)

        value = WfRunId(id=self.faker.uuid4())
        result = extract_value(VariableValue(wf_run_id=value))
        self.assertEqual(value, result)

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

        # TIMESTAMP
        value = datetime.now()
        result = to_variable_value(value)
        self.assertEqual(result, VariableValue(utc_timestamp=value))

        # WF_RUN_ID
        value = WfRunId(id=self.faker.uuid4())
        result = to_variable_value(value)
        self.assertEqual(result, VariableValue(wf_run_id=value))

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
        value = deque([1, 2, 3])

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
        node_output = NodeOutput("some-node")
        self.assertEqual(
            to_variable_assignment(node_output).node_output.node_name,
            "some-node",
        )

        # a NodeOutput with jsonpath
        node_output = node_output.with_json_path("$.asdf")
        var_assn = to_variable_assignment(node_output)
        self.assertEqual(var_assn.node_output.node_name, "some-node")
        self.assertEqual(var_assn.json_path, "$.asdf")

        def entrypoint_func(wf: WorkflowThread) -> None:
            pass

        workflow = Workflow("test-workflow", entrypoint_func)
        workflow_thread = WorkflowThread(workflow, entrypoint_func)

        wf_run_variable = WfRunVariable(
            variable_name="my-var-name",
            variable_type=VariableType.STR,
            parent=workflow_thread,
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

    def test_annotated_parameter_type(self):
        def greeting(name: Annotated[str, LHType(name="test")]):
            print(name)

        param = list(signature(greeting).parameters.values())[0]
        self.assertEqual(
            to_variable_type(param.annotation),
            VariableType.STR,
        )


if __name__ == "__main__":
    unittest.main()
