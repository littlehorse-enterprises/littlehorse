from typing import Any
import unittest
import uuid
import time
from littlehorse.exceptions import TaskSchemaMismatchException
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import VariableDef
from littlehorse.model.object_id_pb2 import NodeRunId, TaskRunId
from littlehorse.model.service_pb2 import ScheduledTask
from littlehorse.model.task_def_pb2 import TaskDef
from littlehorse.model.task_run_pb2 import TaskNodeReference, TaskRunSource
from littlehorse.model.user_tasks_pb2 import UserTaskTriggerReference
from littlehorse.model.service_pb2 import RegisterTaskWorkerResponse


from littlehorse.worker import LHTask, WorkerContext, LHLivenessController


class TestWorkerContext(unittest.TestCase):
    def test_idempotency_key(self):
        wf_id = str(uuid.uuid4())
        task_id = str(uuid.uuid4())
        scheduled_task = ScheduledTask(
            task_run_id=TaskRunId(task_guid=task_id, wf_run_id=wf_id)
        )
        ctx = WorkerContext(scheduled_task)
        self.assertEqual(ctx.idempotency_key, f"{task_id}")

    def test_log_output(self):
        ctx = WorkerContext(ScheduledTask())
        self.assertEqual(ctx.log_output, "")
        ctx.log("my log 1")
        ctx.log("my log 2")
        ctx.log(Exception("my exception"))
        output = ctx.log_output
        self.assertTrue("my log 1" in output)
        self.assertTrue("my log 2" in output)
        self.assertTrue("my exception" in output)

    def test_get_right_node(self):
        wf_id = str(uuid.uuid4())
        node_run_task = NodeRunId(wf_run_id=wf_id)
        scheduled_task_task = ScheduledTask(
            source=TaskRunSource(task_node=TaskNodeReference(node_run_id=node_run_task))
        )
        ctx = WorkerContext(scheduled_task_task)

        self.assertEqual(ctx.node_run_id, node_run_task)

        wf_id = str(uuid.uuid4())
        node_run_user = NodeRunId(wf_run_id=wf_id)
        scheduled_task_user = ScheduledTask(
            source=TaskRunSource(
                user_task_trigger=UserTaskTriggerReference(node_run_id=node_run_user)
            )
        )
        ctx = WorkerContext(scheduled_task_user)

        self.assertEqual(ctx.node_run_id, node_run_user)


class TestLHTask(unittest.TestCase):
    def test_raise_exception_if_it_is_not_a_callable(self):
        not_a_callable = 3
        with self.assertRaises(TypeError) as exception_context:
            LHTask(not_a_callable, TaskDef())

        self.assertEqual(
            f"{not_a_callable} is not a callable object",
            str(exception_context.exception),
        )

    def test_raise_exception_if_contexts_is_not_the_last_param(self):
        async def my_method(ctx: WorkerContext, param: str):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDef())

        self.assertEqual(
            "The WorkerContext should be the last parameter",
            str(exception_context.exception),
        )

    def test_raise_exception_if_it_is_not_a_coroutine(self):
        def my_method():
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDef())

        self.assertEqual(
            "Is not a coroutine function",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_more_than_one_contexts(self):
        async def my_method(ctx1: WorkerContext, ctx2: WorkerContext):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDef())

        self.assertEqual(
            "Too many context arguments (expected 1): ['ctx1', 'ctx2']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_params_without_annotation(self):
        async def my_method(param1: int, param2):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDef())

        self.assertEqual(
            "Not annotated parameters found: ['param2']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_any(self):
        async def my_method(param: Any):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDef())

        self.assertEqual(
            "Any is not allowed: ['param']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_positional_args(self):
        async def my_method(*param):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDef())

        self.assertEqual(
            "Positional parameters (*args) not allowed: ['param']",
            str(exception_context.exception),
        )

    def test_raise_exception_if_there_are_keyword_args(self):
        async def my_method(**param):
            pass

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, TaskDef())

        self.assertEqual(
            "Keyword parameters (*kwargs) not allowed: ['param']",
            str(exception_context.exception),
        )

    def test_has_no_context(self):
        async def my_method(param: str):
            pass

        task = LHTask(
            my_method,
            TaskDef(input_vars=[VariableDef(name="param", type=VariableType.STR)]),
        )

        self.assertFalse(task.has_context())

    def test_has_no_context_in_empty_parameters(self):
        async def my_method():
            pass

        task = LHTask(
            my_method,
            TaskDef(),
        )

        self.assertFalse(task.has_context())

    def test_has_context(self):
        async def my_method(ctx: WorkerContext):
            pass

        task = LHTask(my_method, TaskDef())

        self.assertTrue(task.has_context())

    def test_with_class(self):
        class MyClass:
            async def my_method(self, ctx: WorkerContext):
                pass

            def create_task(self):
                return LHTask(self.my_method, TaskDef())

        try:
            my_class = MyClass()
            my_class.create_task()
        except Exception as e:
            self.fail(f"Unexpected exception {e}")

    def test_callable_matches_with_context(self):
        async def my_method(param1: str, param2: int, ctx: WorkerContext = None):
            pass

        task_def = TaskDef(
            input_vars=[
                VariableDef(name="paramA", type=VariableType.STR),
                VariableDef(name="paramB", type=VariableType.INT),
            ]
        )

        try:
            LHTask(my_method, task_def)
        except Exception as e:
            self.fail(f"Unexpected exception {e}")

    def test_raise_error_if_wrong_order(self):
        async def my_method(param1: str, param2: int, ctx: WorkerContext):
            pass

        task_def = TaskDef(
            input_vars=[
                VariableDef(name="param2", type=VariableType.INT),
                VariableDef(name="param1", type=VariableType.STR),
            ]
        )

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, task_def)

        self.assertEqual(
            "Parameter types do not match, expected: [<class 'int'>, <class 'str'>]",
            str(exception_context.exception),
        )

    def test_raise_error_if_wrong_callable_order(self):
        async def my_method(param1: int, param2: str, ctx: WorkerContext):
            pass

        task_def = TaskDef(
            input_vars=[
                VariableDef(name="param1", type=VariableType.STR),
                VariableDef(name="param2", type=VariableType.INT),
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

        task_def = TaskDef(
            input_vars=[
                VariableDef(name="param1", type=VariableType.STR),
                VariableDef(name="param2", type=VariableType.INT),
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

            task_def = TaskDef(
                input_vars=[VariableDef(name="param", type=variable_type)]
            )

            with self.assertRaises(TaskSchemaMismatchException):
                LHTask(my_method, task_def)

        for variable_type, callable_type in {
            VariableType.JSON_OBJ: dict,
            VariableType.JSON_ARR: list,
            VariableType.DOUBLE: str,
            VariableType.BOOL: str,
            VariableType.STR: int,
            VariableType.INT: str,
            VariableType.BYTES: str,
        }.items():
            test(variable_type, callable_type)

    def test_callable_matches(self):
        def test(variable_type, callable_type):
            async def my_method(param: callable_type):
                pass

            task_def = TaskDef(
                input_vars=[
                    VariableDef(name="param", type=variable_type),
                ]
            )

            try:
                LHTask(my_method, task_def)
            except Exception as e:
                self.fail(f"Unexpected exception {e}")

        for variable_type, callable_type in {
            VariableType.JSON_OBJ: dict[str, Any],
            VariableType.JSON_ARR: list[Any],
            VariableType.DOUBLE: float,
            VariableType.BOOL: bool,
            VariableType.STR: str,
            VariableType.INT: int,
            VariableType.BYTES: bytes,
        }.items():
            test(variable_type, callable_type)


class TestLHLivenessController(unittest.TestCase):
    def test_keep_running_when_no_failure_detected(self):
        controller = LHLivenessController(100)
        self.assertTrue(controller.keep_worker_running())

    def test_stop_running_after_timeout(self):
        controller = LHLivenessController(100)
        controller.notify_call_failure()
        self.assertTrue(controller.keep_worker_running())
        time.sleep(150 / 1000)
        self.assertFalse(controller.keep_worker_running())

    def test_recover_from_failure(self):
        controller = LHLivenessController(100)
        controller.notify_call_failure()
        controller.notify_success_call(RegisterTaskWorkerResponse())
        time.sleep(150 / 1000)
        self.assertTrue(controller.keep_worker_running())

    def test_keep_running_on_server_unhealthy(self):
        reply = RegisterTaskWorkerResponse(is_cluster_healthy=False)
        controller = LHLivenessController(100)
        controller.notify_success_call(reply)
        time.sleep(150 / 1000)
        self.assertTrue(controller.keep_worker_running())


if __name__ == "__main__":
    unittest.main()
