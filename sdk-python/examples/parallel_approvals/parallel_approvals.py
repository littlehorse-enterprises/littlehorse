import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import (
    VariableType,
    Comparator,
    VariableMutationType,
)
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def one_approval(approval_thread: WorkflowThread) -> None:
        approval = approval_thread.add_variable("INPUT", VariableType.JSON_OBJ)
        is_approved = approval_thread.add_variable(
            "did-person-approve", VariableType.BOOL
        ).searchable()

        def is_user_group(user_group_thread: WorkflowThread) -> None:
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

        def is_user(user_thread: WorkflowThread) -> None:
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
        if_statement = approval_thread.do_if(condition, is_user_group)
        if_statement.do_else(is_user)

    def my_entrypoint(wf: WorkflowThread) -> None:
        approvals_var = wf.add_variable("approvals", VariableType.JSON_ARR)
        (wf.add_variable("item-url", VariableType.STR).searchable())
        (wf.add_variable("status", VariableType.STR, "PENDING").searchable())
        spawned_thread_1 = wf.spawn_thread_for_each(
            approvals_var, one_approval, "approval"
        )
        wf.wait_for_threads(spawned_thread_1)

    return Workflow("parallel-approvals-v2", my_entrypoint)


async def main() -> None:
    config = get_config()
    wf = get_workflow()
    littlehorse.create_workflow_spec(wf, config)


if __name__ == "__main__":
    asyncio.run(main())
