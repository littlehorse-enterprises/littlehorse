import asyncio
import logging
from pathlib import Path

from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker, WorkerContext, CheckpointContext
from littlehorse.workflow import Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config_path = Path.home() / ".config" / "littlehorse.config"
    if config_path.exists():
        return LHConfig.from_file(str(config_path))
    return LHConfig()


def get_workflow() -> Workflow:
    def workflow_logic(wf: Workflow) -> None:
        the_name = wf.add_str_variable("input-name", None).searchable()
        wf.execute("greet", the_name).with_retries(2)

    return Workflow("example-checkpointed-tasks", workflow_logic)


async def greet(name: str, context: WorkerContext) -> str:
    attempt_number = context.attempt_number

    print(f"Hello from task worker on attempt {attempt_number} before the checkpoint")

    result = await context.execute_and_checkpoint(
        lambda checkpoint_context: checkpoint_greeting(name, attempt_number, checkpoint_context)
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


def checkpoint_greeting(name: str, attempt_number: int, checkpoint_context: CheckpointContext) -> str:
    checkpoint_context.log("This is a checkpoint log.")
    print(f"Hello from task worker on attempt {attempt_number} in the first checkpoint")
    return f"hello {name} from first checkpoint"


def second_checkpoint(attempt_number: int) -> str:
    print("Hi from inside the second checkpoint")
    return " and the second checkpoint"


async def main():
    config = get_config()
    workflow = get_workflow()
    worker = LHTaskWorker(greet, "greet", config)

    worker.register_task_def()
    workflow.register_wf_spec(config.stub())

    print("Starting Checkpoint Task Worker...")
    print('Run: lhctl run example-checkpointed-tasks input-name "Qui-Gon Jinn"')

    await worker.start()


if __name__ == "__main__":
    asyncio.run(main())
