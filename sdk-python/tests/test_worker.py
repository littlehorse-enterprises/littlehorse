from typing import Any
import unittest
from littlehorse.exceptions import TaskSchemaMismatchException
from littlehorse.model.service_pb2 import TaskDefPb

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

        task = LHTask(my_method, TaskDefPb())

        self.assertFalse(task.has_context())

    def test_has_context(self):
        def my_method(param: str, ctx: LHWorkerContext):
            pass

        task = LHTask(my_method, TaskDefPb())

        self.assertTrue(task.has_context())
