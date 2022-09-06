"""This module defines a PythonTaskWorker class which takes an arbitrary function
and converts it into a worker for a LittleHorse TaskDef. It can be embedded directly
into arbitrary python code and deployed manually by LittleHorse users, or it can be
automatically deployed via the PythonK8sDeployer or PythonDockerDeployer.

NOTE: Remember to close() the PythonTaskWorker.
"""
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from importlib import import_module
import json
import logging
from os import environ
import traceback
from typing import Iterable, Optional

from confluent_kafka import Producer, Consumer

from lh_lib.client import LHClient
from lh_lib.schema.task_run_event_schema import (
    TaskRunEndedEvent,
    TaskRunEventSchema,
    TaskRunResultSchema,
    TaskRunStartedEvent,
    TaskScheduleRequestSchema,
    WFEventSchema,
    WFEventTypeEnum,
)
from lh_lib.schema.wf_run_schema import LHFailureReasonEnum
from lh_lib.schema.wf_spec_schema import (
    ACCEPTABLE_TYPES_LIST,
    DockerTaskDeployMetadata,
    TaskDefSchema,
    TaskImplTypeEnum,
)
from executor.executor_config import (
    API_URL,
    EXECUTOR_NUM_THREADS,
    KAFKA_APPLICATION_ID,
    KAFKA_APPLICATION_IID,
    KAFKA_BOOTSTRAP_SERVERS,
    KAFKA_POLL_PERIOD,
    TASKDEF_ID_KEY,
)
from lh_sdk.utils import stringify


class PythonTaskWorker:
    def __init__(
        self,
        task_def_name: Optional[str] = None,
        num_threads: Optional[int] = None,
        app_id: Optional[str] = None,
        app_instance_id: Optional[str] = None,
        poll_period: Optional[float] = None,
        bootstrap_servers: Optional[str] = None,
        api_url: str = API_URL,
        client: Optional[LHClient] = None,
    ):
        self._task_def_name = task_def_name or environ[TASKDEF_ID_KEY]
        self._num_threads = num_threads or EXECUTOR_NUM_THREADS
        self._app_id = app_id or KAFKA_APPLICATION_ID
        self._app_instance_id = app_instance_id or KAFKA_APPLICATION_IID
        self._poll_period = poll_period or KAFKA_POLL_PERIOD
        self._bootstrap_servers = bootstrap_servers or KAFKA_BOOTSTRAP_SERVERS
        self._client = client or LHClient(api_url)

        response = self._client.get_resource_by_id(
            TaskDefSchema,
            self._task_def_name,
        )

        if response.result is None:
            raise RuntimeError(f"Was unable to find taskdef {self._task_def_name}")

        self._task_def = response.result
        assert self._task_def.deploy_metadata is not None, "Invalid taskdef!"
        dd_metadata = DockerTaskDeployMetadata(**json.loads(
            self._task_def.deploy_metadata
        ))
        assert dd_metadata.task_type == TaskImplTypeEnum.PYTHON
        self._task_module_name = dd_metadata.python_module
        self._task_func_name = dd_metadata.python_function

        assert self._task_func_name is not None, "invalid taskdef!"
        assert self._task_module_name is not None, "invalid taskdef!"

        self._cons = Consumer(**{
            "bootstrap.servers": self._bootstrap_servers,
            "group.id": self._app_id,
            "group.instance.id": self._app_instance_id,
            "enable.auto.commit": True,
            "isolation.level": "read_committed",
            "auto.commit.interval.ms": 500,
            "auto.offset.reset": "earliest",
        })
        self._cons.subscribe([self._task_def.kafka_topic])

        self._prod = Producer(**{
            "bootstrap.servers": self._bootstrap_servers,
            "client.id": self._app_id,
            'partitioner': 'murmur2',
        })

        self._threadpool = ThreadPoolExecutor(max_workers=self._num_threads)

        mod = import_module(self._task_module_name)
        self._executor_func = mod.__dict__[self._task_func_name]

    def _iter_requests(self) -> Iterable[TaskScheduleRequestSchema]:
        while True:
            self._prod.poll(0)
            result = self._cons.consume(self._num_threads * 2, timeout=self._poll_period)
            self._cons.commit()
            for msg in result:
                msg_dict = json.loads(msg.value().decode())
                yield TaskScheduleRequestSchema(**msg_dict)

    def run(self):
        for req in self._iter_requests():
            self._threadpool.submit(self._process_request, req)

    def _process_request(self, req: TaskScheduleRequestSchema):
        try:
            self._process_helper(req)
        except Exception as exn:
            logging.exception(f"Failed processing! ", exc_info=exn)
            # TODO: produce an event back to LH saying the thing failed.

    def _process_helper(self, req: TaskScheduleRequestSchema):

        # Record the TaskRun as started
        ts = datetime.now()
        task_event = TaskRunEventSchema(**{
            "thread_id": req.thread_id,
            "task_run_position": req.task_run_position,
            "timestamp": ts,
            "task_def_version_number": self._task_def.version_number,
            "started_event": TaskRunStartedEvent(
                worker_id=self._app_instance_id,
                task_run_position=req.task_run_position,
                thread_id=req.thread_id,
            )
        })
        self._record_event(WFEventSchema(**{
            "wf_spec_id": req.wf_spec_id,
            "wf_run_id": req.wf_run_id,
            "timestamp": ts,
            "thread_id": req.thread_id,
            "type": WFEventTypeEnum.TASK_EVENT,
            "content": task_event.json(by_alias=True),
        }), req)

        # Now execute the task and report the result in a separate message
        result: TaskRunResultSchema
        failure_reason: Optional[LHFailureReasonEnum] = None
        try:
            ret = self._executor_func(**req.variable_substitutions)
            if ret is not None and type(ret) not in ACCEPTABLE_TYPES_LIST:
                result = TaskRunResultSchema(
                    stdout=None,
                    stderr="Got an invalid type: " + type(ret),
                    returncode=-1,
                    success=False
                )
                failure_reason = LHFailureReasonEnum.INVALID_WF_SPEC_ERROR
            else:
                result = TaskRunResultSchema(**{
                    "stdout": stringify(ret),
                    "stderr": None,
                    "success": True,
                    "returncode": 0,
                })

        except Exception as exn:
            result = TaskRunResultSchema(**{
                "stdout": None,
                "stderr": traceback.format_exc(),
                "returncode": -1,
                "success": False,
            })
            failure_reason = LHFailureReasonEnum.TASK_FAILURE

        ended_event = TaskRunEndedEvent(
            result=result, thread_id=req.thread_id,
            task_run_position=req.task_run_position,
            reason=failure_reason,
        )
        ts = datetime.now()
        self._record_event(WFEventSchema(**{
            "wf_spec_id": req.wf_spec_id,
            "wf_run_id": req.wf_run_id,
            "timestamp": datetime.now(),
            "thread_id": req.thread_id,
            "type": WFEventTypeEnum.TASK_EVENT,
            "content": TaskRunEventSchema(**{
                "thread_id": req.thread_id,
                "task_run_position": req.task_run_position,
                "timestamp": ts,
                "task_def_version_number": self._task_def.version_number,
                "ended_event": ended_event,
            }).json(by_alias=True),
        }), req)

    def _record_event(self, event: WFEventSchema, req: TaskScheduleRequestSchema):
        logging.warn("Producing record: " + event.json(by_alias=True))
        self._prod.produce(
            req.kafka_topic,
            event.json(by_alias=True).encode(),
            key=req.wf_run_id.encode(),
            headers={"lang": "python".encode()}
        )

    def close(self):
        self._threadpool.shutdown()
        self._cons.close()
        self._prod.flush()
