from __future__ import annotations

from enum import Enum as PythonEnum
from datetime import datetime
from typing import Any, List, Mapping, Optional
import uuid

from sqlalchemy import (
    Column,
    ForeignKey,
    Integer,
    String,
    DateTime,
    Enum,
    JSON,
)

from sqlalchemy.orm import registry, relation, relationship # type: ignore
from sqlalchemy.orm.decl_api import DeclarativeMeta
from sqlalchemy.orm.relationships import foreign
from sqlalchemy.sql.schema import (
    PrimaryKeyConstraint,
)

from sqlalchemy.sql.sqltypes import BigInteger, Boolean

mapper_registry = registry()


class BaseOrm(metaclass=DeclarativeMeta):
    __abstract__ = True

    # these are supplied by the sqlalchemy2-stubs, so may be omitted
    # when they are installed
    registry = mapper_registry
    metadata = mapper_registry.metadata

    __init__ = mapper_registry.constructor
    created = Column(DateTime, default=lambda: datetime.now())
    updated = Column(
        DateTime, default=lambda: datetime.now(), onupdate=lambda: datetime.now()
    )
    # guid = Column(String, primary_key=True, default=lambda: uuid.uuid4().hex)
    deleted = Column(Boolean, default=False)

    def to_dict(self):
        # TODO: This might actually be a Really Bad Idea. If I were at REX, I'd ask
        # John Hanley for input...but I'm not.
        # UPDATE: Yeah, the John Hanley in my head was right. Turns out that the lazy
        # loading of sqlalchemy models can cause a headache here because the __dict__
        # doesn't get populated until an actual SELECT is done.
        self.created  # force lazy-loading to actually load so that dict aint empty
        dd = self.__dict__.copy()
        dd.pop('_sa_instance_state', None)
        return dd


class TestStatus(PythonEnum):
    LAUNCHING = "LAUNCHING"
    LAUNCHED = "LAUNCHED"
    FAILED_LAUNCH = "FALIED_LAUNCH"
    SUCCEEDED = "SUCCEEDED"
    FAILED_ACCEPTABLE = "FAILED_ACCEPTABLE"
    FAILED_UNACCEPTABLE = "FAILED_UNACCEPTABLE"


class WFRun(BaseOrm):
    __tablename__ = 'wf_run'

    variables: Mapping[str, Any] = Column(JSON, nullable=True)  # type: ignore
    launch_time: datetime = Column(
        DateTime, default=lambda: datetime.now()
    )  # type: ignore
    wf_spec_id: str = Column(String, nullable=False)  # type: ignore
    wf_run_id: str = Column(String, nullable=False, primary_key=True)  # type: ignore
    message: Optional[str] = Column(String, nullable=True)  # type: ignore
    status: TestStatus = Column(
        Enum(TestStatus), default=TestStatus.LAUNCHING, nullable=False
    )  # type: ignore
    harness_worker_partition: Optional[int] = Column(
        Integer, nullable=True
    ) # type: ignore

    task_runs: List[TaskRun] = relationship('TaskRun')
    already_graded: bool = Column(
        Boolean, nullable=False, default=False
    )  # type: ignore

    check_func_name: str = Column(
        String, nullable=False
    ) # type: ignore
    check_func_module: str = Column(
        String, nullable=False,
    ) # type: ignore

    num_mis_reported: int = Column(Integer, nullable=True)  # type: ignore
    num_orphans: int = Column(Integer, nullable=True)  # type: ignore


class TaskRun(BaseOrm):
    __tablename__ = 'task_run'
    variables: Mapping[str, Any] = Column(JSON, nullable=True)  # type: ignore
    execution_time: datetime = Column(
        DateTime, default=lambda: datetime.now()
    )  # type: ignore
    wf_run_id: str = Column(
        String,
        ForeignKey('wf_run.wf_run_id'),
        nullable=False
    )  # type: ignore
    thread_run_id: int = Column(Integer, nullable=False)  # type: ignore
    task_run_number: int = Column(Integer, nullable=False)  # type: ignore
    stdout: str = Column(String, nullable=True)  # type: ignore
    stderr: str = Column(String, nullable=True)  # type: ignore
    task_def: str = Column(String, nullable=True)  # type: ignore
    guid: str = Column(
        String, default=lambda: uuid.uuid4().hex, primary_key=True
    ) # type: ignore

    wf_run: WFRun = relationship('WFRun', back_populates='task_runs')

