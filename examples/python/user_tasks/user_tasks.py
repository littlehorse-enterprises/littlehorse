import asyncio
import logging
from pathlib import Path
import random

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, PutUserTaskDefRequest, UserTaskField
from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import WorkflowThread, Workflow
from typing import Any

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


async def get_user_task_def() -> PutUserTaskDefRequest:
    return PutUserTaskDefRequest(
        name="person-details",
        fields=[
            UserTaskField(
                name="PersonDetails",
                description="Person complementary information",
                display_name="Other Details",
                required=True,
                type=VariableType.STR,
            )
        ],
    )


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        task_def_name = "greet"
        user_task_output = wf.declare_str("person-details", None, "writer-group")
        delay_in_seconds = 10
        arg1 = "Sam"
        arg2 = {"identification": "1258796641-4", "Address": "NA-Street", "Age": 28}

        wf.schedule_reminder_task(
            user_task_output, delay_in_seconds, task_def_name, arg1, arg2
        )

    return Workflow("example-wait-for-condition", my_entrypoint)


async def greeting(
    name: str, person_details: dict[str, Any], ctx: WorkerContext
) -> str:
    msg = f"Hello {name}!. WfRun {ctx.wf_run_id.id} Person: {person_details}"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg


async def main() -> None:
    config = get_config()
    wf = get_workflow()
    client = config.stub()

    user_task_def = await get_user_task_def()
    client.PutUserTaskDef(user_task_def)
    littlehorse.create_task_def(greeting, "greet", config)
    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(LHTaskWorker(greeting, "greet", config))


if __name__ == "__main__":
    asyncio.run(main())
