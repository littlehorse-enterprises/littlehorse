import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import WorkflowThread, Workflow, SpawnedThreads

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def entrypoint(wf: WorkflowThread) -> None:
    parent_var = wf.declare_int("parent-var")
    parent_var.assign(wf.execute("parent-task-1", parent_var))

    def child_func(child: WorkflowThread) -> None:
        child_var = child.declare_int("child-var").with_default(34)
        parent_var.assign(child_var)
        node_output = child.execute("child-task", child_var)
        def grandchild_func(handler: WorkflowThread) -> None:
            child_child_var = handler.declare_int("child-child-var").with_default(21)
            child_var.assign(child_child_var)
            parent_var.assign(child_child_var)
        child.handle_error(node_output, grandchild_func)
    child_thread = wf.spawn_thread(child_func, "spawned-thread")

    wf.wait_for_threads(SpawnedThreads.from_list(child_thread))
    wf.execute("parent-task-2")


async def get_child_task(number: int) -> None:
    raise Exception("Yikes")

async def get_parent_task_1(number: int) -> None:
    print("hi from parent_task_1")

async def get_parent_task_2() -> None:
    print("hi from parent_task_2")

def get_workflow() -> Workflow:
    return Workflow("example-child-thread", entrypoint)


async def main() -> None:
    config = get_config()

    littlehorse.create_task_def(get_parent_task_1, "parent-task-1", config)
    littlehorse.create_task_def(get_parent_task_2, "parent-task-2", config)
    littlehorse.create_task_def(get_child_task, "child-task", config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(
        LHTaskWorker(get_parent_task_1, "parent-task-1", config),
        LHTaskWorker(get_parent_task_2, "parent-task-2", config),
        LHTaskWorker(get_child_task, "child-task", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
