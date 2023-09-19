import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import (
    IndexType,
    Comparator,
    VariableMutationType,
)
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import ThreadBuilder, Workflow

logging.basicConfig(level=logging.INFO)

TASK_NAME = "task-with-vars"


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def one_approval(approval_thread: ThreadBuilder) -> None:
        approval = approval_thread.add_variable("INPUT", VariableType.JSON_OBJ)
        is_approved = (
            approval_thread.add_variable("did-person-approve", VariableType.BOOL)
            .with_index(IndexType.LOCAL_INDEX)
            .persistent()
        )

        def is_user_group(user_group_thread: ThreadBuilder) -> None:
            user_task_output = approval_thread.assign_user_task(
                "approve-task",
                user_id=None,
                user_group=approval.with_json_path("$.userGroup"),
            )
            user_group_thread.mutate(
                is_approved,
                VariableMutationType.ASSIGN,
                user_task_output.with_json_path("$.isApproved"),
            )

        def is_user(user_thread: ThreadBuilder) -> None:
            user_task_output = approval_thread.assign_user_task(
                "approve-task",
                user_id=approval.with_json_path("$.userId"),
                user_group=approval.with_json_path("$.userGroup"),
            )
            user_thread.mutate(
                is_approved,
                VariableMutationType.ASSIGN,
                user_task_output.with_json_path("$.isApproved"),
            )

        condition = approval_thread.condition(
            approval.with_json_path("$.userId"), Comparator.EQUALS, None
        )
        approval_thread.do_if(
            condition=condition, if_body=is_user_group, else_body=is_user
        )

    def my_entrypoint(thread: ThreadBuilder) -> None:
        # it receives a name
        approvals_var = thread.add_variable("approvals", VariableType.JSON_ARR)
        (
            thread.add_variable("item-url", VariableType.STR)
            .with_index(IndexType.REMOTE_INDEX)
            .persistent()
        )
        (
            thread.add_variable("status", VariableType.STR, "PENDING")
            .with_index(IndexType.LOCAL_INDEX)
            .persistent()
        )
        spawned_thread_1 = thread.spawn_thread_for_each(
            approvals_var, one_approval, "approval"
        )
        thread.wait_for_threads(spawned_thread_1)
        thread.execute(TASK_NAME, "var")

    return Workflow("example-wait-for-threads-v9", my_entrypoint)


async def task(arg1: str) -> None:
    print("This is an example var " + arg1)


async def daily_bugle(news: str) -> None:
    print(news)


async def main() -> None:
    config = get_config()
    wf = get_workflow()

    littlehorse.create_task_def(task, TASK_NAME, config)
    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(
        LHTaskWorker(task, TASK_NAME, config),
    )


if __name__ == "__main__":
    asyncio.run(main())
