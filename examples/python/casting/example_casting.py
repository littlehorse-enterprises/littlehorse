import asyncio
import logging
from pathlib import Path

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType
from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        string_input = wf.add_variable("string-number", VariableType.STR, default_value="3.14")
        string_bool = wf.add_variable("string-bool", VariableType.STR, default_value="false")
        json_input = wf.add_variable(
            "json-input", 
            VariableType.JSON_OBJ, 
            default_value={"int": "1", "string": "hello"}
        )

        # Manual cast from STR variable to DOUBLE
        double_result = wf.execute("double-method", string_input.cast_to_double())
        
        # Manual cast from DOUBLE output to INT
        int_result = wf.execute("int-method", double_result.cast_to_int())
        
        # Math expression over DOUBLE, then cast to INT
        math_over_double = double_result.multiply(2.0).divide(6.0)
        wf.execute("int-method", math_over_double.cast_to_int())
        
        # Manual cast from STR to BOOL with error handler
        bool_result = wf.execute("bool-method", string_bool.cast_to_bool())
        
        def handle_casting_error(handler: WorkflowThread) -> None:
            # If casting "Hello" to BOOL fails, handle it here
            handler.execute("string-method", "This is how to handle casting errors")
        
        wf.handle_error(bool_result, handle_casting_error)
        
        # Manual cast from DOUBLE to INT
        wf.execute("int-method", double_result.cast_to_int())
        
        # Auto cast from INT to DOUBLE
        wf.execute("double-method", int_result)
        
        # Extract JSON path and cast to INT
        wf.execute("int-method", json_input.with_json_path("$.int").cast_to_int())
        
        # Print the original string
        wf.execute("string-method", string_input)

    return Workflow("casting-workflow", my_entrypoint)


async def string_method(value: str, ctx: WorkerContext) -> str:
    ctx.log(f"Executing string-method with value: {value}")
    logging.info(f"Executing string-method with value: {value}")
    return value


async def int_method(value: int, ctx: WorkerContext) -> int:
    result = value * 2
    ctx.log(f"Executing int-method with value: {value}, and doubling its value to: {result}")
    logging.info(f"Executing int-method with value: {value}, and doubling its value to: {result}")
    return result


async def double_method(value: float, ctx: WorkerContext) -> float:
    result = value * 0.9
    ctx.log(f"Executing double-method with value: {value}, and reducing its value to: {result}")
    logging.info(f"Executing double-method with value: {value}, and reducing its value to: {result}")
    return result


async def bool_method(value: bool, ctx: WorkerContext) -> bool:
    result = not value
    ctx.log(f"Executing bool-method with value: {value}, and toggling its value to: {result}")
    logging.info(f"Executing bool-method with value: {value}, and toggling its value to: {result}")
    return result


async def main() -> None:
    config = get_config()
    wf = get_workflow()

    littlehorse.create_task_def(string_method, "string-method", config)
    littlehorse.create_task_def(int_method, "int-method", config)
    littlehorse.create_task_def(double_method, "double-method", config)
    littlehorse.create_task_def(bool_method, "bool-method", config)

    littlehorse.create_workflow_spec(wf, config)

    await littlehorse.start(
        LHTaskWorker(string_method, "string-method", config),
        LHTaskWorker(int_method, "int-method", config),
        LHTaskWorker(double_method, "double-method", config),
        LHTaskWorker(bool_method, "bool-method", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
