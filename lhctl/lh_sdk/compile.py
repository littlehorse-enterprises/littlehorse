from inspect import signature, Signature
import json
import time
from typing import Iterable, List, Mapping, Set

import os
from humps import camelize
from pydantic import Field

from lh_sdk.thread_spec_builder import Workflow
from lh_sdk.utils import LHBaseModel, add_resource, get_lh_var_type, get_task_def_name, parse_task_def_name
from lh_lib.schema.wf_spec_schema import (
    DockerTaskDeployMetadata,
    ExternalEventDefSchema,
    TaskDefSchema,
    TaskImplTypeEnum,
    WFSpecSchema,
    NodeSchema,
    NodeType,
)


DEFAULT_API_URL = os.getenv("LHORSE_API_URL", "http://localhost:5000")
DEFAULT_DOCKER_IMAGE = os.getenv(
    "LHORSE_TEST_DOCKER_IMAGE",
    "little-horse-test:latest"
)


def iter_nodes(wf: WFSpecSchema) -> Iterable[NodeSchema]:
    for tspec_name in wf.thread_specs.keys():
        tspec = wf.thread_specs[tspec_name]
        for node_name in tspec.nodes.keys():
            node = tspec.nodes[node_name]
            yield node


def get_task_defs_to_build(wf: Workflow) -> Set[str]:
    spec = wf.spec
    out = set({})
    for node in iter_nodes(spec):
        if node.node_type != NodeType.TASK:
            continue
        if wf.should_skip_build(node):
            continue

        out.add(node.task_def_name)
    return out


def get_external_events_for_wf(spec: WFSpecSchema) -> Set[str]:
    out = set({})
    for node in iter_nodes(spec):
        if node.node_type != NodeType.EXTERNAL_EVENT:
            continue
        out.add(node.external_event_def_name)

    for tspec in spec.thread_specs.values():
        for eev_name in (tspec.interrupt_defs or []):
            out.add(eev_name)
    return out


# TODO: This should return a BaseModel not a raw dict
def create_external_event_def(name: str) -> dict:
    return {"name": name}


# TODO: This should return a BaseModel, not a raw dict
def create_task_def(task_def_name: str, wf: Workflow) -> dict:
    _, func_name = parse_task_def_name(task_def_name)
    task_func = wf.module_dict[func_name]

    sig: Signature = signature(task_func)

    required_vars = {}

    for param_name in sig.parameters.keys():
        param = sig.parameters[param_name]
        if param.annotation is None:
            raise RuntimeError("You must annotate your parameters!")

        required_vars[param_name] = {
            "type": get_lh_var_type(param.annotation).value
        }

    bash_command = [
        "python", "-m", "executor",
        get_task_def_name(task_func)
    ]

    for varname in required_vars.keys():
        bash_command.append(f"<<{varname}>>")

    deploy_meta = DockerTaskDeployMetadata(
        docker_image=f"lh-task-{task_def_name}:latest",
        task_type=TaskImplTypeEnum.PYTHON,
        python_function=task_func.__name__,
        python_module=task_func.__module__,
    )

    task_def = {
        "name": task_def_name,
        "deployMetadata": deploy_meta.json(by_alias=True),
        "requiredVars": required_vars,
    }
    return task_def


def _spec_result_alias_generator(s: str) -> str:
    return {
        'externalEventDef': "ExternalEventDef",
        'taskDef': "TaskDef",
        'wfRun': "WFRun",
        "wfSpec": "WFSpec",
        "dockerfile": "Dockerfile"
    }.get(camelize(s), camelize(s))  # type: ignore


class SpecsResult(LHBaseModel):
    external_event_def: List[ExternalEventDefSchema] = Field(
        default_factory=lambda: list([])
    )
    task_def: List[TaskDefSchema] = Field(default_factory=lambda: list([]))
    wf_spec: List[WFSpecSchema] = Field(default_factory=lambda: list([]))
    dockerfile: Mapping[str, str] = Field(default_factory=lambda: dict({}))

    class Config:
        alias_generator = _spec_result_alias_generator


def get_dockerfile(task_def_name: str) -> str:
    os.system(
        "test ! -e pre-install.sh && >pre-install.sh && chmod +x pre-install.sh"
    )
    os.system("test ! -e pre-launch.sh && >pre-launch.sh chmod +x pre-launch.sh")
    os.system("test ! -e requirements.txt && >requirements.txt")

    return """
FROM little-horse-api:latest

COPY pre-install.sh /pre-install.sh
RUN /pre-install.sh

COPY requirements.txt /task-requirements.txt
RUN pip install -r /task-requirements.txt

COPY . .

CMD [ \
    './pre-launch.sh',\
    '&&',\
    'java', '-cp', '/littleHorse.jar',\
    'little.horse.lib.deployers.examples.docker.DockerTaskWorker'\
]\
    """


def get_specs(wf: Workflow):
    task_def_names = get_task_defs_to_build(wf)
    events = get_external_events_for_wf(wf.spec)

    return SpecsResult(**{
        'ExternalEventDef': [create_external_event_def(e) for e in events],
        'WFSpec': [json.loads(wf.spec.json(by_alias=True))],
        'Dockerfile': {
            tdn: get_dockerfile(tdn) for tdn in task_def_names
        },
        'TaskDef': [create_task_def(t, wf) for t in task_def_names],
    })
