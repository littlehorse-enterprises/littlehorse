from lh_lib.schema.wf_run_schema import WFRunSchema
from lh_lib.schema.wf_spec_schema import ExternalEventDefSchema, TaskDefSchema, WFSpecSchema


RESOURCE_TYPES = {
    "WFRun": WFRunSchema,
    "WFSpec": WFSpecSchema,
    "TaskDef": TaskDefSchema,
    "ExternalEventDef": ExternalEventDefSchema,
}

RESOURCE_TYPES_INV = {
    RESOURCE_TYPES[k]: k for k in RESOURCE_TYPES.keys()
}


__all__ = [
    'WFRunSchema',
    'WFSpecSchema',
]