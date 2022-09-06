from __future__ import annotations

from inspect import signature, Signature
from typing import (
    TYPE_CHECKING,
    Optional,
    Union,
)
from lh_sdk.condition_utils import IfElseCondition
from lh_sdk.node_output import NodeOutput

from lh_lib.schema.wf_spec_schema import (
    ACCEPTABLE_TYPES,
    ACCEPTABLE_TYPES_LIST,
    LHComparisonEnum,
    VariableAssignmentSchema,
    VariableMutationOperation,
    VariableMutationSchema,
    WFRunVariableTypeEnum,
)


if TYPE_CHECKING:
    from lh_sdk.thread_spec_builder import ThreadSpecBuilder

class WFRunVariable:
    def __init__(
        self,
        name: str,
        var_type: WFRunVariableTypeEnum,
        thread: ThreadSpecBuilder,
        jsonpath: Optional[str] = None,
    ):
        self._name = name
        self._var_type = var_type
        self._thread = thread
        self._jsonpath = jsonpath

    @property
    def var_type(self) -> WFRunVariableTypeEnum:
        return self._var_type

    @property
    def name(self) -> str:
        return self._name

    @property
    def thread(self) -> ThreadSpecBuilder:
        return self._thread

    def jsonpath(self, path: str) -> WFRunVariable:
        return WFRunVariable(
            self.name,
            self.var_type,
            self._thread,
            path,
        )

    def get_jsonpath(self) -> Optional[str]:
        return self._jsonpath

    ######################################
    # Stuff for VariableMutation goes here
    ######################################

    def assign(
        self,
        target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput]
    ):
        self._mutate(VariableMutationOperation.ASSIGN, target)

    def add(self, target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput]):
        self._mutate(VariableMutationOperation.ADD, target)

    def extend(self, target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput]):
        self._mutate(VariableMutationOperation.EXTEND, target)

    def subtract(self, target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput]):
        self._mutate(VariableMutationOperation.SUBTRACT, target)

    def multiply(self, target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput]):
        self._mutate(VariableMutationOperation.MULTIPLY, target)

    def divide(self, target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput]):
        self._mutate(VariableMutationOperation.DIVIDE, target)

    def remove_if_present(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput]
    ):
        self._mutate(VariableMutationOperation.REMOVE_IF_PRESENT, target)

    def remove_idx(self, target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput]):
        self._mutate(VariableMutationOperation.REMOVE_INDEX, target)

    def remove_key(self, target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput]):
        self._mutate(VariableMutationOperation.REMOVE_KEY, target)

    def _create_mutation(
        self,
        op: VariableMutationOperation,
        target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput],
    ) -> VariableMutationSchema:
        out = VariableMutationSchema(
            operation=op,
        )

        if type(target) in ACCEPTABLE_TYPES_LIST:
            out.literal_value = target
        elif isinstance(target, WFRunVariable):
            out.source_variable = VariableAssignmentSchema(
                wf_run_variable_name=target.name,
                json_path=target.get_jsonpath(),
            )
        else:
            assert isinstance(target, NodeOutput)
            out.json_path = target.get_jsonpath()

        return out

    def _mutate(
        self,
        op: VariableMutationOperation,
        target: Union[ACCEPTABLE_TYPES, WFRunVariable, NodeOutput],
    ):
        mutation = self._create_mutation(op, target)
        self._thread._mutate(self.name, mutation)

    #####################################
    # Stuff for conditionals follows here
    #####################################
    def less_than(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        return IfElseCondition(
            self.thread, self, target, LHComparisonEnum.LESS_THAN
        )

    def greater_than(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        return IfElseCondition(
            self.thread, self, target, LHComparisonEnum.GREATER_THAN
        )

    def greater_than_eq(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        return IfElseCondition(
            self.thread, self, target, LHComparisonEnum.GREATER_THAN_EQ
        )

    def less_than_eq(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        return IfElseCondition(
            self.thread, self, target, LHComparisonEnum.LESS_THAN_EQ
        )

    def equals(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        return IfElseCondition(
            self.thread, self, target, LHComparisonEnum.EQUALS
        )

    def not_equals(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        return IfElseCondition(
            self.thread, self, target, LHComparisonEnum.NOT_EQUALS
        )

    def contains(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        raise NotImplementedError()

    def not_contains(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        raise NotImplementedError()

    def is_in(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        return IfElseCondition(
            self.thread, self, target, LHComparisonEnum.IN
        )

    def is_not_in(
        self, target: Union[ACCEPTABLE_TYPES, WFRunVariable]
    ) -> IfElseCondition:
        return IfElseCondition(
            self.thread, self, target, LHComparisonEnum.NOT_IN
        )
