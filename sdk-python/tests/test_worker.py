from typing import Any
import unittest
from littlehorse.exceptions import TaskSchemaMismatchException
from littlehorse.model.service_pb2 import TaskDefPb, VariableDefPb, VariableTypePb

from littlehorse.worker import LHTask, LHWorkerContext


class LHTaskExecutorTest(unittest.TestCase):
    def test_raise_exception_if_it_is_not_a_callable(self):
        not_a_callable = 3
        with self.assertRaises(TypeError) as exception_context:
            LHTask(not_a_callable, TaskDefPb())
        self.assertEqual(
            f"{not_a_callable} is not a callable object",
            str(exception_context.exception),
        )

    def test_raise_exception_if_contexts_is_not_the_last_param(self):
        async def my_method(ctx: LHWorkerContext, param: str):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "The WorkerContext should be the last parameter",
            str(exception_context.exception),
        )

    def test_raise_exception_if_it_is_not_a_coroutine(self):
        def my_method():
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Is not a coroutine function",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_more_than_one_contexts(self):
        async def my_method(ctx1: LHWorkerContext, ctx2: LHWorkerContext):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Too many context arguments (expected 1): ['ctx1', 'ctx2']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_params_without_annotation(self):
        async def my_method(param1: int, param2):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Not annotated parameters found: ['param2']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_any(self):
        async def my_method(param: Any):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Any is not allowed: ['param']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_positional_args(self):
        async def my_method(*param):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Positional parameters (*args) not allowed: ['param']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_keyword_args(self):
        async def my_method(**param):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDefPb())

        self.assertEqual(
            "Keyword parameters (*kwargs) not allowed: ['param']",
            str(exception_context.exception),
        )

    def test_has_no_context(self):
        async def my_method(param: str):
            pass

        task = LHTask(
            my_method,
            TaskDefPb(
                input_vars=[VariableDefPb(name="param", type=VariableTypePb.STR)]
            ),
        )

        self.assertFalse(task.has_context())

    def test_has_context(self):
        async def my_method(ctx: LHWorkerContext):
            pass

        task = LHTask(my_method, TaskDefPb())

        self.assertTrue(task.has_context())

    def test_callable_matches_with_context(self):
        async def my_method(param1: str, param2: int, ctx: LHWorkerContext = None):
            pass

        task_def = TaskDefPb(
            input_vars=[
                VariableDefPb(name="paramA", type=VariableTypePb.STR),
                VariableDefPb(name="paramB", type=VariableTypePb.INT),
            ]
        )

        try:
            LHTask(my_method, task_def)
        except Exception as e:
            self.fail(f"Unexpected exception {e}")

    def test_raise_error_if_wrong_order(self):
        async def my_method(param1: str, param2: int, ctx: LHWorkerContext):
            pass

        task_def = TaskDefPb(
            input_vars=[
                VariableDefPb(name="param2", type=VariableTypePb.INT),
                VariableDefPb(name="param1", type=VariableTypePb.STR),
            ]
        )

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, task_def)

        self.assertEqual(
            "Parameter types do not match, expected: [<class 'int'>, <class 'str'>]",
            str(exception_context.exception),
        )

    def test_raise_error_if_wrong_callable_order(self):
        async def my_method(param1: int, param2: str, ctx: LHWorkerContext):
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
            "Parameter types do not match, expected: [<class 'str'>, <class 'int'>]",
            str(exception_context.exception),
        )

    def test_raise_exception_if_callable_does_not_match_with_task_def(self):
        async def my_method(param: str):
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
            "Incorrect parameter list, expected: [<class 'str'>, <class 'int'>]",
            str(exception_context.exception),
        )

    def test_raise_exception_if_callable_does_not_match(self):
        def test(variable_type, callable_type):
            async def my_method(param: callable_type):
                pass

            task_def = TaskDefPb(
                input_vars=[VariableDefPb(name="param", type=variable_type)]
            )

            with self.assertRaises(TaskSchemaMismatchException):
                LHTask(my_method, task_def)

        for variable_type, callable_type in {
            VariableTypePb.JSON_OBJ: dict,
            VariableTypePb.JSON_ARR: list,
            VariableTypePb.DOUBLE: str,
            VariableTypePb.BOOL: str,
            VariableTypePb.STR: int,
            VariableTypePb.INT: str,
            VariableTypePb.BYTES: str,
        }.items():
            test(variable_type, callable_type)

    def test_callable_matches(self):
        def test(variable_type, callable_type):
            async def my_method(param: callable_type):
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
