from __future__ import annotations
"""
This file is a concept prototype implementation that will show the seeds of a cool
SDK for TaskDef creation from actual code.
"""

from inspect import signature, Signature
import json
import os
import requests
from typing import TYPE_CHECKING, Any, Callable, Mapping, Tuple, Union

from pydantic import BaseModel as PyThingBaseModel
from humps import camelize

if TYPE_CHECKING:
    from lh_lib.schema.wf_spec_schema import ACCEPTABLE_TYPES, WFRunVariableTypeEnum


DEFAULT_URL = os.getenv("LHORSE_API_URL", "http://localhost:5000")


class LHBaseModel(PyThingBaseModel):
    class Config:
        alias_generator = camelize
        allow_population_by_field_name = True
        smart_union = True


def get_lh_var_type(original_type: Any) -> WFRunVariableTypeEnum:
    from lh_lib.schema.wf_spec_schema import WFRunVariableTypeEnum

    if original_type == str:
        return WFRunVariableTypeEnum.STRING
    elif original_type == float:
        return WFRunVariableTypeEnum.FLOAT
    elif original_type == bool:
        return WFRunVariableTypeEnum.BOOLEAN
    elif original_type == int:
        return WFRunVariableTypeEnum.INT
    elif original_type == dict:
        return WFRunVariableTypeEnum.OBJECT
    elif original_type == list:
        return WFRunVariableTypeEnum.ARRAY
    else:
        raise RuntimeError(f"Bad class type for param: {original_type}")


def iter_all_nodes(wf_spec: dict):
    threads = [wf_spec['threadSpecs'][k] for k in wf_spec['threadSpecs'].keys()]

    for thread in threads:
        for node_name in thread['nodes'].keys():
            yield thread['nodes'][node_name]


def get_taskdefs_for_wf(wf_spec: dict):
    task_defs = set({})

    for node in iter_all_nodes(wf_spec):
        if node['nodeType'] == 'TASK':
            task_defs.add(node['taskDefName'])

    return task_defs


def add_resource(type_name: str, data: dict, api_url: str):
    response = requests.post(f"{api_url}/{type_name}", json=data)
    try:
        response.raise_for_status()
    except Exception as exn:
        print(response.content.decode())
        raise exn

    j = response.json()
    if j['status'] != 'OK':
        print(json.dumps(response.json()))
    else:
        print(f"Successfully created {type_name} {j['result']['objectId']}")


def cast_all_args(func, *splat_args) -> dict:
    sig: Signature = signature(func)

    args = list(splat_args)

    out = {}
    i = 0
    for param_name in sig.parameters.keys():
        arg = args[i]
        i += 1

        param = sig.parameters[param_name]
        assert param.annotation is not None  # we know it's annotated by now

        if param.annotation in [list, dict]:
            out[param_name] = json.loads(arg)
        elif param.annotation == bool:
            out[param_name] = True if arg.lower() == 'true' else False
        else:
            assert param.annotation in [int, float, str]
            out[param_name] = param.annotation(arg)

    return out


def get_task_def_name(func: Callable):
    return f"{func.__module__.replace('.', '-')}-{func.__name__}"


def parse_task_def_name(td_name: str) -> Tuple[str, str]:
    return '.'.join(td_name.split('-')[:-1]), td_name.split('-')[-1]


def stringify(thing: ACCEPTABLE_TYPES) -> str:
    if type(thing) in [str, int, bool, float]:
        return str(thing)

    else:
        return json.dumps(thing)