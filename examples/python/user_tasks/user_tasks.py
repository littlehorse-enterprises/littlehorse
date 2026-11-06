import asyncio
import logging
from pathlib import Path
from typing import Annotated

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.lh_struct import (
    LHStructField,
    class_to_put_struct_def_request,
    lh_struct_def,
)
from littlehorse.model import PutUserTaskDefRequest, StructDefId
from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


@lh_struct_def(name="person-details-form")
class PersonDetails:
    identification: Annotated[str, LHStructField(description="Person identification")]
    address: Annotated[str, LHStructField(description="Mailing address")]
    age: Annotated[int, LHStructField(description="Age in years")]


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_user_task_def(struct_def_id: StructDefId) -> PutUserTaskDefRequest:
    return PutUserTaskDefRequest(
        name="person-details",
        result_struct_def_id=struct_def_id,
    )


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        user_task_output = wf.assign_user_task("person-details", None, "writer-group")
        wf.schedule_reminder_task(user_task_output, 10, "remind-person-details", "Sam")
        # The worker receives the submitted Struct as a typed Python object.
        wf.execute("greet", "Sam", user_task_output)

    return Workflow("example-user-tasks", my_entrypoint)


async def remind(name: str) -> str:
    message = f"Reminder: complete person details for {name}."
    print(message, flush=True)
    return message


async def greeting(name: str, person_details: PersonDetails, ctx: WorkerContext) -> str:
    msg = (
        f"Hello {name}! WfRun {ctx.wf_run_id.id} Person: "
        f"identification={person_details.identification}, "
        f"address={person_details.address}, age={person_details.age}"
    )
    print(msg, flush=True)
    return msg


async def main() -> None:
    config = get_config()
    client = config.stub()

    struct_def = client.PutStructDef(class_to_put_struct_def_request(PersonDetails))
    client.PutUserTaskDef(get_user_task_def(struct_def.id))
    littlehorse.create_task_def(remind, "remind-person-details", config)
    littlehorse.create_task_def(greeting, "greet", config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(
        LHTaskWorker(remind, "remind-person-details", config),
        LHTaskWorker(greeting, "greet", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
