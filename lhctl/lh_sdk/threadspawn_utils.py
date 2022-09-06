from __future__ import annotations
from typing import TYPE_CHECKING, Optional

from lh_sdk.node_output import NodeOutput
from lh_sdk.wf_run_variable import WFRunVariable


if TYPE_CHECKING:
    from lh_sdk.thread_spec_builder import ThreadSpecBuilder


class ThreadSpawnOutput(NodeOutput):
    def __init__(
        self,
        node_name: str,
        thread: ThreadSpecBuilder,
        jsonpath: Optional[str] = None
    ):
        super().__init__(node_name, thread, jsonpath=jsonpath)
        self._var: Optional[WFRunVariable] = None

    def get_var(self) -> WFRunVariable:
        assert self._var is not None
        return self._var
