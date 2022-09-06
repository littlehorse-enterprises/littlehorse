from __future__ import annotations

from typing import TYPE_CHECKING, Any, Callable, Optional, Union

from lh_lib.schema.wf_spec_schema import ExceptionHandlerSpecSchema, NodeSchema, NodeType

if TYPE_CHECKING:
    from lh_sdk.wf_run_variable import WFRunVariable
    from lh_sdk.thread_spec_builder import ThreadSpecBuilder, THREAD_FUNC


class NodeOutput:
    def __init__(
        self,
        node_name: str,
        thread: ThreadSpecBuilder,
        output_type: Optional[Any] = None,
        jsonpath: Optional[str] = None,
    ):
        self._node_name = node_name
        self._output_type = output_type
        self._jsonpath = jsonpath
        self._thread = thread

    @property
    def output_type(self) -> Any:
        return self._output_type

    @property
    def node_name(self) -> str:
        if self._thread._last_node_name != self._node_name:
            raise RuntimeError(
                "Accessing node output after other nodes executed!"
            )
        return self._node_name

    @property
    def thread(self):
        return self._thread

    @property
    def node(self) -> NodeSchema:
        return self.thread._spec.nodes[self.node_name]

    def jsonpath(self, path: str) -> NodeOutput:
        if self._thread._last_node_name != self._node_name:
            raise RuntimeError(
                "Accessing node output after other nodes executed!"
            )
        if self._jsonpath is not None:
            raise RuntimeError(
                "Cannot double-up the jsonpath!"
            )
        return NodeOutput(
            self.node_name,
            self._thread,
            output_type=self.output_type,
            jsonpath=path,
        )

    def get_jsonpath(self) -> Optional[str]:
        if self._thread._last_node_name != self._node_name:
            raise RuntimeError(
                "Accessing node output after other nodes executed!"
            )
        return self._jsonpath

    def with_retries(self, num_retries) -> NodeOutput:
        if self.node.node_type != NodeType.TASK:
            raise RuntimeError("Can only retry task node")
        self.node.num_retries = num_retries
        return self

    def with_timeout(self, timeout_seconds: Union[int, WFRunVariable]) -> NodeOutput:
        self.node.timeout_seconds = self.thread.construct_var_assign(timeout_seconds)
        return self

    def catch_exception(
        self,
        handler: THREAD_FUNC,
        exc_name: Optional[str] = None
    ) -> NodeOutput:
        if self.node_name != self.thread._last_node_name:
            raise RuntimeError("Modifying an already-set node!")

        thread_name = self.thread.add_subthread(handler)

        handler_spec = ExceptionHandlerSpecSchema(
            handler_thread_spec_name=thread_name
        )
        if exc_name is None:
            self.node.base_exceptionhandler = handler_spec
        else:
            if self.node.custom_exception_handlers is None:
                self.node.custom_exception_handlers = {}
            self.node.custom_exception_handlers[exc_name] = handler_spec

        return self
