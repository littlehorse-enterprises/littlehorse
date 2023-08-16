from typing import Any
import unittest
from littlehorse.exceptions import TaskSchemaMismatchException
from littlehorse.model.service_pb2 import TaskDefPb, VariableDefPb, VariableTypePb

from littlehorse.worker import LHTask, LHWorkerContext


class TestLHTask(unittest.TestCase):
    def test_raise_exception_if_it_is_not_a_callable(self):
        not_a_callable = 3
        with self.assertRaises(TypeError) as exception_context:
            LHTask(not_a_callable, TaskDefPb())
        self.assertEqual(
            f"{not_a_callable} is not a callable object",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_more_than_one_contexts(self):
        def my_method(ctx1: LHWorkerContext, ctx2: LHWorkerContext):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Too many context arguments (expected 1): ['ctx1', 'ctx2']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_params_without_annotation(self):
        def my_method(param1: int, param2):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Not annotated parameters found: ['param2']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_any(self):
        def my_method(param: Any):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Any is not allowed: ['param']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_positional_args(self):
        def my_method(*param):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Positional parameters (*args) not allowed: ['param']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_keyword_args(self):
        def my_method(**param):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Keyword parameters (*kwargs) not allowed: ['param']",
            str(exception_context.exception),
        )

    def test_has_no_context(self):
        def my_method(param: str):
            pass

        task = LHTask(
            my_method,
            TaskDefPb(
                input_vars=[VariableDefPb(name="param", type=VariableTypePb.STR)]
            ),
        )

        self.assertFalse(task.has_context())

    def test_has_context(self):
        def my_method(ctx: LHWorkerContext):
            pass

        task = LHTask(my_method, TaskDefPb())

        self.assertTrue(task.has_context())

    def test_callable_matches_with_context(self):
        def my_method(param1: str, param2: int, ctx: LHWorkerContext = None):
            pass

        task_def = TaskDefPb(
            input_vars=[
                VariableDefPb(name="param1", type=VariableTypePb.STR),
                VariableDefPb(name="param2", type=VariableTypePb.INT),
            ]
        )

        try:
            LHTask(my_method, task_def)
        except Exception as e:
            self.fail(f"Unexpected exception {e}")

    def test_callable_matches_with_context_and_any_order(self):
        def my_method(
            param3: dict[str, Any],
            param1: str,
            param2: int,
            ctx: LHWorkerContext,
            param4: list[Any],
        ):
            pass

        task_def = TaskDefPb(
            input_vars=[
                VariableDefPb(name="param2", type=VariableTypePb.INT),
                VariableDefPb(name="param1", type=VariableTypePb.STR),
                VariableDefPb(name="param4", type=VariableTypePb.JSON_ARR),
                VariableDefPb(name="param3", type=VariableTypePb.JSON_OBJ),
            ]
        )

        try:
            LHTask(my_method, task_def)
        except Exception as e:
            self.fail(f"Unexpected exception {e}")

    def test_raise_exception_if_callable_does_not_match_with_task_def(self):
        def my_method(param: str):
            pass

        task_def = TaskDefPb(
            input_vars=[
                VariableDefPb(name="param1", type=VariableTypePb.STR),
                VariableDefPb(name="param2", type=VariableTypePb.INT),
            ]
        )

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, task_def)

        self.assertEqual(
            "Parameters do not match, expected: "
            + "{'param1': <class 'str'>, 'param2': <class 'int'>}, "
            + "and was: {'param': <class 'str'>}",
            str(exception_context.exception),
        )

    def test_raise_exception_if_callable_does_not_match(self):
        def test(variable_type, callable_type):
            def my_method(param: callable_type):
                pass

            task_def = TaskDefPb(
                input_vars=[VariableDefPb(name="param", type=variable_type)]
            )

            with self.assertRaises(TaskSchemaMismatchException):
                LHTask(my_method, task_def)

        for variable_type, callable_type in {
            VariableTypePb.JSON_OBJ: str,
            VariableTypePb.JSON_ARR: str,
            VariableTypePb.DOUBLE: str,
            VariableTypePb.BOOL: str,
            VariableTypePb.STR: int,
            VariableTypePb.INT: str,
            VariableTypePb.BYTES: str,
        }.items():
            test(variable_type, callable_type)

    def test_callable_matches(self):
        def test(variable_type, callable_type):
            def my_method(param: callable_type):
                pass

            task_def = TaskDefPb(
                input_vars=[
                    VariableDefPb(name="param", type=variable_type),
                ]
            )

            try:
                LHTask(my_method, task_def)
            except Exception as e:
                self.fail(f"Unexpected exception {e}")

        for variable_type, callable_type in {
            VariableTypePb.JSON_OBJ: dict[str, Any],
            VariableTypePb.JSON_ARR: list[Any],
            VariableTypePb.DOUBLE: float,
            VariableTypePb.BOOL: bool,
            VariableTypePb.STR: str,
            VariableTypePb.INT: int,
            VariableTypePb.BYTES: bytes,
        }.items():
            test(variable_type, callable_type)
