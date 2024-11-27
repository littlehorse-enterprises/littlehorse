import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, VariableMutationType
from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config

def test_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        user_id = wf.add_variable("user-id", VariableType.STR).required() 
        item = wf.add_variable("item", VariableType.STR).required() 
        quantity = wf.add_variable("quantity", VariableType.INT).required()

        wf.execute("charge-credit-card", user_id, quantity.multiply(wf.execute("fetch-price"), item))

    return Workflow("example-basic", my_entrypoint)

async def fetch_price(item: str, ctx: WorkerContext) -> str:
    return 5

async def charge_credit_card(user_id: str, total_price: float, ctx: WorkerContext) -> str:
    print(f"Charging {user_id} ${total_price}")

async def main() -> None:
    config = get_config()
    wf = test_workflow()

    littlehorse.create_task_def(fetch_price, "fetch-price", config)
    littlehorse.create_task_def(charge_credit_card, "charge-credit-card", config)
    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(LHTaskWorker(fetch_price, "fetch-price", config), LHTaskWorker(charge_credit_card, "charge-credit-card", config))


if __name__ == "__main__":
    asyncio.run(main())
