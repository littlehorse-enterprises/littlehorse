import asyncio
import logging
from pathlib import Path
import random

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, Comparator, VariableMutationType
from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import WfRunVariable, WorkflowIfStatement, WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow1() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        amount = wf.declare_int("amount").required()

        def admin_case(wf: WorkflowThread) -> None:
            wf.execute("admin-tasks")
        def manager_case(wf: WorkflowThread) -> None:
            wf.execute("manager-tasks")

        wf.do_if(
            wf.condition(amount, Comparator.EQUALS, "admin"),
            admin_case
        ).do_else_if(
            wf.condition(my_role, Comparator.EQUALS, "manager"),
            manager_case
        ).do_else(
            lambda wf: wf.execute("customer-tasks")
        )

    return Workflow("example-if-statement", my_entrypoint)

def get_workflow2() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        amount = wf.declare_int("amount").required()

        wf.do_if(
            wf.condition(amount, Comparator.GREATER_THAN, 20),
            lambda wf: wf.execute("message", "You have more than $20")
        ).do_else_if(
            wf.condition(amount, Comparator.GREATER_THAN, 15),
            lambda wf: wf.execute("message", "You have greater than $15 but no more than $20")
        ).do_else(
            lambda wf: wf.execute("message", "You have no more than $15")
        )


    return Workflow("example-if-statement-amount", my_entrypoint)

async def adminTasks(ctx: WorkerContext) -> str:
    msg = "I am doing admin tasks"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg

async def managerTasks(ctx: WorkerContext) -> str:
    msg = "I am doing manager tasks"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg

async def customerTasks(ctx: WorkerContext) -> str:
    msg = "You are a customer, you may not perform these tasks"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg

async def message(msg: str, ctx: WorkerContext) -> str:
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg

async def main() -> None:
    config = get_config()
    wf = get_workflow2()

    littlehorse.create_task_def(adminTasks, "admin-tasks", config)
    littlehorse.create_task_def(managerTasks, "manager-tasks", config)
    littlehorse.create_task_def(customerTasks, "customer-tasks", config)
    littlehorse.create_task_def(message, "message", config)

    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(LHTaskWorker(adminTasks, "admin-tasks", config),
                            LHTaskWorker(managerTasks, "manager-tasks", config),
                            LHTaskWorker(customerTasks, "customer-tasks", config),
                            LHTaskWorker(message, "message", config))


if __name__ == "__main__":
    asyncio.run(main())
