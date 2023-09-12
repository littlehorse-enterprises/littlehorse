import unittest
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import (
    Comparator,
    IndexType,
    JsonIndex,
    TaskNode,
    VariableAssignment,
    VariableDef,
    VariableMutation,
    VariableMutationType,
)
from littlehorse.model.service_pb2 import PutWfSpecRequest
from littlehorse.model.variable_pb2 import VariableValue
from littlehorse.model.wf_spec_pb2 import (
    Edge,
    EdgeCondition,
    EntrypointNode,
    ExitNode,
    ExternalEventNode,
    Node,
    NopNode,
    ThreadSpec,
)
from littlehorse.workflow import to_variable_assignment

from littlehorse.workflow import (
    NodeOutput,
    ThreadBuilder,
    WfRunVariable,
    Workflow,
)


class TestNodeOutput(unittest.TestCase):
    def test_validate_with_json_path_already_set(self):
        variable = NodeOutput("my-node")
        variable.json_path = "$.myPath"
        with self.assertRaises(ValueError) as exception_context:
            variable.with_json_path("$.myNewOne")
        self.assertEqual(
            "Cannot set a json_path twice on same var",
            str(exception_context.exception),
        )

    def test_validate_json_path_already_set(self):
        variable = NodeOutput("my-node")
        variable.json_path = "$.myPath"
        with self.assertRaises(ValueError) as exception_context:
            variable.json_path = "$.myNewOne"
        self.assertEqual(
            "Cannot set a json_path twice on same var",
            str(exception_context.exception),
        )

    def test_validate_json_path_format(self):
        variable = NodeOutput("my-node")
        with self.assertRaises(ValueError) as exception_context:
            variable.json_path = "$myNewOne"
        self.assertEqual(
            "Invalid JsonPath: $myNewOne. Use $. at the beginning",
            str(exception_context.exception),
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

    def test_persistent(self):
        variable = WfRunVariable("my-var", VariableType.STR).persistent()
        self.assertEqual(variable.compile().persistent, True)

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

    def test_do_if_else(self):
        class MyClass:
            def if_condition(self, thread: ThreadBuilder) -> None:
                thread.execute("task-a")
                thread.execute("task-b")

            def else_condition(self, thread: ThreadBuilder) -> None:
                thread.execute("task-c")
                thread.execute("task-d")

            def my_entrypoint(self, thread: ThreadBuilder) -> None:
                thread.do_if(
                    thread.condition(20, Comparator.GREATER_THAN, 10),
                    self.if_condition,
                    self.else_condition,
                )

            def to_thread(self):
                return ThreadBuilder(workflow=None, initializer=self.my_entrypoint)

        my_object = MyClass()
        thread_builder = my_object.to_thread()
        self.assertEqual(
            thread_builder.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-nop-NOP")],
                    ),
                    "1-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="2-task-a-TASK",
                                condition=EdgeCondition(
                                    comparator=Comparator.GREATER_THAN,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=20
                                        )
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=10
                                        )
                                    ),
                                ),
                            ),
                            Edge(
                                sink_node_name="5-task-c-TASK",
                                condition=EdgeCondition(
                                    comparator=Comparator.LESS_THAN_EQ,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=20
                                        )
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=10
                                        )
                                    ),
                                ),
                            ),
                        ],
                    ),
                    "2-task-a-TASK": Node(
                        task=TaskNode(task_def_name="task-a"),
                        outgoing_edges=[Edge(sink_node_name="3-task-b-TASK")],
                    ),
                    "3-task-b-TASK": Node(
                        task=TaskNode(task_def_name="task-b"),
                        outgoing_edges=[Edge(sink_node_name="4-nop-NOP")],
                    ),
                    "4-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[Edge(sink_node_name="7-exit-EXIT")],
                    ),
                    "5-task-c-TASK": Node(
                        task=TaskNode(task_def_name="task-c"),
                        outgoing_edges=[Edge(sink_node_name="6-task-d-TASK")],
                    ),
                    "6-task-d-TASK": Node(
                        task=TaskNode(task_def_name="task-d"),
                        outgoing_edges=[Edge(sink_node_name="4-nop-NOP")],
                    ),
                    "7-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_do_if(self):
        class MyClass:
            def my_condition(self, thread: ThreadBuilder) -> None:
                thread.execute("my-task")

            def my_entrypoint(self, thread: ThreadBuilder) -> None:
                thread.do_if(
                    thread.condition(4, Comparator.LESS_THAN, 5), self.my_condition
                )

            def to_thread(self):
                return ThreadBuilder(workflow=None, initializer=self.my_entrypoint)

        my_object = MyClass()
        thread_builder = my_object.to_thread()
        self.assertEqual(
            thread_builder.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-nop-NOP")],
                    ),
                    "1-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="2-my-task-TASK",
                                condition=EdgeCondition(
                                    comparator=Comparator.LESS_THAN,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=4
                                        )
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=5
                                        )
                                    ),
                                ),
                            ),
                            Edge(
                                sink_node_name="3-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.GREATER_THAN_EQ,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=4
                                        )
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=5
                                        )
                                    ),
                                ),
                            ),
                        ],
                    ),
                    "2-my-task-TASK": Node(
                        task=TaskNode(task_def_name="my-task"),
                        outgoing_edges=[Edge(sink_node_name="3-nop-NOP")],
                    ),
                    "3-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[Edge(sink_node_name="4-exit-EXIT")],
                    ),
                    "4-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_do_while(self):
        class MyClass:
            def my_condition(self, thread: ThreadBuilder) -> None:
                thread.execute("my-task")

            def my_entrypoint(self, thread: ThreadBuilder) -> None:
                thread.do_while(
                    thread.condition(4, Comparator.LESS_THAN, 5), self.my_condition
                )

            def to_thread(self):
                return ThreadBuilder(workflow=None, initializer=self.my_entrypoint)

        my_object = MyClass()
        thread_builder = my_object.to_thread()
        self.assertEqual(
            thread_builder.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-nop-NOP")],
                    ),
                    "1-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="2-my-task-TASK",
                                condition=EdgeCondition(
                                    comparator=Comparator.LESS_THAN,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=4
                                        )
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=5
                                        )
                                    ),
                                ),
                            ),
                            Edge(
                                sink_node_name="3-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.GREATER_THAN_EQ,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=4
                                        )
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=5
                                        )
                                    ),
                                ),
                            ),
                        ],
                    ),
                    "2-my-task-TASK": Node(
                        task=TaskNode(task_def_name="my-task"),
                        outgoing_edges=[Edge(sink_node_name="3-nop-NOP")],
                    ),
                    "3-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="1-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.LESS_THAN,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=4
                                        )
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(
                                            type=VariableType.INT, int=5
                                        )
                                    ),
                                ),
                            ),
                            Edge(sink_node_name="4-exit-EXIT"),
                        ],
                    ),
                    "4-exit-EXIT": Node(exit=ExitNode()),
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

    def test_compile_with_ext_event_no_timeout(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            thread.wait_for_event("my-event")

        thread = ThreadBuilder(workflow=None, initializer=my_entrypoint)

        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[
                            Edge(sink_node_name="1-my-event-EXTERNAL_EVENT")
                        ],
                    ),
                    "1-my-event-EXTERNAL_EVENT": Node(
                        external_event=ExternalEventNode(
                            external_event_def_name="my-event",
                        ),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_compile_with_ext_event(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            thread.wait_for_event("my-event", 3)

        thread = ThreadBuilder(workflow=None, initializer=my_entrypoint)

        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[
                            Edge(sink_node_name="1-my-event-EXTERNAL_EVENT")
                        ],
                    ),
                    "1-my-event-EXTERNAL_EVENT": Node(
                        external_event=ExternalEventNode(
                            external_event_def_name="my-event",
                            timeout_seconds=to_variable_assignment(3),
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

    def test_mutate_with_literal_value(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            value = thread.add_variable("value", VariableType.INT)
            thread.mutate(value, VariableMutationType.MULTIPLY, 2)
            thread.execute("result", value)

        thread = ThreadBuilder(workflow=None, initializer=my_entrypoint)

        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                variable_defs=[VariableDef(name="value", type=VariableType.INT)],
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-result-TASK")],
                        variable_mutations=[
                            VariableMutation(
                                lhs_name="value",
                                operation=VariableMutationType.MULTIPLY,
                                literal_value=VariableValue(
                                    int=2, type=VariableType.INT
                                ),
                            )
                        ],
                    ),
                    "1-result-TASK": Node(
                        task=TaskNode(
                            task_def_name="result",
                            variables=[VariableAssignment(variable_name="value")],
                        ),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )


class TestWorkflow(unittest.TestCase):
    def test_entrypoint_is_a_function(self):
        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", "").compile()

        self.assertEqual(
            "Object is not a ThreadInitializer",
            str(exception_context.exception),
        )

    def test_entrypoint_has_one_parameter(self):
        def my_entrypoint(thread: ThreadBuilder, another: str) -> None:
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint).compile()

        self.assertEqual(
            "ThreadInitializer receives only one parameter",
            str(exception_context.exception),
        )

    def test_entrypoint_receives_thread_builder(self):
        def my_entrypoint(thread: str) -> None:
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint).compile()

        self.assertEqual(
            "ThreadInitializer receives a ThreadBuilder",
            str(exception_context.exception),
        )

    def test_entrypoint_returns_none(self):
        def my_entrypoint(thread: ThreadBuilder):
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint).compile()

        self.assertEqual(
            "ThreadInitializer returns None",
            str(exception_context.exception),
        )

    def test_validate_entrypoint(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            pass

        try:
            Workflow("my-wf", my_entrypoint)
        except Exception as e:
            self.fail(f"No exception expected != {type(e)}: {e}")

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

    def test_compile_with_function_as_class_member(self):
        class MyClass:
            def my_entrypoint(self, thread: ThreadBuilder) -> None:
                thread.add_variable("input-name", VariableType.STR)

        my_class = MyClass()

        try:
            Workflow("my-wf", my_class.my_entrypoint)
        except Exception as e:
            self.fail(f"No exception expected != {type(e)}: {e}")

    def test_compile_wf_with_variables(self):
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
