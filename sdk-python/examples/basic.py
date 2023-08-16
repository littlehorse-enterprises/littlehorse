import logging
from pathlib import Path

from littlehorse.config import LHConfig
from littlehorse.model.service_pb2 import (
    GetTaskDefReplyPb,
    LHResponseCodePb,
    TaskDefIdPb,
)
from littlehorse.worker import LHTaskWorker


logging.basicConfig(level=logging.DEBUG)
config_path = Path.home().joinpath(".config", "littlehorse.config")


def my_callable(name: str) -> None:
    print(f"Hello {name}!")


# Initiate le worker
task_def_name = "greet"
config = LHConfig()
config.load(config_path)
worker = LHTaskWorker(my_callable, task_def_name, config)

# Register the TaskDef
stub = config.blocking_stub()
reply: GetTaskDefReplyPb = stub.GetTaskDef(TaskDefIdPb(name=task_def_name))

if reply.code is LHResponseCodePb.OK:
    print("Task already exist, skipping creation")
else:
    print("Here we should put a task")


# Start the worker
# loop = asyncio.get_event_loop()
# loop.create_task(worker.start())

# try:
#     loop.run_forever()
# except KeyboardInterrupt:
#     pass
