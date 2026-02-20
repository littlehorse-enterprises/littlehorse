from typing import Any
import unittest
import uuid
import datetime
from unittest.mock import Mock, AsyncMock

from littlehorse.exceptions import TaskSchemaMismatchException
from littlehorse.model import (
    VariableType,
    VariableDef,
    NodeRunId,
    TaskRunId,
    WfRunId,
    ScheduledTask,
    RegisterTaskWorkerResponse,
    TaskDef,
    TaskNodeReference,
    TaskRunSource,
    TypeDefinition,
    UserTaskTriggerReference,
    Checkpoint,
    CheckpointId,
    PutCheckpointResponse,
    VariableValue,
)

from littlehorse.worker import (
    LHTask,
    WorkerContext,
    CheckpointContext,
    LHLivenessController,
    LHTaskWorkerHealth,
    TaskWorkerHealthReason,
)


class TestWorkerContext(unittest.IsolatedAsyncioTestCase):
    def test_idempotency_key(self):
        wf_id = str(uuid.uuid4())
        task_id = str(uuid.uuid4())
        scheduled_task = ScheduledTask(
            task_run_id=TaskRunId(task_guid=task_id, wf_run_id=WfRunId(id=wf_id))
        )
        mock_client = Mock()
        ctx = WorkerContext(scheduled_task, mock_client)
        self.assertEqual(ctx.idempotency_key, f"{task_id}")

    def test_log_output(self):
        mock_client = Mock()
        ctx = WorkerContext(ScheduledTask(), mock_client)
        self.assertEqual(ctx.log_output, "")
        ctx.log("my log 1")
        ctx.log("my log 2")
        ctx.log(Exception("my exception"))
        output = ctx.log_output
        self.assertTrue("my log 1" in output)
        self.assertTrue("my log 2" in output)
        self.assertTrue("my exception" in output)

    def test_get_right_node(self):
        mock_client = Mock()
        wf_id = str(uuid.uuid4())
        node_run_task = NodeRunId(wf_run_id=WfRunId(id=wf_id))
        scheduled_task_task = ScheduledTask(
            source=TaskRunSource(task_node=TaskNodeReference(node_run_id=node_run_task))
        )
        ctx = WorkerContext(scheduled_task_task, mock_client)

        self.assertEqual(ctx.node_run_id, node_run_task)

        wf_id = str(uuid.uuid4())
        node_run_user = NodeRunId(wf_run_id=WfRunId(id=wf_id))
        scheduled_task_user = ScheduledTask(
            source=TaskRunSource(
                user_task_trigger=UserTaskTriggerReference(node_run_id=node_run_user)
            )
        )
        ctx = WorkerContext(scheduled_task_user, mock_client)

        self.assertEqual(ctx.node_run_id, node_run_user)

    async def test_execute_and_checkpoint_saves_new_checkpoint(self):
        mock_client = Mock()
        scheduled_task = ScheduledTask(
            task_run_id=TaskRunId(
                task_guid="mock-guid", wf_run_id=WfRunId(id="mock-id")
            ),
            total_observed_checkpoints=0,
        )

        mock_response = PutCheckpointResponse(
            flow_control_continue_type=PutCheckpointResponse.FlowControlContinue.CONTINUE_TASK
        )
        mock_client.PutCheckpoint = AsyncMock(return_value=mock_response)

        ctx = WorkerContext(scheduled_task, mock_client)

        result = await ctx.execute_and_checkpoint(lambda checkpoint_ctx: "checkpoint_value")

        self.assertEqual(result, "checkpoint_value")
        mock_client.PutCheckpoint.assert_called_once()
        self.assertEqual(ctx._checkpoints_so_far_in_this_run, 1)

    async def test_should_fetch_checkpoint_on_second_checkpoint_attempt(self):
        task_run_id = TaskRunId(wf_run_id=WfRunId(id="mock-id"), task_guid="mock-guid")

        scheduled_task = ScheduledTask(
            task_run_id=task_run_id, total_observed_checkpoints=1
        )

        mock_client = Mock()
        mock_checkpoint = Checkpoint(
            id=CheckpointId(task_run=task_run_id, checkpoint_number=1),
            value=VariableValue(str="checkpoint_value"),
        )
        mock_client.GetCheckpoint = AsyncMock(return_value=mock_checkpoint)
        mock_client.PutCheckpoint = AsyncMock()

        ctx = WorkerContext(scheduled_task, mock_client)

        checkpoint_data = await ctx.execute_and_checkpoint(
            lambda checkpoint_ctx: "checkpoint_value"
        )

        mock_client.PutCheckpoint.assert_not_called()
        mock_client.GetCheckpoint.assert_called_once()
        self.assertEqual(checkpoint_data, "checkpoint_value")

    async def test_save_checkpoint_halts_on_server_instruction(self):
        mock_client = Mock()
        scheduled_task = ScheduledTask(
            task_run_id=TaskRunId(task_guid="mock-guid"), total_observed_checkpoints=0
        )

        mock_response = PutCheckpointResponse(
            flow_control_continue_type=PutCheckpointResponse.FlowControlContinue.STOP_TASK
        )
        mock_client.PutCheckpoint = AsyncMock(return_value=mock_response)

        ctx = WorkerContext(scheduled_task, mock_client)

        with self.assertRaises(Exception) as exception_context:
            await ctx.execute_and_checkpoint(lambda ctx: "checkpoint_value")

        self.assertEqual(
            "Halting execution because the server told us to.",
            str(exception_context.exception),
        )


class TestCheckpointContext(unittest.TestCase):
    def test_log_output(self):
        ctx = CheckpointContext()
        self.assertEqual(ctx.log_output, "")

        ctx.log("checkpoint log 1")
        ctx.log("checkpoint log 2")
        ctx.log(67)

        output = ctx.log_output
        self.assertIn("checkpoint log 1", output)
        self.assertIn("checkpoint log 2", output)
        self.assertIn("67", output)


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
            TaskDef(
                input_vars=[
                    VariableDef(
                        name="param",
                        type_def=TypeDefinition(
                            primitive_type=VariableType.STR, masked=False
                        ),
                    )
                ]
            ),
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
                VariableDef(
                    name="paramA",
                    type_def=TypeDefinition(
                        primitive_type=VariableType.STR, masked=False
                    ),
                ),
                VariableDef(
                    name="paramB",
                    type_def=TypeDefinition(
                        primitive_type=VariableType.INT, masked=False
                    ),
                ),
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
                VariableDef(
                    name="param2",
                    type_def=TypeDefinition(
                        primitive_type=VariableType.INT, masked=False
                    ),
                ),
                VariableDef(
                    name="param1",
                    type_def=TypeDefinition(
                        primitive_type=VariableType.STR, masked=False
                    ),
                ),
            ]
        )

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, task_def)

        self.assertEqual(
            "Parameter types do not match, expected: [<class 'int'>, <class 'str'>] got: [<class 'str'>, <class 'int'>]",
            str(exception_context.exception),
        )

    def test_raise_error_if_wrong_callable_order(self):
        async def my_method(param1: int, param2: str, ctx: WorkerContext):
            pass

        task_def = TaskDef(
            input_vars=[
                VariableDef(
                    name="param1",
                    type_def=TypeDefinition(
                        primitive_type=VariableType.STR, masked=False
                    ),
                ),
                VariableDef(
                    name="param2",
                    type_def=TypeDefinition(
                        primitive_type=VariableType.INT, masked=False
                    ),
                ),
            ]
        )

        with self.assertRaises(TaskSchemaMismatchException) as exception_context:
            LHTask(my_method, task_def)

        self.assertEqual(
            "Parameter types do not match, expected: [<class 'str'>, <class 'int'>] got: [<class 'int'>, <class 'str'>]",
            str(exception_context.exception),
        )

    def test_raise_exception_if_callable_does_not_match_with_task_def(self):
        async def my_method(param: str):
            pass

        task_def = TaskDef(
            input_vars=[
                VariableDef(
                    name="param1",
                    type_def=TypeDefinition(
                        primitive_type=VariableType.STR, masked=False
                    ),
                ),
                VariableDef(
                    name="param2",
                    type_def=TypeDefinition(
                        primitive_type=VariableType.INT, masked=False
                    ),
                ),
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
                input_vars=[
                    VariableDef(
                        name="param",
                        type_def=TypeDefinition(
                            primitive_type=variable_type, masked=False
                        ),
                    )
                ]
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
            VariableType.TIMESTAMP: str,
        }.items():
            test(variable_type, callable_type)

    def test_callable_matches(self):
        def test(variable_type, callable_type):
            async def my_method(param: callable_type):
                pass

            task_def = TaskDef(
                input_vars=[
                    VariableDef(
                        name="param",
                        type_def=TypeDefinition(
                            primitive_type=variable_type, masked=False
                        ),
                    ),
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
            VariableType.TIMESTAMP: type(datetime.datetime),
        }.items():
            test(variable_type, callable_type)


class TestLHLivenessController(unittest.TestCase):
    def test_keep_running_when_no_failure_detected(self):
        controller = LHLivenessController()
        self.assertTrue(controller.keep_worker_running)

    def test_get_health(self):
        controller = LHLivenessController()

        self.assertEqual(
            controller.health(),
            LHTaskWorkerHealth(True, TaskWorkerHealthReason.HEALTHY),
        )

        controller.notify_worker_failure()

        self.assertEqual(
            controller.health(),
            LHTaskWorkerHealth(False, TaskWorkerHealthReason.UNHEALTHY),
        )

        controller.notify_success_call(
            RegisterTaskWorkerResponse(is_cluster_healthy=True)
        )

        self.assertEqual(
            controller.health(),
            LHTaskWorkerHealth(True, TaskWorkerHealthReason.HEALTHY),
        )

        controller.notify_success_call(
            RegisterTaskWorkerResponse(is_cluster_healthy=False)
        )

        self.assertEqual(
            controller.health(),
            LHTaskWorkerHealth(False, TaskWorkerHealthReason.SERVER_REBALANCING),
        )


if __name__ == "__main__":
    unittest.main()
