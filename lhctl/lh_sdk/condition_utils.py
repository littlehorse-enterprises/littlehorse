from __future__ import annotations

from typing import (
    TYPE_CHECKING,
    Optional,
    Union,
)
import uuid


from lh_lib.schema.wf_spec_schema import (
    CONDITION_INVERSES,
    ACCEPTABLE_TYPES,
    EdgeConditionSchema,
    LHComparisonEnum,
    VariableAssignmentSchema,
)


if TYPE_CHECKING:
    from lh_sdk.wf_run_variable import WFRunVariable
    from lh_sdk.thread_spec_builder import ThreadSpecBuilder


class IfElseCondition:
    def __init__(
        self,
        thread: ThreadSpecBuilder,
        lhs: WFRunVariable,
        rhs: Union[ACCEPTABLE_TYPES, WFRunVariable],
        operator: LHComparisonEnum,
    ):
        self._thread = thread
        self._lhs = lhs
        self._rhs = rhs
        self._operator = operator
        self._id = uuid.uuid4().hex
        self._cancelled = False
        self._initial_feeder_node = thread._last_node_name

        self._feeder_nodes: dict[str, Optional[EdgeConditionSchema]] = {}

    @property
    def condition_schema(self) -> EdgeConditionSchema:
        return EdgeConditionSchema(
            left_side=self.left_side,
            right_side=self.right_side,
            comparator=self.operator,
        )

    @property
    def thread(self):
        return self._thread

    @property
    def left_side(self) -> VariableAssignmentSchema:
        return VariableAssignmentSchema(
            wf_run_variable_name=self._lhs.name,
            json_path=self._lhs.get_jsonpath(),
        )

    @property
    def right_side(self) -> VariableAssignmentSchema:
        from lh_sdk.wf_run_variable import WFRunVariable
        if isinstance(self._rhs, WFRunVariable):
            return VariableAssignmentSchema(
                wf_run_variable_name=self._rhs.name,
                json_path=self._rhs.get_jsonpath(),
            )
        else:
            return VariableAssignmentSchema(literal_value=self._rhs)

    @property
    def reverse_condition(self) -> EdgeConditionSchema:
        condition = self.condition_schema
        new_comparator = CONDITION_INVERSES[condition.comparator]
        return EdgeConditionSchema(
            left_side=self.left_side,
            right_side=self.right_side,
            comparator=new_comparator,
        )

    def notify_thread_between_if_else(self):
        self.thread._between_if_elses[self._id] = self

    def notify_thread_else_begun(self):
        del self.thread._between_if_elses[self._id]

    # To be called if the thread starts executing crap without doing the if_else
    # first.
    def handle_else_cancelled(self):
        self._cancelled = True

    @property
    def operator(self):
        return self._operator

    def is_true(self) -> IfConditionContext:
        return IfConditionContext(self)

    def is_false(self) -> ElseConditionContext:
        if self._cancelled:
            raise RuntimeError(
                "Must call the is_false() directly after end of is_true()!"
            )
        return ElseConditionContext(self)


class IfConditionContext:
    def __init__(
        self,
        parent: IfElseCondition,
    ):
        self._parent = parent

        self._feeder_nodes: dict[str, Optional[EdgeConditionSchema]] = {}

    @property
    def parent(self):
        return self._parent

    def __enter__(self):
        new_condition = self.parent.condition_schema

        if len(self.parent.thread._spec.nodes) == 0:
            self.parent.thread.add_nop_node()

        for node_name in self.parent.thread._feeder_nodes:
            if self.parent.thread._feeder_nodes[node_name] is not None:
                self.parent.thread.add_nop_node()  # Just to make things work
                break

        for node_name in self.parent.thread._feeder_nodes:
            self.parent.thread._feeder_nodes[node_name] = new_condition

        self._feeder_nodes.update(self.parent.thread._feeder_nodes)

        if self.parent.thread._last_node_name is None:
            assert len(self.parent.thread._feeder_nodes) == 0
            self.parent.thread.add_nop_node()
            assert self.parent.thread._last_node_name is not None

        self._feeder_nodes[
            self.parent.thread._last_node_name
        ] = self.parent.reverse_condition

    def __exit__(self, exc_type, exc_value, tb):
        self.parent.thread._feeder_nodes.update(self._feeder_nodes)
        self.parent.notify_thread_between_if_else()


class ElseConditionContext:
    def __init__(self, parent: IfElseCondition):
        self._parent = parent
        self._popped_node_name: Optional[str] = None

    @property
    def parent(self):
        return self._parent

    def __enter__(self):
        # The IfConditionContext has already __enter__()'ed and __exit__()'ed,
        # so we know that the parent.thread._feeder_nodes contains the last_node from
        # before the IfCondition actually entered, and it also contains the last
        # node from the if block.
        # We want to pop the last node from the if block and then re-add it later on.
        self._popped_node_name = self.parent.thread._last_node_name
        assert self._popped_node_name is not None
        assert self._popped_node_name in self.parent.thread._feeder_nodes
        del self.parent.thread._feeder_nodes[self._popped_node_name]

        # There should still be nodes in there!
        assert len(self.parent.thread._feeder_nodes) > 0

    def __exit__(self, exc_type, exc_value, tb):
        # After both the if and else blocks exit, we need the last node from
        # each block to have a null condition and both go to the next thing.
        assert self._popped_node_name is not None
        self.parent.thread._feeder_nodes[self._popped_node_name] = None
        self.parent.thread.add_nop_node()  # too lazy to do edge cases manually...


