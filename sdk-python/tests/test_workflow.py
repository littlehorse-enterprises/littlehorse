import unittest
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import (
    IndexType,
    JsonIndex,
    TaskNode,
    VariableAssignment,
    VariableDef,
)
from littlehorse.model.service_pb2 import PutWfSpecRequest
from littlehorse.model.wf_spec_pb2 import (
    Edge,
    EntrypointNode,
    ExitNode,
    Node,
    ThreadSpec,
)

from littlehorse.workflow import (
    ThreadBuilder,
    WfRunVariable,
    Workflow,
)


class TestWfRunVariable(unittest.TestCase):
    def test_value_is_not_none(self):
        variable = WfRunVariable("my-var", VariableType.STR, "my-str")
        self.assertEqual(variable.default_value.type, VariableType.STR)
        self.assertEqual(variable.default_value.str, "my-str")

        variable = WfRunVariable("my-var", VariableType.STR)
        self.assertEqual(variable.default_value, None)

    def test_validate_are_same_type(self):
        with self.assertRaises(TypeError) as exception_context:
            WfRunVariable("my-var", VariableType.STR, 10)
        self.assertEqual(
            "Default value is not a STR",
            str(exception_context.exception),
        )

    def test_validate_with_json_path_already_set(self):
        variable = WfRunVariable("my-var", VariableType.STR)
        variable.json_path = "$.myPath"
        with self.assertRaises(ValueError) as exception_context:
            variable.with_json_path("$.myNewOne")
        self.assertEqual(
            "Cannot set a json_path twice on same var",
            str(exception_context.exception),
        )

    def test_validate_json_path_already_set(self):
        variable = WfRunVariable("my-var", VariableType.STR)
        variable.json_path = "$.myPath"
        with self.assertRaises(ValueError) as exception_context:
            variable.json_path = "$.myNewOne"
        self.assertEqual(
            "Cannot set a json_path twice on same var",
            str(exception_context.exception),
        )

    def test_validate_json_path_format(self):
        variable = WfRunVariable("my-var", VariableType.STR)
        with self.assertRaises(ValueError) as exception_context:
            variable.json_path = "$myNewOne"
        self.assertEqual(
            "Invalid JsonPath: $myNewOne. Use $. at the beginning",
            str(exception_context.exception),
        )

    def test_validate_is_json_obj_when_using_json_index(self):
        variable = WfRunVariable("my-var", VariableType.STR)
        with self.assertRaises(ValueError) as exception_context:
            variable.with_json_index("$.myPath", IndexType.LOCAL_INDEX)
        self.assertEqual(
            "JsonPath not allowed in a STR variable",
            str(exception_context.exception),
        )

    def test_validate_is_json_obj_when_using_json_pth(self):
        variable = WfRunVariable("my-var", VariableType.STR)
        with self.assertRaises(ValueError) as exception_context:
            variable.with_json_path("$.myPath")
        self.assertEqual(
            "JsonPath not allowed in a STR variable",
            str(exception_context.exception),
        )

        variable = WfRunVariable("my-var", VariableType.JSON_OBJ)
        variable.with_json_path("$.myPath")

        variable = WfRunVariable("my-var", VariableType.JSON_ARR)
        variable.with_json_path("$.myPath")

    def test_json_path_creates_new(self):
        variable = WfRunVariable("my-var", VariableType.JSON_ARR)
        with_json = variable.with_json_path("$.myPath")
        self.assertIsNot(variable, with_json)

    def test_compile_variable(self):
        variable = WfRunVariable("my-var", VariableType.STR)
        self.assertEqual(
            variable.compile(), VariableDef(name="my-var", type=VariableType.STR)
        )

        variable = WfRunVariable("my-var", VariableType.JSON_OBJ)
        variable.with_json_index("$.myPath", IndexType.LOCAL_INDEX)
        expected_output = VariableDef(name="my-var", type=VariableType.JSON_OBJ)
        expected_output.json_indexes.append(
            JsonIndex(path="$.myPath", index_type=IndexType.LOCAL_INDEX)
        )
        self.assertEqual(variable.compile(), expected_output)


class TestThreadBuilder(unittest.TestCase):
    def test_compile_with_variables(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            thread.add_variable("input-name", VariableType.STR)

        thread = ThreadBuilder(workflow=None, initializer=my_entrypoint)
        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                variable_defs=[VariableDef(name="input-name", type=VariableType.STR)],
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-exit-EXIT")],
                    ),
                    "1-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_compile_with_task(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            thread.execute("greet")

        thread = ThreadBuilder(workflow=None, initializer=my_entrypoint)

        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-greet-TASK")],
                    ),
                    "1-greet-TASK": Node(
                        task=TaskNode(task_def_name="greet"),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_compile_with_variables_and_task(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            the_name = thread.add_variable("input-name", VariableType.STR)
            thread.execute("greet", the_name)

        thread = ThreadBuilder(workflow=None, initializer=my_entrypoint)

        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                variable_defs=[VariableDef(name="input-name", type=VariableType.STR)],
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-greet-TASK")],
                    ),
                    "1-greet-TASK": Node(
                        task=TaskNode(
                            task_def_name="greet",
                            variables=[VariableAssignment(variable_name="input-name")],
                        ),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_validate_variable_already_exists(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            thread.add_variable("input-name", VariableType.STR)
            thread.add_variable("input-name", VariableType.STR)

        with self.assertRaises(ValueError) as exception_context:
            ThreadBuilder(workflow=None, initializer=my_entrypoint)

        self.assertEqual(
            "Variable input-name already added",
            str(exception_context.exception),
        )

    def test_validate_is_active(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            thread.add_variable("input-name", VariableType.STR)

        thread = ThreadBuilder(workflow=None, initializer=my_entrypoint)

        with self.assertRaises(ReferenceError) as exception_context:
            thread.add_variable("new-input", VariableType.STR)

        self.assertEqual(
            "Using an inactive thread, check your workflow",
            str(exception_context.exception),
        )


class TestWorkflow(unittest.TestCase):
    def test_entrypoint_is_a_function(self):
        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", "")
        self.assertEqual(
            "Object is not a ThreadInitializer",
            str(exception_context.exception),
        )

    def test_entrypoint_has_one_parameter(self):
        def my_entrypoint(thread: ThreadBuilder, another: str) -> None:
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            "ThreadInitializer receives only one parameter",
            str(exception_context.exception),
        )

    def test_entrypoint_receives_thread_builder(self):
        def my_entrypoint(thread: str) -> None:
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            "ThreadInitializer receives a ThreadBuilder",
            str(exception_context.exception),
        )

    def test_entrypoint_returns_none(self):
        def my_entrypoint(thread: ThreadBuilder):
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            "ThreadInitializer returns None",
            str(exception_context.exception),
        )

    def test_validate_entrypoint(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            pass

        Workflow("my-wf", my_entrypoint)

    def test_validate_thread_already_exists(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            pass

        wf = Workflow("my-wf", my_entrypoint)
        wf.add_sub_thread("entrypoint", my_entrypoint)

        with self.assertRaises(ValueError) as exception_context:
            wf.add_sub_thread("entrypoint", my_entrypoint)

        self.assertEqual(
            "Thread entrypoint already added",
            str(exception_context.exception),
        )

    def test_compile_with_variables(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            thread.add_variable("input-name", VariableType.STR)

        wf = Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        variable_defs=[
                            VariableDef(name="input-name", type=VariableType.STR)
                        ],
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-exit-EXIT")],
                            ),
                            "1-exit-EXIT": Node(exit=ExitNode()),
                        },
                    )
                },
            ),
        )


if __name__ == "__main__":
    unittest.main()
