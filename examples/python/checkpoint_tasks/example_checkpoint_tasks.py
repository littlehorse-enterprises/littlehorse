import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType
from littlehorse.worker import LHTaskWorker, WorkerContext, CheckpointContext
from littlehorse.workflow import Workflow, WorkflowThread

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config

def get_workflow() -> Workflow:
    def workflow_logic(wf: WorkflowThread) -> None:
        the_name = wf.declare_str("input-name").searchable()
        wf.execute("greet", the_name, retries=2)

    return Workflow("example-checkpointed-tasks", workflow_logic)


async def greet(name: str, context: WorkerContext) -> str:
    attempt_number = context.attempt_number

    print(f"Hello from task worker on attempt {attempt_number} before the checkpoint")

    result = await context.execute_and_checkpoint(
        lambda checkpoint_context: first_checkpoint(name, attempt_number, checkpoint_context)
    )

    print("Hello from after the first checkpoint")

    if attempt_number == 0:
        raise RuntimeError(
            "Throwing a failure in the second checkpoint to show how the checkpoint works"
        )

    result += await context.execute_and_checkpoint(
        lambda context2: second_checkpoint(attempt_number)
    )

    print(f"Hi from after the checkpoints on attemptNumber {attempt_number}")

    return result + " and after the second checkpoint"


def first_checkpoint(name: str, attempt_number: int, checkpoint_context: CheckpointContext) -> str:
    checkpoint_context.log("This is a checkpoint log.")
    print(f"Hello from task worker on attempt {attempt_number} in the first checkpoint")
    return f"hello {name} from first checkpoint"

def second_checkpoint(attempt_number: int) -> str:
    print("Hi from inside the second checkpoint")
    return " and the second checkpoint"


async def main():
    config = get_config()
    workflow = get_workflow()

    littlehorse.create_task_def(greet, "greet", config)
    littlehorse.create_workflow_spec(workflow, config)

    await littlehorse.start(LHTaskWorker(greet, "greet", config))


if __name__ == "__main__":
    asyncio.run(main())
