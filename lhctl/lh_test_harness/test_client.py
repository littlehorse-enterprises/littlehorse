"""
This class defines a client that can be used in the test harness functions.
"""

from contextlib import closing
import logging
from typing import Callable, Iterable, Tuple

from sqlalchemy.orm import Session
from lh_lib.client import LHClient
from lh_lib.schema.wf_run_schema import WFRunSchema
from lh_test_harness.test_utils import generate_guid, get_session
from lh_test_harness.db_schema import (
    TestStatus,
    WFRun,
)


class TestClient:
    def __init__(self, client: LHClient):
        self._client = client

    def run_wf(self, wf_spec_id: str, check_func: Callable, **kwargs):
        wf_run_id = generate_guid()

        wf_run = WFRun(
            wf_run_id=wf_run_id,
            wf_spec_id=wf_spec_id,
            variables=kwargs,
            status=TestStatus.LAUNCHING,
            check_func_name=check_func.__name__,
            check_func_module=check_func.__module__,
        )
        with closing(get_session()) as ses:
            ses.add(wf_run)
            ses.commit()

            try:
                self._client.run_wf(
                    wf_spec_id,
                    vars=kwargs,
                    wf_run_id=wf_run_id,
                )
                new_status = TestStatus.LAUNCHED
                message = None
            except Exception as exn:
                logging.exception(f"Orzdash on {wf_run_id}!", exc_info=exn)
                new_status = TestStatus.FAILED_LAUNCH
                message = "Failed launching the WFRun!"

            wf_run.status = new_status
            wf_run.message = message

            ses.merge(wf_run)
            ses.commit()
        return wf_run_id

    def iter_test_runs(
        self, test_name: str, ses: Session
    ) -> Iterable[Tuple[WFRun, WFRunSchema]]:

        result = ses.query(WFRun).filter(
            WFRun.status == TestStatus.LAUNCHED
        ).filter(
            WFRun.wf_spec_id == test_name
        ).all()

        for row in result:
            wf_run_schema = self._client.get_resource_by_id(
                WFRunSchema,
                row.wf_run_id,
            ).result

            if wf_run_schema is None:
                row.status = TestStatus.FAILED_UNACCEPTABLE
                row.message = "WFRun was not found in API!"
                ses.merge(row)
                ses.flush()
                continue

            yield row, wf_run_schema

    def send_event(self, name, wf_run_id, payload: str):
        self._client.send_event_by_name_or_id(name, wf_run_id, payload)
