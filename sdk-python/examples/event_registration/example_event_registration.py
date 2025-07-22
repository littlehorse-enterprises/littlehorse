import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import CorrelatedEventConfig
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)

WORKFLOW_NAME = "example-event-registration"
GREET = "greet"
DO_SUMMARY_TASK = "do-summary"
ASK_FOR_NAME_EVENT = "what-is-your-name"
ASK_FOR_AGE_EVENT = "how-old-are-you"
ALLOW_SUMMARY_TASK = "allow-summary"
THROW_EVENT = "post-summary"


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        name = wf.wait_for_event(
            ASK_FOR_NAME_EVENT, timeout=60, auto_register=True, return_type=str
        )
        greet_id = wf.declare_str("id")
        greet_id.assign(wf.execute(GREET, name))

        age = wf.wait_for_event(
            ASK_FOR_AGE_EVENT,
            timeout=60,
            correlation_id=greet_id,
            return_type=int,
            correlated_event_config=CorrelatedEventConfig(
                delete_after_first_correlation=True,
            ),
        )

        wf.wait_for_event(
            ALLOW_SUMMARY_TASK, correlation_id=greet_id, auto_register=True
        )

        summary = wf.execute(DO_SUMMARY_TASK, name, age)
        wf.throw_event(THROW_EVENT, summary, return_type=str)

    return Workflow(WORKFLOW_NAME, my_entrypoint)


counter = 0


async def greet(name: str) -> str:
    msg = f"Hello, {name}"
    print(msg)
    return "greeted-" + name + f"-{counter}"


async def show_summary(name: str, age: int) -> str:
    summary = f"Summary: Name: {name}, Age: {age}"
    if age < 18:
        summary += " (You are a minor)"
    else:
        summary += " (You are an adult)"
    print(summary)
    return summary


async def main() -> None:
    config = get_config()

    littlehorse.create_task_def(show_summary, DO_SUMMARY_TASK, config)
    littlehorse.create_task_def(greet, GREET, config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(
        LHTaskWorker(show_summary, DO_SUMMARY_TASK, config),
        LHTaskWorker(greet, GREET, config),
    )


if __name__ == "__main__":
    asyncio.run(main())
