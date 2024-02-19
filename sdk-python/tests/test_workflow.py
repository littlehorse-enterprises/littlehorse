import unittest
from unittest.mock import MagicMock
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.model.common_wfspec_pb2 import (
    Comparator,
    TaskNode,
    VariableAssignment,
    VariableDef,
    VariableMutation,
    VariableMutationType,
)
from littlehorse.model.service_pb2 import PutWfSpecRequest
from littlehorse.model.variable_pb2 import VariableValue
from littlehorse.model.object_id_pb2 import (
    ExternalEventDefId,
    TaskDefId,
)
from littlehorse.model.wf_spec_pb2 import (
    Edge,
    EdgeCondition,
    EntrypointNode,
    ExitNode,
    ExternalEventNode,
    FailureDef,
    InterruptDef,
    JsonIndex,
    Node,
    NopNode,
    ThreadRetentionPolicy,
    ThreadSpec,
    FailureHandlerDef,
    ThreadVarDef,
    WfRunVariableAccessLevel,
    WorkflowRetentionPolicy,
)
from littlehorse.workflow import to_variable_assignment, LHErrorType

from littlehorse.workflow import (
    NodeOutput,
    WorkflowThread,
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
        variable = WfRunVariable("my-var", VariableType.STR, default_value="my-str")
        self.assertEqual(variable.default_value.WhichOneof("value"), "str")
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
            variable.searchable_on("$.myPath", VariableType.STR)
        self.assertEqual(
            "JsonPath not allowed in a STR variable",
            str(exception_context.exception),
        )

    def test_persistent(self):
        variable = WfRunVariable("my-var", VariableType.STR).searchable()
        self.assertEqual(variable.compile().searchable, True)

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
            variable.compile(),
            ThreadVarDef(var_def=VariableDef(name="my-var", type=VariableType.STR)),
        )

        variable = WfRunVariable("my-var", VariableType.JSON_OBJ)
        variable.searchable_on("$.myPath", VariableType.STR)
        expected_output = ThreadVarDef(
            var_def=VariableDef(name="my-var", type=VariableType.JSON_OBJ),
            access_level="PUBLIC_VAR",
        )
        expected_output.json_indexes.append(
            JsonIndex(field_path="$.myPath", field_type=VariableType.STR)
        )
        self.assertEqual(variable.compile(), expected_output)

    def test_compile_private_variable(self):
        variable = WfRunVariable("my-var", VariableType.STR, access_level="PRIVATE_VAR")
        expected_output = ThreadVarDef(
            var_def=VariableDef(name="my-var", type=VariableType.STR),
            access_level="PRIVATE_VAR",
        )
        self.assertEqual(variable.compile(), expected_output)

    def test_compile_inherited_variable(self):
        variable = WfRunVariable("my-var", VariableType.STR)
        variable.with_access_level(WfRunVariableAccessLevel.INHERITED_VAR)
        expected_output = ThreadVarDef(
            var_def=VariableDef(name="my-var", type=VariableType.STR),
            access_level="INHERITED_VAR",
        )
        self.assertEqual(variable.compile(), expected_output)


class TestThreadBuilder(unittest.TestCase):
    def test_compile_with_variables(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.add_variable("input-name", VariableType.STR)

        thread = WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)
        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                variable_defs=[
                    ThreadVarDef(
                        var_def=VariableDef(name="input-name", type=VariableType.STR),
                    ),
                ],
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-exit-EXIT")],
                    ),
                    "1-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_fail(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.fail("my_failure_name", "my_message")

        wt = WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)
        self.assertEqual(
            wt.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-my_failure_name-EXIT")],
                    ),
                    "1-my_failure_name-EXIT": Node(
                        exit=ExitNode(
                            failure_def=FailureDef(
                                failure_name="my_failure_name", message="my_message"
                            )
                        ),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_fail_with_output(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.fail("my_failure_name", "my message", "my output")

        wt = WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)
        self.assertEqual(
            wt.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-my_failure_name-EXIT")],
                    ),
                    "1-my_failure_name-EXIT": Node(
                        exit=ExitNode(
                            failure_def=FailureDef(
                                failure_name="my_failure_name",
                                message="my message",
                                content=VariableAssignment(
                                    literal_value=VariableValue(str="my output")
                                ),
                            )
                        ),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_find_variable_validate_input(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.add_variable("my-variable", VariableType.STR)
            thread.find_variable("INPUT")

        with self.assertRaises(ValueError) as exception_context:
            WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

        self.assertEqual(
            "Variable INPUT unaccessible",
            str(exception_context.exception),
        )

    def test_do_if_else(self):
        class MyClass:
            def if_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-1", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )
                thread.execute("task-a")
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-3", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    3,
                )
                thread.execute("task-b")

            def else_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-2", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    2,
                )
                thread.execute("task-c")
                thread.execute("task-d")
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-4", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    4,
                )

            def my_entrypoint(self, thread: WorkflowThread) -> None:
                thread.do_if(
                    thread.condition(20, Comparator.GREATER_THAN, 10),
                    self.if_condition,
                    self.else_condition,
                )

            def to_thread(self):
                return WorkflowThread(
                    workflow=MagicMock(), initializer=self.my_entrypoint
                )

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
                                        literal_value=VariableValue(int=20)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=10)
                                    ),
                                ),
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-1",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=1),
                                    )
                                ],
                            ),
                            Edge(
                                sink_node_name="4-task-c-TASK",
                                condition=EdgeCondition(
                                    comparator=Comparator.LESS_THAN_EQ,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(int=20)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=10)
                                    ),
                                ),
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-2",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=2),
                                    )
                                ],
                            ),
                        ],
                    ),
                    "2-task-a-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="task-a")),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="3-task-b-TASK",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-3",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=3),
                                    )
                                ],
                            )
                        ],
                    ),
                    "3-task-b-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="task-b")),
                        outgoing_edges=[Edge(sink_node_name="6-nop-NOP")],
                    ),
                    "4-task-c-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="task-c")),
                        outgoing_edges=[Edge(sink_node_name="5-task-d-TASK")],
                    ),
                    "5-task-d-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="task-d")),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="6-nop-NOP",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-4",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=4),
                                    )
                                ],
                            )
                        ],
                    ),
                    "6-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[Edge(sink_node_name="7-exit-EXIT")],
                    ),
                    "7-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_do_if_else_only_with_mutations(self):
        class MyClass:
            def if_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-1", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )

            def else_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-2", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    2,
                )

            def my_entrypoint(self, thread: WorkflowThread) -> None:
                thread.do_if(
                    thread.condition(4, Comparator.LESS_THAN, 5),
                    self.if_condition,
                    self.else_condition,
                )

            def to_thread(self):
                return WorkflowThread(
                    workflow=MagicMock(), initializer=self.my_entrypoint
                )

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
                                sink_node_name="2-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.GREATER_THAN_EQ,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(int=4)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=5)
                                    ),
                                ),
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-2",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=2),
                                    )
                                ],
                            ),
                            Edge(
                                sink_node_name="2-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.LESS_THAN,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(int=4)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=5)
                                    ),
                                ),
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-1",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=1),
                                    )
                                ],
                            ),
                        ],
                    ),
                    "2-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                    ),
                    "3-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_do_if_only_with_mutations(self):
        class MyClass:
            def if_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-2", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    2,
                )

            def my_entrypoint(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-1", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )
                thread.do_if(
                    thread.condition(4, Comparator.GREATER_THAN, 5), self.if_condition
                )
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-3", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    3,
                )

            def to_thread(self):
                return WorkflowThread(
                    workflow=MagicMock(), initializer=self.my_entrypoint
                )

        my_object = MyClass()
        thread_builder = my_object.to_thread()
        self.assertEqual(
            thread_builder.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="1-nop-NOP",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-1",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=1),
                                    )
                                ],
                            )
                        ],
                    ),
                    "1-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="2-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.GREATER_THAN,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(int=4)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=5)
                                    ),
                                ),
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-2",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=2),
                                    )
                                ],
                            ),
                            Edge(
                                sink_node_name="2-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.LESS_THAN_EQ,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(int=4)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=5)
                                    ),
                                ),
                            ),
                        ],
                    ),
                    "2-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="3-exit-EXIT",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-3",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=3),
                                    )
                                ],
                            )
                        ],
                    ),
                    "3-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_do_if(self):
        class MyClass:
            def my_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-1", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )
                thread.execute("my-task")
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-2", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    2,
                )

            def my_entrypoint(self, thread: WorkflowThread) -> None:
                thread.do_if(
                    thread.condition(4, Comparator.LESS_THAN, 5), self.my_condition
                )

            def to_thread(self):
                return WorkflowThread(
                    workflow=MagicMock(), initializer=self.my_entrypoint
                )

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
                                        literal_value=VariableValue(int=4)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=5)
                                    ),
                                ),
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-1",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=1),
                                    )
                                ],
                            ),
                            Edge(
                                sink_node_name="3-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.GREATER_THAN_EQ,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(int=4)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=5)
                                    ),
                                ),
                            ),
                        ],
                    ),
                    "2-my-task-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="3-nop-NOP",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-2",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=2),
                                    )
                                ],
                            )
                        ],
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
            def my_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-1", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )
                thread.execute("my-task")
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-2", variable_type=VariableType.INT
                    ),
                    VariableMutationType.ASSIGN,
                    2,
                )

            def my_entrypoint(self, thread: WorkflowThread) -> None:
                thread.do_while(
                    thread.condition(4, Comparator.LESS_THAN, 5), self.my_condition
                )

            def to_thread(self):
                return WorkflowThread(
                    workflow=MagicMock(), initializer=self.my_entrypoint
                )

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
                                        literal_value=VariableValue(int=4)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=5)
                                    ),
                                ),
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-1",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=1),
                                    )
                                ],
                            ),
                            Edge(
                                sink_node_name="3-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.GREATER_THAN_EQ,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(int=4)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=5)
                                    ),
                                ),
                            ),
                        ],
                    ),
                    "2-my-task-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="3-nop-NOP",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-2",
                                        operation=VariableMutationType.ASSIGN,
                                        literal_value=VariableValue(int=2),
                                    )
                                ],
                            )
                        ],
                    ),
                    "3-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="1-nop-NOP",
                                condition=EdgeCondition(
                                    comparator=Comparator.LESS_THAN,
                                    left=VariableAssignment(
                                        literal_value=VariableValue(int=4)
                                    ),
                                    right=VariableAssignment(
                                        literal_value=VariableValue(int=5)
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
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.execute("greet")

        thread = WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-greet-TASK")],
                    ),
                    "1-greet-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="greet")),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_compile_with_variables_and_task(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            the_name = thread.add_variable("input-name", VariableType.STR)
            thread.execute("greet", the_name)

        thread = WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                variable_defs=[
                    ThreadVarDef(
                        var_def=VariableDef(name="input-name", type=VariableType.STR),
                    )
                ],
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[Edge(sink_node_name="1-greet-TASK")],
                    ),
                    "1-greet-TASK": Node(
                        task=TaskNode(
                            task_def_id=TaskDefId(name="greet"),
                            variables=[VariableAssignment(variable_name="input-name")],
                        ),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_compile_with_ext_event_no_timeout(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.wait_for_event("my-event")

        thread = WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

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
                            external_event_def_id=ExternalEventDefId(name="my-event"),
                        ),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_compile_with_ext_event(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.wait_for_event("my-event", 3)

        thread = WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

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
                            external_event_def_id=ExternalEventDefId(name="my-event"),
                            timeout_seconds=to_variable_assignment(3),
                        ),
                        outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                    ),
                    "2-exit-EXIT": Node(exit=ExitNode()),
                },
            ),
        )

    def test_validate_variable_already_exists(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.add_variable("input-name", VariableType.STR)
            thread.add_variable("input-name", VariableType.STR)

        with self.assertRaises(ValueError) as exception_context:
            WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

        self.assertEqual(
            "Variable input-name already added",
            str(exception_context.exception),
        )

    def test_validate_is_active(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.add_variable("input-name", VariableType.STR)

        thread = WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

        with self.assertRaises(ReferenceError) as exception_context:
            thread.add_variable("new-input", VariableType.STR)

        self.assertEqual(
            "Using an inactive thread, check your workflow",
            str(exception_context.exception),
        )

    def test_invalid_int_sleep(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.sleep(0)

        with self.assertRaises(ValueError) as exception_context:
            WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

        self.assertEqual(
            "Value '0' not allowed",
            str(exception_context.exception),
        )

    def test_valid_int_sleep(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.sleep(1)

        try:
            WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)
        except Exception as e:
            self.fail(f"Exception was NOT expected: {e}")

    def test_invalid_variable_sleep(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.add_variable("my-var", VariableType.STR)
            thread.sleep(my_var)

        with self.assertRaises(ValueError) as exception_context:
            WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

        self.assertEqual(
            "WfRunVariable must be VariableType.INT",
            str(exception_context.exception),
        )

    def test_invalid_variable_sleep_until(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.add_variable("my-var", VariableType.STR)
            thread.sleep_until(my_var)

        with self.assertRaises(ValueError) as exception_context:
            WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

        self.assertEqual(
            "WfRunVariable must be VariableType.INT",
            str(exception_context.exception),
        )

    def test_mutate_with_literal_value(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            value = thread.add_variable("value", VariableType.INT)
            thread.mutate(value, VariableMutationType.MULTIPLY, 2)
            thread.execute("result", value)

        thread = WorkflowThread(workflow=MagicMock(), initializer=my_entrypoint)

        self.assertEqual(
            thread.compile(),
            ThreadSpec(
                variable_defs=[
                    ThreadVarDef(
                        var_def=VariableDef(name="value", type=VariableType.INT),
                    ),
                ],
                nodes={
                    "0-entrypoint-ENTRYPOINT": Node(
                        entrypoint=EntrypointNode(),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="1-result-TASK",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="value",
                                        operation=VariableMutationType.MULTIPLY,
                                        literal_value=VariableValue(int=2),
                                    )
                                ],
                            )
                        ],
                    ),
                    "1-result-TASK": Node(
                        task=TaskNode(
                            task_def_id=TaskDefId(name="result"),
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
        def my_entrypoint(thread: WorkflowThread, another: str) -> None:
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
        def my_entrypoint(thread: WorkflowThread):
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint).compile()

        self.assertEqual(
            "ThreadInitializer returns None",
            str(exception_context.exception),
        )

    def test_validate_entrypoint(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            pass

        try:
            Workflow("my-wf", my_entrypoint)
        except Exception as e:
            self.fail(f"No exception expected != {type(e)}: {e}")

    def test_validate_thread_already_exists(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
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
            def my_entrypoint(self, thread: WorkflowThread) -> None:
                thread.add_variable("input-name", VariableType.STR)

        my_class = MyClass()

        try:
            Workflow("my-wf", my_class.my_entrypoint)
        except Exception as e:
            self.fail(f"No exception expected != {type(e)}: {e}")

    def test_compile_with_interrupt(self):
        def my_interrupt_handler(thread: WorkflowThread) -> None:
            thread.execute("interrupt-handler")

        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.add_interrupt_handler("interruption-event", my_interrupt_handler)
            thread.execute("my-task")

        wf = Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        interrupt_defs=[
                            InterruptDef(
                                external_event_def_id=ExternalEventDefId(
                                    name="interruption-event",
                                ),
                                handler_spec_name="interrupt-interruption-event",
                            )
                        ],
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-my-task-TASK")],
                            ),
                            "1-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                    "interrupt-interruption-event": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[
                                    Edge(sink_node_name="1-interrupt-handler-TASK")
                                ],
                            ),
                            "1-interrupt-handler-TASK": Node(
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="interrupt-handler")
                                ),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        }
                    ),
                },
            ),
        )

    def test_compile_with_parent(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.execute("my-task")

        wf = Workflow("my-wf", my_entrypoint, "my-parent-wf")
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        interrupt_defs=[],
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-my-task-TASK")],
                            ),
                            "1-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                },
                parent_wf_spec={"wf_spec_name": "my-parent-wf"},
            ),
        )

    def test_retention_policy(self):
        def workflow_thread(wf: WorkflowThread) -> None:
            wf.with_retention_policy(
                ThreadRetentionPolicy(seconds_after_thread_termination=1)
            )
            wf.execute("some-task")

        wf = Workflow("my-wf", workflow_thread)
        wf.with_retention_policy(
            WorkflowRetentionPolicy(seconds_after_wf_termination=137)
        )
        result = wf.compile()
        self.assertEqual(result.retention_policy.seconds_after_wf_termination, 137)

        entrypoint = result.thread_specs[result.entrypoint_thread_name]
        policy = entrypoint.retention_policy
        self.assertEqual(policy.seconds_after_thread_termination, 1)

    def test_handle_any_failure(self):
        def my_interrupt_handler(thread: WorkflowThread) -> None:
            thread.execute("my-task")

        def my_entrypoint(thread: WorkflowThread) -> None:
            node = thread.execute("fail")
            thread.handle_any_failure(node, my_interrupt_handler)
            thread.execute("my-task")

        wf = Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-fail-TASK")],
                            ),
                            "1-fail-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="fail")),
                                outgoing_edges=[Edge(sink_node_name="2-my-task-TASK")],
                                failure_handlers=[
                                    FailureHandlerDef(
                                        handler_spec_name="exn-handler-1-fail-TASK-any-failure",
                                    )
                                ],
                            ),
                            "2-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                            ),
                            "3-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                    "exn-handler-1-fail-TASK-any-failure": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-my-task-TASK")],
                            ),
                            "1-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        }
                    ),
                },
            ),
        )

    def test_handle_specific_exception(self):
        def my_interrupt_handler(thread: WorkflowThread) -> None:
            thread.execute("my-task")

        def my_entrypoint(thread: WorkflowThread) -> None:
            node = thread.execute("fail")
            thread.handle_exception(node, my_interrupt_handler, "my-exception")
            thread.execute("my-task")

        wf = Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-fail-TASK")],
                            ),
                            "1-fail-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="fail")),
                                outgoing_edges=[Edge(sink_node_name="2-my-task-TASK")],
                                failure_handlers=[
                                    FailureHandlerDef(
                                        handler_spec_name="exn-handler-1-fail-TASK-my-exception",
                                        specific_failure="my-exception",
                                    )
                                ],
                            ),
                            "2-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                            ),
                            "3-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                    "exn-handler-1-fail-TASK-my-exception": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-my-task-TASK")],
                            ),
                            "1-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        }
                    ),
                },
            ),
        )

    def test_handle_any_exception(self):
        def my_interrupt_handler(thread: WorkflowThread) -> None:
            thread.execute("my-task")

        def my_entrypoint(thread: WorkflowThread) -> None:
            node = thread.execute("fail")
            thread.handle_exception(node, my_interrupt_handler)
            thread.execute("my-task")

        wf = Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-fail-TASK")],
                            ),
                            "1-fail-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="fail")),
                                outgoing_edges=[Edge(sink_node_name="2-my-task-TASK")],
                                failure_handlers=[
                                    FailureHandlerDef(
                                        any_failure_of_type=FailureHandlerDef.LHFailureType.FAILURE_TYPE_EXCEPTION,
                                        handler_spec_name="exn-handler-1-fail-TASK",
                                    )
                                ],
                            ),
                            "2-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                            ),
                            "3-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                    "exn-handler-1-fail-TASK": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-my-task-TASK")],
                            ),
                            "1-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        }
                    ),
                },
            ),
        )

    def test_handle_any_error(self):
        def my_interrupt_handler(thread: WorkflowThread) -> None:
            thread.execute("my-task")

        def my_entrypoint(thread: WorkflowThread) -> None:
            node = thread.execute("fail")
            thread.handle_error(node, my_interrupt_handler)
            thread.execute("my-task")

        wf = Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-fail-TASK")],
                            ),
                            "1-fail-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="fail")),
                                outgoing_edges=[Edge(sink_node_name="2-my-task-TASK")],
                                failure_handlers=[
                                    FailureHandlerDef(
                                        handler_spec_name="exn-handler-1-fail-TASK-FAILURE_TYPE_ERROR",
                                        any_failure_of_type="FAILURE_TYPE_ERROR",
                                    )
                                ],
                            ),
                            "2-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                            ),
                            "3-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                    "exn-handler-1-fail-TASK-FAILURE_TYPE_ERROR": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-my-task-TASK")],
                            ),
                            "1-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        }
                    ),
                },
            ),
        )

    def test_handle_task_failure_error(self):
        def my_interrupt_handler(thread: WorkflowThread) -> None:
            thread.execute("my-task")

        def my_entrypoint(thread: WorkflowThread) -> None:
            node = thread.execute("fail")
            thread.handle_error(node, my_interrupt_handler, LHErrorType.TASK_ERROR)
            thread.execute("my-task")

        wf = Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-fail-TASK")],
                            ),
                            "1-fail-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="fail")),
                                outgoing_edges=[Edge(sink_node_name="2-my-task-TASK")],
                                failure_handlers=[
                                    FailureHandlerDef(
                                        handler_spec_name="exn-handler-1-fail-TASK-TASK_ERROR",
                                        specific_failure="TASK_ERROR",
                                    )
                                ],
                            ),
                            "2-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                            ),
                            "3-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                    "exn-handler-1-fail-TASK-TASK_ERROR": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[Edge(sink_node_name="1-my-task-TASK")],
                            ),
                            "1-my-task-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        }
                    ),
                },
            ),
        )

    def test_find_variable_in_another_thread(self):
        def my_handler(thread: WorkflowThread) -> None:
            thread.find_variable("my-variable")

        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.add_variable("my-variable", VariableType.STR)
            thread.handle_error(thread.execute("my-task"), my_handler)

        try:
            wf = Workflow("my-wf", my_entrypoint)
            wf.compile()
        except Exception as e:
            self.fail(f"Exception not expected: {e}")

    def test_compile_wf_with_variables(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.add_variable(
                "input-name", VariableType.STR, access_level="INHERITED_VAR"
            )

        wf = Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        variable_defs=[
                            ThreadVarDef(
                                var_def=VariableDef(
                                    name="input-name", type=VariableType.STR
                                ),
                                access_level="INHERITED_VAR",
                            ),
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

    def test_compile_wf_with_no_updates_type(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.add_variable("input-name", VariableType.STR)

        wf = Workflow("my-wf", my_entrypoint)
        wf.with_update_type("NO_UPDATES")
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                allowed_updates="NO_UPDATES",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        variable_defs=[
                            ThreadVarDef(
                                var_def=VariableDef(
                                    name="input-name", type=VariableType.STR
                                )
                            ),
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

    def test_compile_wf_with_minor_revision_updates_type(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.add_variable("input-name", VariableType.STR)

        wf = Workflow("my-wf", my_entrypoint)
        wf.with_update_type("MINOR_REVISION_UPDATES")
        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                allowed_updates="MINOR_REVISION_UPDATES",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        variable_defs=[
                            ThreadVarDef(
                                var_def=VariableDef(
                                    name="input-name", type=VariableType.STR
                                )
                            ),
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


class TestUserTasks(unittest.TestCase):
    def test_assign_to_user_id(self):
        def wf_func(thread: WorkflowThread) -> None:
            thread.assign_user_task("my-user-task", user_id="obi-wan")

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task
        self.assertEqual(ut_node.user_task_def_name, "my-user-task")
        self.assertFalse(ut_node.HasField("user_group"))
        self.assertTrue(ut_node.HasField("user_id"))
        self.assertEqual(ut_node.user_id.literal_value.str, "obi-wan")

    def test_assign_to_user_group(self):
        def wf_func(thread: WorkflowThread) -> None:
            thread.assign_user_task("my-user-task", user_group="jedi")

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task
        self.assertEqual(ut_node.user_task_def_name, "my-user-task")
        self.assertFalse(ut_node.HasField("user_id"))
        self.assertTrue(ut_node.HasField("user_group"))
        self.assertEqual(ut_node.user_group.literal_value.str, "jedi")

    def test_assign_to_user_and_group(self):
        def wf_func(thread: WorkflowThread) -> None:
            thread.assign_user_task("my-user-task", user_id="yoda", user_group="jedi")

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task
        self.assertEqual(ut_node.user_task_def_name, "my-user-task")
        self.assertTrue(ut_node.HasField("user_id"))
        self.assertTrue(ut_node.HasField("user_group"))
        self.assertEqual(ut_node.user_group.literal_value.str, "jedi")
        self.assertEqual(ut_node.user_id.literal_value.str, "yoda")

    def test_mutations(self):
        def wf_func(thread: WorkflowThread) -> None:
            var = thread.add_variable("my-var", VariableType.INT)
            ut_output = thread.assign_user_task("my-user-task", user_id="obi-wan")
            thread.mutate(var, VariableMutationType.ASSIGN, ut_output)

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        mutations = node.outgoing_edges[0].variable_mutations
        self.assertEqual(1, len(mutations))

        mutation = mutations[0]
        self.assertEqual("my-var", mutation.lhs_name)
        self.assertEqual(VariableMutationType.ASSIGN, mutation.operation)
        self.assertTrue(mutation.HasField("node_output"))

    def test_assign_to_variable_user_id(self):
        def wf_func(thread: WorkflowThread) -> None:
            userid = thread.add_variable("userid", VariableType.INT)
            thread.assign_user_task("my-user-task", user_id=userid)

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task
        self.assertEqual(ut_node.user_task_def_name, "my-user-task")
        self.assertFalse(ut_node.HasField("user_group"))
        self.assertTrue(ut_node.HasField("user_id"))
        self.assertEqual(ut_node.user_id.variable_name, "userid")

    def test_release_to_group(self):
        def wf_func(thread: WorkflowThread) -> None:
            uto = thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            )
            thread.release_to_group_on_deadline(uto, 60)

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task

        self.assertEqual(len(ut_node.actions), 1)

        action = ut_node.actions[0]
        self.assertEqual(action.delay_seconds.literal_value.int, 60)
        self.assertTrue(action.HasField("reassign"))
        self.assertFalse(action.HasField("cancel"))

        reassign = action.reassign
        self.assertEqual(reassign.user_group.literal_value.str, "my-group")

    def test_reminder_task(self):
        def wf_func(thread: WorkflowThread) -> None:
            uto = thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            )
            thread.schedule_reminder_task(uto, 60, "my-reminder-task", "my-arg")

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task

        self.assertEqual(len(ut_node.actions), 1)

        action = ut_node.actions[0]
        self.assertEqual(action.delay_seconds.literal_value.int, 60)
        self.assertTrue(action.HasField("task"))
        reminder_task = action.task
        print(reminder_task)
        self.assertEqual(reminder_task.task.task_def_id.name, "my-reminder-task")

    def test_reassign_to_user_str(self):
        def wf_func(thread: WorkflowThread) -> None:
            uto = thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            )
            thread.reassign_user_task_on_deadline(uto, 60, user_id="obiwan")

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task

        self.assertEqual(len(ut_node.actions), 1)

        action = ut_node.actions[0]
        self.assertTrue(action.HasField("reassign"))
        self.assertFalse(action.HasField("cancel"))

        reassign = action.reassign
        self.assertEqual(reassign.user_id.literal_value.str, "obiwan")
        self.assertFalse(reassign.HasField("user_group"))

    def test_reassign_to_user_var(self):
        def wf_func(thread: WorkflowThread) -> None:
            user_var = WfRunVariable("my-var", VariableType.STR)
            uto = thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            )
            thread.reassign_user_task_on_deadline(uto, 60, user_id=user_var)

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task

        self.assertEqual(len(ut_node.actions), 1)

        action = ut_node.actions[0]
        self.assertTrue(action.HasField("reassign"))
        self.assertFalse(action.HasField("cancel"))

        reassign = action.reassign
        self.assertEqual(reassign.user_id.variable_name, "my-var")
        self.assertFalse(reassign.HasField("user_group"))

    def test_reassign_to_group(self):
        def wf_func(thread: WorkflowThread) -> None:
            user_var = WfRunVariable("my-var", VariableType.STR)
            uto = thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            )
            thread.reassign_user_task_on_deadline(uto, 60, user_group=user_var)

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task

        self.assertEqual(len(ut_node.actions), 1)

        action = ut_node.actions[0]
        self.assertTrue(action.HasField("reassign"))
        self.assertFalse(action.HasField("cancel"))

        reassign = action.reassign
        self.assertEqual(reassign.user_group.variable_name, "my-var")
        self.assertFalse(reassign.HasField("user_id"))

    def test_reassign_to_user_with_group(self):
        def wf_func(thread: WorkflowThread) -> None:
            uto = thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            )
            thread.reassign_user_task_on_deadline(
                uto,
                60,
                user_group="jedi-council",
                user_id="yoda",
            )

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task

        self.assertEqual(len(ut_node.actions), 1)

        action = ut_node.actions[0]
        self.assertTrue(action.HasField("reassign"))
        self.assertFalse(action.HasField("cancel"))

        reassign = action.reassign
        self.assertEqual(reassign.user_group.literal_value.str, "jedi-council")
        self.assertEqual(reassign.user_id.literal_value.str, "yoda")

    def test_notes(self):
        def wf_func(thread: WorkflowThread) -> None:
            thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            ).with_notes("hi there")

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task
        self.assertEqual(ut_node.notes, to_variable_assignment("hi there"))


class FormatStringTest(unittest.TestCase):
    def test_format_string(self):
        def wf_func(thread: WorkflowThread) -> None:
            str_var = thread.add_variable("my-str", VariableType.STR)
            str_var2 = thread.add_variable("my-str-2", VariableType.STR)
            thread.execute("asdf", thread.format("hi {0} there {1}", str_var, str_var2))

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]
        node = thread.nodes["1-asdf-TASK"]
        self.assertTrue(node.HasField("task"))
        task = node.task

        inputs = task.variables
        self.assertEqual(len(inputs), 1)

        self.assertTrue(inputs[0].HasField("format_string"))
        fstr = inputs[0].format_string
        self.assertEqual(fstr.format.literal_value.str, "hi {0} there {1}")
        self.assertEqual(len(fstr.args), 2)
        self.assertEqual(fstr.args[0].variable_name, "my-str")
        self.assertEqual(fstr.args[1].variable_name, "my-str-2")


if __name__ == "__main__":
    unittest.main()
