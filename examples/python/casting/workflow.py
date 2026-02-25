import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import VariableType
from littlehorse.workflow import Workflow, WorkflowThread

WorkflowName = "casting-workflow"

StringTask = "string-method"
IntTask = "int-method"
DoubleTask = "double-method"
BoolTask = "bool-method"


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        string_input = wf.add_variable("string-number", VariableType.STR)
        string_bool = wf.add_variable("string-bool", VariableType.STR)
        json_input = wf.add_variable("json-input", VariableType.JSON_OBJ)

        # Manual cast STR -> DOUBLE
        double_result = wf.execute(DoubleTask, string_input.cast_to_double())
        # Manual cast DOUBLE -> INT
        int_result = wf.execute(IntTask, double_result.cast_to_int())

        # DOUBLE expression
        math_over_double = double_result.multiply(2.0).divide(6.0)
        wf.execute(IntTask, math_over_double.cast_to_int())

        # STR -> BOOL with error handler
        bool_result = wf.execute(BoolTask, string_bool.cast_to_bool())
        wf.handle_error(bool_result, None, lambda t: t.execute(StringTask, "This is how to handle casting errors"))

        wf.execute(IntTask, double_result.cast_to_int())
        wf.execute(DoubleTask, int_result)  # INT -> DOUBLE (auto)
        wf.execute(IntTask, json_input.with_json_path("$.int").cast_to_int())
        wf.execute(StringTask, string_input)

    return Workflow(WorkflowName, my_entrypoint)
