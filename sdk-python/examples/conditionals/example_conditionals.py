import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, Comparator, VariableMutationType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


GOAL = 1000000.0


class SaleService:
    def __init__(self) -> None:
        self.total = 0.0

    async def sale(self, amount: float) -> float:
        print(f"Selling: {amount}")
        self.total += amount
        print(f"Total sold: {self.total}")
        return self.total

    async def reach_goal(self, amount: float) -> None:
        print(f"Congratulations we reached ${amount}!")

    async def keep_selling(self, amount: float) -> None:
        print(f"Keep selling, still missing ${GOAL-amount} to reach the goal")

    def if_body(self, wf: WorkflowThread) -> None:
        wf.execute("reach-goal", wf.find_variable("total-sales"))

    def else_body(self, wf: WorkflowThread) -> None:
        wf.execute("keep-selling", wf.find_variable("total-sales"))

    def entrypoint(self, wf: WorkflowThread) -> None:
        amount = wf.add_variable("amount", VariableType.DOUBLE)
        total_sales = wf.add_variable("total-sales", VariableType.DOUBLE)
        output = wf.execute("sale", amount)

        wf.mutate(total_sales, VariableMutationType.ASSIGN, output)

        condition = wf.condition(total_sales, Comparator.GREATER_THAN_EQ, GOAL)
        wf.do_if(condition, self.if_body).do_else(self.else_body)

    def get_workflow(self) -> Workflow:
        return Workflow("example-conditionals", self.entrypoint)


async def main() -> None:
    config = get_config()
    sale_service = SaleService()

    littlehorse.create_task_def(sale_service.sale, "sale", config)
    littlehorse.create_task_def(sale_service.reach_goal, "reach-goal", config)
    littlehorse.create_task_def(sale_service.keep_selling, "keep-selling", config)
    littlehorse.create_workflow_spec(sale_service.get_workflow(), config)

    await littlehorse.start(
        LHTaskWorker(sale_service.sale, "sale", config),
        LHTaskWorker(sale_service.reach_goal, "reach-goal", config),
        LHTaskWorker(sale_service.keep_selling, "keep-selling", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
