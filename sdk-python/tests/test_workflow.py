import unittest
from unittest.mock import MagicMock

from littlehorse.model import LHErrorType, VariableType
from littlehorse.model import (
    Comparator,
    TaskNode,
    VariableAssignment,
    VariableDef,
    VariableMutation,
    VariableMutationType,
    UTActionTrigger,
    ExponentialBackoffRetryPolicy,
    ExternalEventDefId,
    TaskDefId,
    PutWfSpecRequest,
    VariableValue,
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
    StartThreadNode,
    ThreadRetentionPolicy,
    ThreadSpec,
    TypeDefinition,
    FailureHandlerDef,
    ThreadVarDef,
    WfRunVariableAccessLevel,
    WorkflowRetentionPolicy,
)
from littlehorse.workflow import (
    NodeOutput,
    WorkflowThread,
    WfRunVariable,
    Workflow,
    WorkflowIfStatement,
)
from littlehorse.workflow import SpawnedThreads, to_variable_assignment


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

    @classmethod
    def setUpClass(cls):
        def entrypoint_func(wf: WorkflowThread) -> None:
            pass

        workflow = Workflow("test-workflow", entrypoint_func)
        cls.workflow_thread = WorkflowThread(workflow, entrypoint_func)

    def test_value_is_not_none(self):
        variable = WfRunVariable(
            "my-var", VariableType.STR, self.workflow_thread, default_value="my-str"
        )
        self.assertEqual(variable.default_value.WhichOneof("value"), "str")
        self.assertEqual(variable.default_value.str, "my-str")

        variable = WfRunVariable("my-var", VariableType.STR, self.workflow_thread)
        self.assertEqual(variable.default_value, None)

    def test_validate_are_same_type(self):
        with self.assertRaises(TypeError) as exception_context:
            WfRunVariable("my-var", VariableType.STR, self.workflow_thread, 10)
        self.assertEqual(
            "Default value type does not match LH variable type STR",
            str(exception_context.exception),
        )

    def test_validate_with_json_path_already_set(self):
        variable = WfRunVariable("my-var", VariableType.STR, self.workflow_thread)
        variable.json_path = "$.myPath"
        with self.assertRaises(ValueError) as exception_context:
            variable.with_json_path("$.myNewOne")
        self.assertEqual(
            "Cannot set a json_path twice on same var",
            str(exception_context.exception),
        )

    def test_validate_json_path_already_set(self):
        variable = WfRunVariable("my-var", VariableType.STR, self.workflow_thread)
        variable.json_path = "$.myPath"
        with self.assertRaises(ValueError) as exception_context:
            variable.json_path = "$.myNewOne"
        self.assertEqual(
            "Cannot set a json_path twice on same var",
            str(exception_context.exception),
        )

    def test_validate_json_path_format(self):
        variable = WfRunVariable("my-var", VariableType.STR, self.workflow_thread)
        with self.assertRaises(ValueError) as exception_context:
            variable.json_path = "$myNewOne"
        self.assertEqual(
            "Invalid JsonPath: $myNewOne. Use $. at the beginning",
            str(exception_context.exception),
        )

    def test_validate_is_json_obj_when_using_json_index(self):
        variable = WfRunVariable("my-var", VariableType.STR, self.workflow_thread)
        with self.assertRaises(ValueError) as exception_context:
            variable.searchable_on("$.myPath", VariableType.STR)
        self.assertEqual(
            "JsonPath not allowed in a STR variable",
            str(exception_context.exception),
        )

    def test_persistent(self):
        variable = WfRunVariable(
            "my-var", VariableType.STR, self.workflow_thread
        ).searchable()
        self.assertEqual(variable.compile().searchable, True)

    def test_validate_is_json_obj_when_using_json_pth(self):
        variable = WfRunVariable("my-var", VariableType.STR, self.workflow_thread)
        with self.assertRaises(ValueError) as exception_context:
            variable.with_json_path("$.myPath")
        self.assertEqual(
            "JsonPath not allowed in a STR variable",
            str(exception_context.exception),
        )

        variable = WfRunVariable("my-var", VariableType.JSON_OBJ, self.workflow_thread)
        variable.with_json_path("$.myPath")

        variable = WfRunVariable("my-var", VariableType.JSON_ARR, self.workflow_thread)
        variable.with_json_path("$.myPath")

    def test_json_path_creates_new(self):
        variable = WfRunVariable("my-var", VariableType.JSON_ARR, self.workflow_thread)
        with_json = variable.with_json_path("$.myPath")
        self.assertIsNot(variable, with_json)

    def test_compile_variable(self):
        variable = WfRunVariable("my-var", VariableType.STR, self.workflow_thread)
        self.assertEqual(
            variable.compile(),
            ThreadVarDef(
                var_def=VariableDef(
                    name="my-var",
                    type_def=TypeDefinition(primitive_type=VariableType.STR, masked=False),
                ),
                access_level=WfRunVariableAccessLevel.PRIVATE_VAR,
            ),
        )

        variable = WfRunVariable("my-var", VariableType.JSON_OBJ, self.workflow_thread)
        variable.searchable_on("$.myPath", VariableType.STR)
        expected_output = ThreadVarDef(
            var_def=VariableDef(
                name="my-var",
                type_def=TypeDefinition(primitive_type=VariableType.JSON_OBJ, masked=False),
            ),
            access_level="PRIVATE_VAR",
        )
        expected_output.json_indexes.append(
            JsonIndex(field_path="$.myPath", field_type=VariableType.STR)
        )
        self.assertEqual(variable.compile(), expected_output)

    def test_compile_private_variable(self):
        variable = WfRunVariable(
            "my-var", VariableType.STR, self.workflow_thread, access_level="PRIVATE_VAR"
        )
        expected_output = ThreadVarDef(
            var_def=VariableDef(
                name="my-var",
                type_def=TypeDefinition(primitive_type=VariableType.STR, masked=False),
            ),
            access_level="PRIVATE_VAR",
        )
        self.assertEqual(variable.compile(), expected_output)

    def test_compile_inherited_variable(self):
        variable = WfRunVariable("my-var", VariableType.STR, self.workflow_thread)
        variable.with_access_level(WfRunVariableAccessLevel.INHERITED_VAR)
        expected_output = ThreadVarDef(
            var_def=VariableDef(
                name="my-var",
                type_def=TypeDefinition(primitive_type=VariableType.STR, masked=False),
            ),
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
                        var_def=VariableDef(
                            name="input-name",
                            type_def=TypeDefinition(
                                primitive_type=VariableType.STR, masked=False
                            ),
                        ),
                        access_level=WfRunVariableAccessLevel.PRIVATE_VAR,
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

    def test_initializing_thread_without_parent_workflow_should_raise_an_error(self):
        def entrypoint_func(thread: WorkflowThread) -> None:
            pass

        with self.assertRaises(ValueError) as exception_context:
            WorkflowThread(workflow=None, initializer=entrypoint_func)
        self.assertEqual(
            "Workflow cannot be None.",
            str(exception_context.exception),
        )

    def test_compile_with_declare_str(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.declare_str("test-var", default_value="Qui-Gon Jinn")

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        variable_def = entrypoint.variable_defs[0].var_def

        self.assertEqual(variable_def.name, "test-var")
        self.assertEqual(variable_def.type_def.type, VariableType.STR)
        self.assertEqual(variable_def.default_value, VariableValue(str="Qui-Gon Jinn"))

    def test_compile_with_declare_int(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.declare_int("test-var", default_value=1977)

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        variable_def = entrypoint.variable_defs[0].var_def

        self.assertEqual(variable_def.name, "test-var")
        self.assertEqual(variable_def.type_def.type, VariableType.INT)
        self.assertEqual(variable_def.default_value, VariableValue(int=1977))

    def test_compile_with_declare_double(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.declare_double("test-var", default_value=3.141592)

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        variable_def = entrypoint.variable_defs[0].var_def

        self.assertEqual(variable_def.name, "test-var")
        self.assertEqual(variable_def.type_def.type, VariableType.DOUBLE)
        self.assertEqual(variable_def.default_value, VariableValue(double=3.141592))

    def test_compile_with_declare_bool(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.declare_bool("test-var", default_value=False)

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        variable_def = entrypoint.variable_defs[0].var_def

        self.assertEqual(variable_def.name, "test-var")
        self.assertEqual(variable_def.type_def.type, VariableType.BOOL)
        self.assertEqual(variable_def.default_value, VariableValue(bool=False))

    def test_compile_with_declare_bytes(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.declare_bytes("test-var", default_value=b"Hello World")

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        variable_def = entrypoint.variable_defs[0].var_def

        self.assertEqual(variable_def.name, "test-var")
        self.assertEqual(variable_def.type_def.type, VariableType.BYTES)
        self.assertEqual(
            variable_def.default_value, VariableValue(bytes=b"Hello World")
        )

    def test_compile_with_declare_json_obj(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.declare_json_obj(
                "test-var", default_value={"key1": 5, "key2": "value2"}
            )

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        variable_def = entrypoint.variable_defs[0].var_def

        self.assertEqual(variable_def.name, "test-var")
        self.assertEqual(variable_def.type_def.type, VariableType.JSON_OBJ)
        self.assertEqual(
            variable_def.default_value,
            VariableValue(json_obj='{"key1": 5, "key2": "value2"}'),
        )

    def test_compile_with_declare_json_arr(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.declare_json_arr("test-var", default_value=[5, 10, 15, 20])

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        variable_def = entrypoint.variable_defs[0].var_def

        self.assertEqual(variable_def.name, "test-var")
        self.assertEqual(variable_def.type_def.type, VariableType.JSON_ARR)
        self.assertEqual(
            variable_def.default_value, VariableValue(json_arr="[5, 10, 15, 20]")
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
                    ),
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
                    ),
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
                        variable_name="variable-1",
                        variable_type=VariableType.INT,
                        parent=thread,
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )
                thread.execute("task-a")
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-3",
                        variable_type=VariableType.INT,
                        parent=thread,
                    ),
                    VariableMutationType.ASSIGN,
                    3,
                )
                thread.execute("task-b")

            def else_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-2",
                        variable_type=VariableType.INT,
                        parent=thread,
                    ),
                    VariableMutationType.ASSIGN,
                    2,
                )
                thread.execute("task-c")
                thread.execute("task-d")
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-4",
                        variable_type=VariableType.INT,
                        parent=thread,
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=1)
                                        ),
                                    )
                                ],
                            ),
                            Edge(
                                sink_node_name="5-task-c-TASK",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-2",
                                        operation=VariableMutationType.ASSIGN,
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=2)
                                        ),
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=3)
                                        ),
                                    )
                                ],
                            )
                        ],
                    ),
                    "3-task-b-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="task-b")),
                        outgoing_edges=[Edge(sink_node_name="4-nop-NOP")],
                    ),
                    "4-nop-NOP": Node(
                        nop=NopNode(),
                        outgoing_edges=[Edge(sink_node_name="7-exit-EXIT")],
                    ),
                    "5-task-c-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="task-c")),
                        outgoing_edges=[Edge(sink_node_name="6-task-d-TASK")],
                    ),
                    "6-task-d-TASK": Node(
                        task=TaskNode(task_def_id=TaskDefId(name="task-d")),
                        outgoing_edges=[
                            Edge(
                                sink_node_name="4-nop-NOP",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-4",
                                        operation=VariableMutationType.ASSIGN,
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=4)
                                        ),
                                    )
                                ],
                            )
                        ],
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
                        variable_name="variable-1",
                        variable_type=VariableType.INT,
                        parent=thread,
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )

            def else_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-2",
                        variable_type=VariableType.INT,
                        parent=thread,
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=1)
                                        ),
                                    )
                                ],
                            ),
                            Edge(
                                sink_node_name="2-nop-NOP",
                                variable_mutations=[
                                    VariableMutation(
                                        lhs_name="variable-2",
                                        operation=VariableMutationType.ASSIGN,
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=2)
                                        ),
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
                        variable_name="variable-2",
                        variable_type=VariableType.INT,
                        parent=thread,
                    ),
                    VariableMutationType.ASSIGN,
                    2,
                )

            def my_entrypoint(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-1",
                        variable_type=VariableType.INT,
                        parent=thread,
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )
                thread.do_if(
                    thread.condition(4, Comparator.GREATER_THAN, 5), self.if_condition
                )
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-3",
                        variable_type=VariableType.INT,
                        parent=thread,
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=1)
                                        ),
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=2)
                                        ),
                                    )
                                ],
                            ),
                            Edge(
                                sink_node_name="2-nop-NOP",
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=3)
                                        ),
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
                        variable_name="variable-1",
                        variable_type=VariableType.INT,
                        parent=thread,
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )
                thread.execute("my-task")
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-2",
                        variable_type=VariableType.INT,
                        parent=thread,
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=1)
                                        ),
                                    )
                                ],
                            ),
                            Edge(
                                sink_node_name="3-nop-NOP",
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=2)
                                        ),
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

    def test_do_if_with_lambda(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            wf.do_if(
                wf.condition(5, Comparator.EQUALS, 5), lambda wf: wf.execute("my-task")
            )

        workflow = Workflow("my-wf", my_entrypoint)
        entrypoint_thread = workflow.compile().thread_specs.get("entrypoint")
        self.assertEqual(
            entrypoint_thread,
            ThreadSpec(
                nodes=(
                    {
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
                                        left=VariableAssignment(
                                            literal_value=VariableValue(int=5)
                                        ),
                                        comparator=Comparator.EQUALS,
                                        right=VariableAssignment(
                                            literal_value=VariableValue(int=5)
                                        ),
                                    ),
                                ),
                                Edge(
                                    sink_node_name="3-nop-NOP",
                                ),
                            ],
                        ),
                        "2-my-task-TASK": Node(
                            task=TaskNode(task_def_id=TaskDefId(name="my-task")),
                            outgoing_edges=[
                                Edge(
                                    sink_node_name="3-nop-NOP",
                                )
                            ],
                        ),
                        "3-nop-NOP": Node(
                            nop=NopNode(),
                            outgoing_edges=[Edge(sink_node_name="4-exit-EXIT")],
                        ),
                        "4-exit-EXIT": Node(exit=ExitNode()),
                    }
                )
            ),
        )

    def test_should_compile_a_wf_with_multiple_if_conditions_and_one_task_in_each_body_functions_case_do_else_if(
        self,
    ):
        def my_entrypoint(wf: WorkflowThread) -> None:
            my_int = wf.declare_int("my-int")

            def if_body_a(thread: WorkflowThread) -> None:
                thread.execute("task-a")
                my_int.assign(9)

            def if_body_b(thread: WorkflowThread) -> None:
                my_int.assign(10)
                thread.execute("task-b")

            def if_body_c(thread: WorkflowThread) -> None:
                thread.execute("task-c")

            if_statement: WorkflowIfStatement = wf.do_if(
                condition=wf.condition(5, Comparator.GREATER_THAN_EQ, 9),
                if_body=if_body_a,
            )
            if_statement.do_else_if(
                wf.condition(7, Comparator.LESS_THAN, 4), body=if_body_b
            )
            if_statement.do_else_if(wf.condition(5, Comparator.EQUALS, 5), if_body_c)

            my_int.assign(0)

        workflow = Workflow("test-wf", my_entrypoint)
        compiled_wf = workflow.compile()

        compiled_first_nope_node = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "1-nop-NOP"
        )
        compiled_task_a = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "2-task-a-TASK"
        )
        compiled_task_b = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "4-task-b-TASK"
        )
        compiled_task_c = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "5-task-c-TASK"
        )
        compiled_last_nope_node = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "3-nop-NOP"
        )

        expected_number_outgoing_edges_from_first_nop_node = 4
        expected_last_sink_nop_node_name = "3-nop-NOP"
        expected_exit_sink_node_name = "6-exit-EXIT"

        self.assertEqual(
            expected_number_outgoing_edges_from_first_nop_node,
            len(compiled_first_nope_node.outgoing_edges),
        )
        self.assertEqual(
            Node(
                nop=NopNode(),
                outgoing_edges=[
                    Edge(
                        sink_node_name="2-task-a-TASK",
                        condition=EdgeCondition(
                            comparator=Comparator.GREATER_THAN_EQ,
                            left=VariableAssignment(literal_value=VariableValue(int=5)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=9)
                            ),
                        ),
                    ),
                    Edge(
                        sink_node_name="4-task-b-TASK",
                        condition=EdgeCondition(
                            comparator=Comparator.LESS_THAN,
                            left=VariableAssignment(literal_value=VariableValue(int=7)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=4)
                            ),
                        ),
                        variable_mutations=[
                            VariableMutation(
                                lhs_name="my-int",
                                operation=VariableMutationType.ASSIGN,
                                rhs_assignment=VariableAssignment(
                                    literal_value=VariableValue(int=10)
                                ),
                            )
                        ],
                    ),
                    Edge(
                        sink_node_name="5-task-c-TASK",
                        condition=EdgeCondition(
                            comparator=Comparator.EQUALS,
                            left=VariableAssignment(literal_value=VariableValue(int=5)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=5)
                            ),
                        ),
                    ),
                    Edge(sink_node_name="3-nop-NOP"),
                ],
            ),
            compiled_first_nope_node,
        )
        self.assertEqual(
            VariableMutation(
                lhs_name="my-int",
                operation=VariableMutationType.ASSIGN,
                rhs_assignment=VariableAssignment(literal_value=VariableValue(int=9)),
            ),
            compiled_task_a.outgoing_edges[0].variable_mutations[0],
        )
        self.assertEqual(
            VariableMutation(
                lhs_name="my-int",
                operation=VariableMutationType.ASSIGN,
                rhs_assignment=VariableAssignment(literal_value=VariableValue(int=0)),
            ),
            compiled_last_nope_node.outgoing_edges[0].variable_mutations[0],
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_a.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_b.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_c.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_exit_sink_node_name,
            compiled_last_nope_node.outgoing_edges[0].sink_node_name,
        )

    def test_should_compile_a_wf_with_multiple_conditions_including_else_one(self):
        def my_entrypoint2(wf: WorkflowThread) -> None:
            my_int = wf.declare_int("my-int")

            def if_body_a(thread: WorkflowThread) -> None:
                thread.execute("task-a")
                my_int.assign(9)

            def if_body_b(thread: WorkflowThread) -> None:
                my_int.assign(10)
                thread.execute("task-b")

            def if_body_c(thread: WorkflowThread) -> None:
                thread.execute("task-c")

            if_statement: WorkflowIfStatement = wf.do_if(
                condition=wf.condition(5, Comparator.GREATER_THAN_EQ, 9),
                if_body=if_body_a,
            )
            if_statement.do_else_if(
                wf.condition(7, Comparator.LESS_THAN, 4), body=if_body_b
            )
            if_statement.do_else(if_body_c)

        workflow = Workflow("test-wf2", my_entrypoint2)
        compiled_wf = workflow.compile()

        compiled_first_nope_node = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "1-nop-NOP"
        )
        compiled_task_a = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "2-task-a-TASK"
        )
        compiled_task_b = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "4-task-b-TASK"
        )
        compiled_task_c = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "5-task-c-TASK"
        )
        compiled_last_nope_node = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "3-nop-NOP"
        )

        expected_number_outgoing_edges_from_first_nop_node = 3
        expected_last_sink_nop_node_name = "3-nop-NOP"
        expected_exit_sink_node_name = "6-exit-EXIT"

        self.assertEqual(
            expected_number_outgoing_edges_from_first_nop_node,
            len(compiled_first_nope_node.outgoing_edges),
        )
        self.assertEqual(
            Node(
                nop=NopNode(),
                outgoing_edges=[
                    Edge(
                        sink_node_name="2-task-a-TASK",
                        condition=EdgeCondition(
                            comparator=Comparator.GREATER_THAN_EQ,
                            left=VariableAssignment(literal_value=VariableValue(int=5)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=9)
                            ),
                        ),
                    ),
                    Edge(
                        sink_node_name="4-task-b-TASK",
                        condition=EdgeCondition(
                            comparator=Comparator.LESS_THAN,
                            left=VariableAssignment(literal_value=VariableValue(int=7)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=4)
                            ),
                        ),
                        variable_mutations=[
                            VariableMutation(
                                lhs_name="my-int",
                                operation=VariableMutationType.ASSIGN,
                                rhs_assignment=VariableAssignment(
                                    literal_value=VariableValue(int=10)
                                ),
                            )
                        ],
                    ),
                    Edge(sink_node_name="5-task-c-TASK"),
                ],
            ),
            compiled_first_nope_node,
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_a.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_b.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_c.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_exit_sink_node_name,
            compiled_last_nope_node.outgoing_edges[0].sink_node_name,
        )

    def test_should_throw_an_error_compiling_a_wf_when_else_body_and_do_else_if_are_used(
        self,
    ):
        def my_entrypoint(wf: WorkflowThread) -> None:
            def if_body_a(thread: WorkflowThread) -> None:
                thread.execute("task-a")

            def if_body_b(thread: WorkflowThread) -> None:
                thread.execute("task-b")

            def else_body_c(thread: WorkflowThread) -> None:
                thread.execute("task-c")

            if_statement: WorkflowIfStatement = wf.do_if(
                condition=wf.condition(5, Comparator.EQUALS, 9),
                if_body=if_body_a,
                else_body=else_body_c,
            )
            if_statement.do_else_if(
                wf.condition(7, Comparator.LESS_THAN, 4), body=if_body_b
            )

        workflow = Workflow("test-wf", my_entrypoint)

        with self.assertRaises(AttributeError) as exception_context:
            workflow.compile()

        self.assertEqual(
            "'WorkflowIfStatement' object has no attribute 'do_else_if'",
            str(exception_context.exception),
        )

    def test_should_throw_an_error_compiling_a_wf_when_else_body_and_do_else_are_used(
        self,
    ):
        def my_entrypoint(wf: WorkflowThread) -> None:
            def if_body_a(thread: WorkflowThread) -> None:
                thread.execute("task-a")

            def if_body_b(thread: WorkflowThread) -> None:
                thread.execute("task-b")

            def else_body_c(thread: WorkflowThread) -> None:
                thread.execute("task-c")

            if_statement: WorkflowIfStatement = wf.do_if(
                condition=wf.condition(5, Comparator.EQUALS, 9),
                if_body=if_body_a,
                else_body=else_body_c,
            )
            if_statement.do_else(body=if_body_b)

        workflow = Workflow("test-wf", my_entrypoint)

        with self.assertRaises(AttributeError) as exception_context:
            workflow.compile()

        self.assertEqual(
            "'WorkflowIfStatement' object has no attribute 'do_else'",
            str(exception_context.exception),
        )

    def test_should_add_variable_mutations_to_default_last_node_outgoing_edges_arrowed_to_last_nop_node(
        self,
    ):
        def my_entrypoint(wf: WorkflowThread) -> None:
            my_int = wf.declare_int("my-int")

            def if_body_a(thread: WorkflowThread) -> None:
                thread.execute("task-a")

            def if_body_b(thread: WorkflowThread) -> None:
                thread.execute("task-b")

            def if_body_c(thread: WorkflowThread) -> None:
                my_int.assign(1)

            if_statement: WorkflowIfStatement = wf.do_if(
                condition=wf.condition(5, Comparator.EQUALS, 9), if_body=if_body_a
            )
            if_statement.do_else_if(
                wf.condition(7, Comparator.LESS_THAN, 4), body=if_body_b
            )
            if_statement.do_else_if(
                wf.condition(2, Comparator.EQUALS, 2), body=if_body_c
            )

        workflow = Workflow("test-wf", my_entrypoint)
        compiled_wf = workflow.compile()

        compiled_first_nope_node = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "1-nop-NOP"
        )
        compiled_task_a = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "2-task-a-TASK"
        )
        compiled_task_b = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "4-task-b-TASK"
        )
        compiled_last_nope_node = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "3-nop-NOP"
        )

        expected_number_outgoing_edges_from_first_nop_node = 4
        expected_last_sink_nop_node_name = "3-nop-NOP"
        expected_exit_sink_node_name = "5-exit-EXIT"

        self.assertEqual(
            expected_number_outgoing_edges_from_first_nop_node,
            len(compiled_first_nope_node.outgoing_edges),
        )
        self.assertEqual(
            Node(
                nop=NopNode(),
                outgoing_edges=[
                    Edge(
                        sink_node_name="2-task-a-TASK",
                        condition=EdgeCondition(
                            comparator=Comparator.EQUALS,
                            left=VariableAssignment(literal_value=VariableValue(int=5)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=9)
                            ),
                        ),
                    ),
                    Edge(
                        sink_node_name="4-task-b-TASK",
                        condition=EdgeCondition(
                            comparator=Comparator.LESS_THAN,
                            left=VariableAssignment(literal_value=VariableValue(int=7)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=4)
                            ),
                        ),
                    ),
                    Edge(
                        sink_node_name="3-nop-NOP",
                        condition=EdgeCondition(
                            comparator=Comparator.EQUALS,
                            left=VariableAssignment(literal_value=VariableValue(int=2)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=2)
                            ),
                        ),
                        variable_mutations=[
                            VariableMutation(
                                lhs_name="my-int",
                                operation=VariableMutationType.ASSIGN,
                                rhs_assignment=VariableAssignment(
                                    literal_value=VariableValue(int=1)
                                ),
                            )
                        ],
                    ),
                    Edge(sink_node_name="3-nop-NOP"),
                ],
            ),
            compiled_first_nope_node,
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_a.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_b.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_exit_sink_node_name,
            compiled_last_nope_node.outgoing_edges[0].sink_node_name,
        )

    def test_should_throw_an_error_when_do_else_is_called_multiple_times(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            def if_body_a(thread: WorkflowThread) -> None:
                thread.execute("task-a")

            def if_body_b(thread: WorkflowThread) -> None:
                thread.execute("task-b")

            def else_body_c(thread: WorkflowThread) -> None:
                thread.execute("task-c")

            def else_body_d(thread: WorkflowThread) -> None:
                thread.execute("task-d")

            if_statement: WorkflowIfStatement = wf.do_if(
                condition=wf.condition(5, Comparator.EQUALS, 9), if_body=if_body_a
            )
            if_statement.do_else_if(
                wf.condition(7, Comparator.LESS_THAN, 4), body=if_body_b
            )
            if_statement.do_else(body=else_body_c)

            with self.assertRaises(RuntimeError) as exception_context:
                if_statement.do_else(body=else_body_d)

            self.assertEqual(
                "Else block has already been executed. Cannot add another else block.",
                str(exception_context.exception),
            )

        workflow = Workflow("test-wf", my_entrypoint)
        workflow.compile()

    def test_should_compile_a_wf_with_do_else_if_in_the_middle_of_other_lh_statements(
        self,
    ):
        def my_entrypoint(wf: WorkflowThread) -> None:
            def if_body_a(thread: WorkflowThread) -> None:
                thread.execute("task-a")

            def if_body_b(thread: WorkflowThread) -> None:
                thread.execute("task-b")

            if_statement: WorkflowIfStatement = wf.do_if(
                condition=wf.condition(5, Comparator.GREATER_THAN_EQ, 9),
                if_body=if_body_a,
            )

            wf.execute("task-c")

            if_statement.do_else_if(
                wf.condition(7, Comparator.LESS_THAN, 4), body=if_body_b
            )

        workflow = Workflow("test-wf", my_entrypoint)
        compiled_wf = workflow.compile()

        compiled_first_nope_node = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "1-nop-NOP"
        )
        compiled_task_a = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "2-task-a-TASK"
        )
        compiled_task_b = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "5-task-b-TASK"
        )
        compiled_task_c = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "4-task-c-TASK"
        )
        compiled_last_nope_node = compiled_wf.thread_specs.get("entrypoint").nodes.get(
            "3-nop-NOP"
        )

        expected_number_outgoing_edges_from_first_nop_node = 3
        expected_last_sink_nop_node_name = "3-nop-NOP"
        expected_exit_sink_node_name = "6-exit-EXIT"

        self.assertEqual(
            expected_number_outgoing_edges_from_first_nop_node,
            len(compiled_first_nope_node.outgoing_edges),
        )
        self.assertEqual(
            Node(
                nop=NopNode(),
                outgoing_edges=[
                    Edge(
                        sink_node_name="2-task-a-TASK",
                        condition=EdgeCondition(
                            comparator=Comparator.GREATER_THAN_EQ,
                            left=VariableAssignment(literal_value=VariableValue(int=5)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=9)
                            ),
                        ),
                    ),
                    Edge(
                        sink_node_name="5-task-b-TASK",
                        condition=EdgeCondition(
                            comparator=Comparator.LESS_THAN,
                            left=VariableAssignment(literal_value=VariableValue(int=7)),
                            right=VariableAssignment(
                                literal_value=VariableValue(int=4)
                            ),
                        ),
                    ),
                    Edge(sink_node_name="3-nop-NOP"),
                ],
            ),
            compiled_first_nope_node,
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_a.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_last_sink_nop_node_name,
            compiled_task_b.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            expected_exit_sink_node_name,
            compiled_task_c.outgoing_edges[0].sink_node_name,
        )
        self.assertEqual(
            "4-task-c-TASK",
            compiled_last_nope_node.outgoing_edges[0].sink_node_name,
        )

    def test_wf_raises_error_when_adding_node_after_completing_thread(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            def if_body_a(body: WorkflowThread) -> None:
                body.complete()
                body.execute("greet")

            wf.do_if(wf.condition(5, Comparator.GREATER_THAN, 4), if_body_a)

        workflow = Workflow("test-wf", my_entrypoint)

        with self.assertRaises(TypeError):
            workflow.compile()

    def test_wf_raises_error_when_mutating_variable_after_completing_thread(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            my_var = wf.declare_str("name")

            def if_body_a(body: WorkflowThread) -> None:
                body.complete()
                my_var.assign("hello")

            wf.do_if(wf.condition(5, Comparator.GREATER_THAN, 4), if_body_a)

        workflow = Workflow("test-wf", my_entrypoint)

        with self.assertRaises(TypeError):
            workflow.compile()

    def test_wf_raises_error_when_adding_variable_after_completing_thread(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            def if_body_a(body: WorkflowThread) -> None:
                body.complete()
                body.declare_str("test-var")

            wf.do_if(wf.condition(5, Comparator.GREATER_THAN, 4), if_body_a)

        workflow = Workflow("test-wf", my_entrypoint)

        with self.assertRaises(TypeError):
            workflow.compile()

    def test_compile_wf_with_do_if_and_multiple_exit_nodes(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            def if_body_a(body: WorkflowThread) -> None:
                body.complete()

            wf.do_if(wf.condition(5, Comparator.GREATER_THAN, 4), if_body_a)

        workflow = Workflow("test-wf", my_entrypoint)
        compiled_wf = workflow.compile()
        entrypoint_thread = compiled_wf.thread_specs.get("entrypoint")

        compiled_first_nop_node = entrypoint_thread.nodes.get("1-nop-NOP")
        self.assertEqual(
            compiled_first_nop_node,
            Node(
                nop=NopNode(),
                outgoing_edges=[
                    Edge(
                        sink_node_name="2-complete-EXIT",
                        condition=EdgeCondition(
                            left=VariableAssignment(literal_value=VariableValue(int=5)),
                            comparator=Comparator.GREATER_THAN,
                            right=VariableAssignment(
                                literal_value=VariableValue(int=4)
                            ),
                        ),
                    ),
                    Edge(sink_node_name="3-nop-NOP"),
                ],
            ),
        )

        compiled_complete_exit_node = entrypoint_thread.nodes.get("2-complete-EXIT")
        self.assertEqual(compiled_complete_exit_node, Node(exit=ExitNode()))

        compiled_last_exit_node = entrypoint_thread.nodes.get("4-exit-EXIT")
        self.assertEqual(compiled_last_exit_node, Node(exit=ExitNode()))

    def test_compile_wf_with_do_else_if_and_multiple_exit_nodes(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            def if_body_a(body: WorkflowThread) -> None:
                body.complete()

            def if_body_b(body: WorkflowThread) -> None:
                wf.execute("task-b")
                body.complete()

            wf.do_if(wf.condition(5, Comparator.GREATER_THAN, 4), if_body_a).do_else_if(
                wf.condition(10, Comparator.EQUALS, 11), if_body_b
            )

        workflow = Workflow("test-wf", my_entrypoint)
        compiled_wf = workflow.compile()
        entrypoint_thread = compiled_wf.thread_specs.get("entrypoint")

        compiled_first_nop_node = entrypoint_thread.nodes.get("1-nop-NOP")
        self.assertEqual(
            compiled_first_nop_node,
            Node(
                nop=NopNode(),
                outgoing_edges=[
                    Edge(
                        sink_node_name="2-complete-EXIT",
                        condition=EdgeCondition(
                            left=VariableAssignment(literal_value=VariableValue(int=5)),
                            comparator=Comparator.GREATER_THAN,
                            right=VariableAssignment(
                                literal_value=VariableValue(int=4)
                            ),
                        ),
                    ),
                    Edge(
                        sink_node_name="4-task-b-TASK",
                        condition=EdgeCondition(
                            left=VariableAssignment(
                                literal_value=VariableValue(int=10)
                            ),
                            comparator=Comparator.EQUALS,
                            right=VariableAssignment(
                                literal_value=VariableValue(int=11)
                            ),
                        ),
                    ),
                    Edge(sink_node_name="3-nop-NOP"),
                ],
            ),
        )

        compiled_if_body_a_exit_node = entrypoint_thread.nodes.get("2-complete-EXIT")
        self.assertEqual(compiled_if_body_a_exit_node, Node(exit=ExitNode()))

        compiled_if_body_b_exit_node = entrypoint_thread.nodes.get("5-complete-EXIT")
        self.assertEqual(compiled_if_body_b_exit_node, Node(exit=ExitNode()))

    def test_do_while(self):
        class MyClass:
            def my_condition(self, thread: WorkflowThread) -> None:
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-1",
                        variable_type=VariableType.INT,
                        parent=thread,
                    ),
                    VariableMutationType.ASSIGN,
                    1,
                )
                thread.execute("my-task")
                thread.mutate(
                    WfRunVariable(
                        variable_name="variable-2",
                        variable_type=VariableType.INT,
                        parent=thread,
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=1)
                                        ),
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=2)
                                        ),
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
                        var_def=VariableDef(
                            name="input-name",
                            type_def=TypeDefinition(
                                primitive_type=VariableType.STR, masked=False
                            ),
                        ),
                        access_level=WfRunVariableAccessLevel.PRIVATE_VAR,
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
                        var_def=VariableDef(
                            name="value",
                            type_def=TypeDefinition(
                                primitive_type=VariableType.INT, masked=False
                            ),
                        ),
                        access_level=WfRunVariableAccessLevel.PRIVATE_VAR,
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
                                        rhs_assignment=VariableAssignment(
                                            literal_value=VariableValue(int=2)
                                        ),
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

    def test_mutations_should_use_variable_assignments(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.add_variable("my-var", VariableType.STR)
            my_var.assign("some-value")

        wf_spec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        node = entrypoint.nodes["0-entrypoint-ENTRYPOINT"]

        edge = node.outgoing_edges[0]

        self.assertEqual(
            edge.variable_mutations[0].rhs_assignment.literal_value.str, "some-value"
        )

    def test_node_output_mutations_should_also_use_variable_assignments(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.add_variable("my-var", VariableType.STR)
            my_var.assign(thread.execute("use-the-force"))

        wf_spec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        node = entrypoint.nodes["1-use-the-force-TASK"]

        edge = node.outgoing_edges[0]

        self.assertEqual(
            edge.variable_mutations[0].rhs_assignment.node_output.node_name,
            "1-use-the-force-TASK",
        )

    def test_node_output_mutations_should_carry_json_path(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.add_variable("my-var", VariableType.STR)
            my_var.assign(
                thread.execute("use-the-force").with_json_path("$.hello.there")
            )

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        node = entrypoint.nodes["1-use-the-force-TASK"]

        edge = node.outgoing_edges[0]

        self.assertEqual(
            edge.variable_mutations[0].rhs_assignment.node_output.node_name,
            "1-use-the-force-TASK",
        )

        self.assertEqual(
            edge.variable_mutations[0].rhs_assignment.json_path, "$.hello.there"
        )

    def test_assigning_variables_to_other_variables_should_use_variable_assignment(
        self,
    ):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.add_variable("my-var", VariableType.STR)
            other_var = thread.add_variable("other-var", VariableType.STR)
            my_var.assign(other_var)

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        node = entrypoint.nodes["0-entrypoint-ENTRYPOINT"]

        edge = node.outgoing_edges[0]
        self.assertEqual(
            edge.variable_mutations[0].rhs_assignment.variable_name, "other-var"
        )

    def test_assigning_variables_to_other_variables_should_carry_json_path(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.add_variable("my-var", VariableType.STR)
            other_var = thread.add_variable("other-var", VariableType.JSON_OBJ)
            my_var.assign(other_var.with_json_path("$.hello.there"))

        wfSpec = Workflow("obiwan", my_entrypoint).compile()
        entrypoint = wfSpec.thread_specs[wfSpec.entrypoint_thread_name]
        node = entrypoint.nodes["0-entrypoint-ENTRYPOINT"]

        edge = node.outgoing_edges[0]
        self.assertEqual(
            edge.variable_mutations[0].rhs_assignment.variable_name, "other-var"
        )

        self.assertEqual(
            edge.variable_mutations[0].rhs_assignment.json_path, "$.hello.there"
        )

    def test_initializing_variable_without_parent_thread_should_raise_an_error(self):
        with self.assertRaises(ValueError) as exception_context:
            WfRunVariable("my-var", VariableType.STR, parent=None)

        self.assertEqual(
            "Parent workflow thread cannot be None.",
            str(exception_context.exception),
        )

    def test_should_compile_a_wf_with_does_contain_condition_comparing_variables(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.declare_str("my-var")
            thread.do_if(
                my_var.does_contain("this-value"), lambda wf: wf.execute("task")
            )

        wf_spec = Workflow("test", my_entrypoint).compile()
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        actual_node = entrypoint.nodes["1-nop-NOP"]
        expected_node = Node(
            nop=NopNode(),
            outgoing_edges=[
                Edge(
                    sink_node_name="2-task-TASK",
                    condition=EdgeCondition(
                        comparator=Comparator.IN,
                        left=VariableAssignment(
                            literal_value=VariableValue(str="this-value")
                        ),
                        right=VariableAssignment(variable_name="my-var"),
                    ),
                ),
                Edge(sink_node_name="3-nop-NOP"),
            ],
        )

        self.assertEqual(expected_node, actual_node)

    def test_should_compile_a_wf_with_does_not_contain_condition_comparing_variables(
        self,
    ):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.declare_str("my-var")
            thread.do_if(
                my_var.does_not_contain("this-value"), lambda wf: wf.execute("task")
            )

        wf_spec = Workflow("test", my_entrypoint).compile()
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        actual_node = entrypoint.nodes["1-nop-NOP"]
        expected_node = Node(
            nop=NopNode(),
            outgoing_edges=[
                Edge(
                    sink_node_name="2-task-TASK",
                    condition=EdgeCondition(
                        comparator=Comparator.NOT_IN,
                        left=VariableAssignment(
                            literal_value=VariableValue(str="this-value")
                        ),
                        right=VariableAssignment(variable_name="my-var"),
                    ),
                ),
                Edge(sink_node_name="3-nop-NOP"),
            ],
        )

        self.assertEqual(expected_node, actual_node)

    def test_should_compile_a_wf_with_in_condition_comparing_variables(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.declare_str("my-var")
            thread.do_if(my_var.is_in(["A", "B", "C"]), lambda wf: wf.execute("task"))

        wf_spec = Workflow("test", my_entrypoint).compile()
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        actual_node = entrypoint.nodes["1-nop-NOP"]
        expected_node = Node(
            nop=NopNode(),
            outgoing_edges=[
                Edge(
                    sink_node_name="2-task-TASK",
                    condition=EdgeCondition(
                        comparator=Comparator.IN,
                        left=VariableAssignment(variable_name="my-var"),
                        right=VariableAssignment(
                            literal_value=VariableValue(json_arr='["A", "B", "C"]')
                        ),
                    ),
                ),
                Edge(sink_node_name="3-nop-NOP"),
            ],
        )

        self.assertEqual(expected_node, actual_node)

    def test_should_compile_a_wf_with_not_in_condition_comparing_variables(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            my_var = thread.declare_str("my-var")
            thread.do_if(
                my_var.is_not_in(["A", "B", "C"]), lambda wf: wf.execute("task")
            )

        wf_spec = Workflow("test", my_entrypoint).compile()
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        actual_node = entrypoint.nodes["1-nop-NOP"]
        expected_node = Node(
            nop=NopNode(),
            outgoing_edges=[
                Edge(
                    sink_node_name="2-task-TASK",
                    condition=EdgeCondition(
                        comparator=Comparator.NOT_IN,
                        left=VariableAssignment(variable_name="my-var"),
                        right=VariableAssignment(
                            literal_value=VariableValue(json_arr='["A", "B", "C"]')
                        ),
                    ),
                ),
                Edge(sink_node_name="3-nop-NOP"),
            ],
        )

        self.assertEqual(expected_node, actual_node)


class TestWorkflow(unittest.TestCase):
    def test_entrypoint_is_a_function(self):
        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", "").compile()

        self.assertEqual(
            "Object is not a ThreadInitializer",
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
                                    name="input-name",
                                    type_def=TypeDefinition(
                                        primitive_type=VariableType.STR, masked=False
                                    ),
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
                                    name="input-name",
                                    type_def=TypeDefinition(
                                        primitive_type=VariableType.STR, masked=False
                                    ),
                                ),
                                access_level=WfRunVariableAccessLevel.PRIVATE_VAR,
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
                                    name="input-name",
                                    type_def=TypeDefinition(
                                        primitive_type=VariableType.STR, masked=False
                                    ),
                                ),
                                access_level=WfRunVariableAccessLevel.PRIVATE_VAR,
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

    def test_wf_task_timeout(self):
        timeout_seconds = 5

        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.execute("example-task", timeout_seconds=timeout_seconds)

        wf = Workflow("my-wf", my_entrypoint)

        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        variable_defs=[],
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[
                                    Edge(sink_node_name="1-example-task-TASK")
                                ],
                            ),
                            "1-example-task-TASK": Node(
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="example-task"),
                                    timeout_seconds=timeout_seconds,
                                ),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        },
                    )
                },
            ),
        )

    def test_wf_task_default_timeout(self):
        default_timeout_seconds = 5
        custom_timeout_seconds = 10

        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.execute("use-default-timeout")
            thread.execute("use-custom-timeout", timeout_seconds=custom_timeout_seconds)

        wf = Workflow("my-wf", my_entrypoint).with_task_timeout_seconds(
            default_timeout_seconds
        )

        self.assertEqual(
            wf.compile(),
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        variable_defs=[],
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[
                                    Edge(sink_node_name="1-use-default-timeout-TASK")
                                ],
                            ),
                            "1-use-default-timeout-TASK": Node(
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="use-default-timeout"),
                                    timeout_seconds=default_timeout_seconds,
                                ),
                                outgoing_edges=[
                                    Edge(sink_node_name="2-use-custom-timeout-TASK")
                                ],
                            ),
                            "2-use-custom-timeout-TASK": Node(
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="use-custom-timeout"),
                                    timeout_seconds=custom_timeout_seconds,
                                ),
                                outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                            ),
                            "3-exit-EXIT": Node(exit=ExitNode()),
                        },
                    )
                },
            ),
        )

    def test_workflow_with_parent_var_assigned_to_child_nested_threads_should_compile(
        self,
    ):
        def my_entrypoint(grand_patent_thread: WorkflowThread) -> None:
            grand_parent_var = grand_patent_thread.declare_str("grand-parent-var")

            def son_func(son_thread: WorkflowThread) -> None:
                grand_parent_var.assign("son-value")

                def grand_child_func(grandchild_thread: WorkflowThread) -> None:
                    grand_parent_var.assign("grandchild-value")

                son_thread.spawn_thread(grand_child_func, "grandchild-thread")

            grand_patent_thread.spawn_thread(son_func, "son-thread")

        compiled_wf = Workflow("my-wf", my_entrypoint).compile()
        self.assertEqual(
            compiled_wf,
            PutWfSpecRequest(
                entrypoint_thread_name="entrypoint",
                name="my-wf",
                thread_specs={
                    "entrypoint": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[
                                    Edge(sink_node_name="1-son-thread-START_THREAD")
                                ],
                            ),
                            "1-son-thread-START_THREAD": Node(
                                start_thread=StartThreadNode(
                                    thread_spec_name="son-thread"
                                ),
                                outgoing_edges=[
                                    Edge(
                                        sink_node_name="2-exit-EXIT",
                                        variable_mutations=[
                                            VariableMutation(
                                                lhs_name="1-son-thread-START_THREAD",
                                                rhs_assignment=VariableAssignment(
                                                    node_output=VariableAssignment.NodeOutputReference(
                                                        node_name="1-son-thread-START_THREAD"
                                                    )
                                                ),
                                            )
                                        ],
                                    )
                                ],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        },
                        variable_defs=[
                            ThreadVarDef(
                                var_def=VariableDef(
                                    name="grand-parent-var",
                                    type_def=TypeDefinition(
                                        primitive_type=VariableType.STR, masked=False
                                    ),
                                ),
                                access_level=WfRunVariableAccessLevel.PRIVATE_VAR,
                            ),
                            ThreadVarDef(
                                var_def=VariableDef(
                                    name="1-son-thread-START_THREAD",
                                    type_def=TypeDefinition(
                                        primitive_type=VariableType.INT, masked=False
                                    ),
                                ),
                                access_level=WfRunVariableAccessLevel.PRIVATE_VAR,
                            ),
                        ],
                    ),
                    "son-thread": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[
                                    Edge(
                                        sink_node_name="1-grandchild-thread-START_THREAD",
                                        variable_mutations=[
                                            VariableMutation(
                                                lhs_name="grand-parent-var",
                                                rhs_assignment=VariableAssignment(
                                                    literal_value=VariableValue(
                                                        str="son-value"
                                                    )
                                                ),
                                            )
                                        ],
                                    )
                                ],
                            ),
                            "1-grandchild-thread-START_THREAD": Node(
                                start_thread=StartThreadNode(
                                    thread_spec_name="grandchild-thread"
                                ),
                                outgoing_edges=[
                                    Edge(
                                        sink_node_name="2-exit-EXIT",
                                        variable_mutations=[
                                            VariableMutation(
                                                lhs_name="1-grandchild-thread-START_THREAD",
                                                rhs_assignment=VariableAssignment(
                                                    node_output=VariableAssignment.NodeOutputReference(
                                                        node_name="1-grandchild-thread-START_THREAD"
                                                    )
                                                ),
                                            )
                                        ],
                                    )
                                ],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        },
                        variable_defs=[
                            ThreadVarDef(
                                var_def=VariableDef(
                                    name="1-grandchild-thread-START_THREAD",
                                    type_def=TypeDefinition(
                                        primitive_type=VariableType.INT, masked=False
                                    ),
                                ),
                                access_level=WfRunVariableAccessLevel.PRIVATE_VAR,
                            )
                        ],
                    ),
                    "grandchild-thread": ThreadSpec(
                        nodes={
                            "0-entrypoint-ENTRYPOINT": Node(
                                entrypoint=EntrypointNode(),
                                outgoing_edges=[
                                    Edge(
                                        sink_node_name="1-exit-EXIT",
                                        variable_mutations=[
                                            VariableMutation(
                                                lhs_name="grand-parent-var",
                                                rhs_assignment=VariableAssignment(
                                                    literal_value=VariableValue(
                                                        str="grandchild-value"
                                                    )
                                                ),
                                            )
                                        ],
                                    )
                                ],
                            ),
                            "1-exit-EXIT": Node(exit=ExitNode()),
                        }
                    ),
                },
            ),
        )


class TestRetries(unittest.TestCase):
    def test_add_retries(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.execute("my-task", retries=4)

        wf = Workflow("my-wf", my_entrypoint)
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
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="my-task"), retries=4
                                ),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                },
            ),
        )

    def test_add_retries_1_task(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.execute("my-task1", retries=4)
            thread.execute("my-task2")

        wf = Workflow("my-wf", my_entrypoint)
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
                                outgoing_edges=[Edge(sink_node_name="1-my-task1-TASK")],
                            ),
                            "1-my-task1-TASK": Node(
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="my-task1"), retries=4
                                ),
                                outgoing_edges=[Edge(sink_node_name="2-my-task2-TASK")],
                            ),
                            "2-my-task2-TASK": Node(
                                task=TaskNode(task_def_id=TaskDefId(name="my-task2")),
                                outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                            ),
                            "3-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                },
            ),
        )

    def test_add_retries_with_default(self):
        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.execute("my-task1")
            thread.execute("my-task2")

        wf = Workflow("my-wf", my_entrypoint)
        wf.with_retry_policy(4)
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
                                outgoing_edges=[Edge(sink_node_name="1-my-task1-TASK")],
                            ),
                            "1-my-task1-TASK": Node(
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="my-task1"), retries=4
                                ),
                                outgoing_edges=[Edge(sink_node_name="2-my-task2-TASK")],
                            ),
                            "2-my-task2-TASK": Node(
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="my-task2"), retries=4
                                ),
                                outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                            ),
                            "3-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                },
            ),
        )

    def test_add_retries_with_default_policy(self):
        policy = ExponentialBackoffRetryPolicy(
            base_interval_ms=1000, max_delay_ms=10000, multiplier=4
        )

        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.execute("my-task1")
            thread.execute("my-task2")

        wf = Workflow("my-wf", my_entrypoint)
        wf.with_retry_policy(2, exponential_backoff=policy)
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
                                outgoing_edges=[Edge(sink_node_name="1-my-task1-TASK")],
                            ),
                            "1-my-task1-TASK": Node(
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="my-task1"),
                                    retries=2,
                                    exponential_backoff=policy,
                                ),
                                outgoing_edges=[Edge(sink_node_name="2-my-task2-TASK")],
                            ),
                            "2-my-task2-TASK": Node(
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="my-task2"),
                                    retries=2,
                                    exponential_backoff=policy,
                                ),
                                outgoing_edges=[Edge(sink_node_name="3-exit-EXIT")],
                            ),
                            "3-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
                },
            ),
        )

    def test_add_retries_with_policy(self):
        policy = ExponentialBackoffRetryPolicy(
            base_interval_ms=1000, max_delay_ms=5000, multiplier=2
        )

        def my_entrypoint(thread: WorkflowThread) -> None:
            thread.execute(
                "my-task",
                retries=4,
                exponential_backoff=policy,
            )

        wf = Workflow("my-wf", my_entrypoint)
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
                                task=TaskNode(
                                    task_def_id=TaskDefId(name="my-task"),
                                    retries=4,
                                    exponential_backoff=policy,
                                ),
                                outgoing_edges=[Edge(sink_node_name="2-exit-EXIT")],
                            ),
                            "2-exit-EXIT": Node(exit=ExitNode()),
                        },
                    ),
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
        self.assertTrue(mutation.HasField("rhs_assignment"))

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

    def test_cancel_user_task_on_deadline(self):
        def wf_func(thread: WorkflowThread) -> None:
            uto = thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            )
            thread.cancel_user_task_run_after(uto, 60)

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task

        self.assertEqual(len(ut_node.actions), 1)

        action = ut_node.actions[0]
        self.assertEqual(action.delay_seconds.literal_value.int, 60)
        self.assertEqual(action.hook, UTActionTrigger.UTHook.ON_ARRIVAL)
        self.assertTrue(action.HasField("cancel"))
        self.assertFalse(action.HasField("reassign"))

    def test_cancel_user_task_on_deadline_after_assignment(self):
        def wf_func(thread: WorkflowThread) -> None:
            uto = thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            )
            thread.cancel_user_task_run_after_assignment(uto, 60)

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task

        self.assertEqual(len(ut_node.actions), 1)

        action = ut_node.actions[0]
        self.assertEqual(action.delay_seconds.literal_value.int, 60)
        self.assertEqual(action.hook, UTActionTrigger.UTHook.ON_TASK_ASSIGNED)
        self.assertTrue(action.HasField("cancel"))
        self.assertFalse(action.HasField("reassign"))

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
        self.assertEqual(action.hook, UTActionTrigger.ON_ARRIVAL)
        self.assertEqual(action.delay_seconds.literal_value.int, 60)
        self.assertTrue(action.HasField("task"))
        reminder_task = action.task
        self.assertEqual(reminder_task.task.task_def_id.name, "my-reminder-task")

    def test_reminder_task_on_assignment(self):
        def wf_func(thread: WorkflowThread) -> None:
            uto = thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            )
            thread.schedule_reminder_task_on_assignment(
                uto, 60, "my-reminder-task", "my-arg"
            )

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task

        self.assertEqual(len(ut_node.actions), 1)

        action = ut_node.actions[0]
        self.assertEqual(action.hook, UTActionTrigger.ON_TASK_ASSIGNED)
        self.assertEqual(action.delay_seconds.literal_value.int, 60)
        self.assertTrue(action.HasField("task"))
        reminder_task = action.task
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
            user_var = WfRunVariable("my-var", VariableType.STR, thread)
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
            user_var = WfRunVariable("my-var", VariableType.STR, thread)
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

    def test_on_cancellation_exception_name(self):
        def wf_func(thread: WorkflowThread) -> None:
            thread.assign_user_task(
                "my-user-task",
                user_id="asdf",
                user_group="my-group",
            ).with_on_cancellation_exception("no-response")

        wf = Workflow("my-wf", wf_func).compile()
        thread = wf.thread_specs[wf.entrypoint_thread_name]

        node = thread.nodes["1-my-user-task-USER_TASK"]
        ut_node = node.user_task
        self.assertEqual(
            ut_node.on_cancellation_exception_name,
            to_variable_assignment("no-response"),
        )

    def test_user_id_is_not_empty(self):
        def wf_func(thread: WorkflowThread) -> None:
            thread.assign_user_task("my-user-task", user_id="")

        with self.assertRaises(Exception) as ctx:
            Workflow("my-wf", wf_func).compile()
        self.assertIn("UserId can't be empty to assign_user_task()", str(ctx.exception))

    def test_user_group_is_not_empty(self):
        def wf_func(thread: WorkflowThread) -> None:
            thread.assign_user_task("my-user-task", user_group="  ")

        with self.assertRaises(Exception) as ctx:
            Workflow("my-wf", wf_func).compile()
        self.assertIn(
            "UserGroup can't be empty to assign_user_task()", str(ctx.exception)
        )


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


class ThrowEventNodeTest(unittest.TestCase):
    def test_throw_event_node(self):
        def wf_func(wf: WorkflowThread) -> None:
            var = wf.add_variable("my-var", VariableType.STR)
            wf.throw_event("my-event", var)
            wf.throw_event("another-event", "some-content")

        wf_spec = Workflow("throw-event-wf", wf_func).compile()

        self.assertEqual(len(wf_spec.thread_specs), 1)
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        self.assertEqual(len(entrypoint.nodes), 4)

        first_throw = entrypoint.nodes["1-throw-my-event-THROW_EVENT"]
        self.assertEqual(first_throw.throw_event.event_def_id.name, "my-event")
        self.assertEqual(first_throw.throw_event.content.variable_name, "my-var")

        second_throw = entrypoint.nodes["2-throw-another-event-THROW_EVENT"]
        self.assertEqual(second_throw.throw_event.event_def_id.name, "another-event")
        self.assertEqual(
            second_throw.throw_event.content.literal_value.str, "some-content"
        )


class TestWaitForThreads(unittest.TestCase):

    def test_wait_for_threads_handle_exception_on_child(self):
        def failure_handler(wf: WorkflowThread) -> None:
            wf.execute("some-task")

        def child_thread(wf: WorkflowThread) -> None:
            wf.execute("some-task")

        def wf_func(wf: WorkflowThread) -> None:
            child = wf.spawn_thread(child_thread, "child")
            result = wf.wait_for_threads(SpawnedThreads(fixed_threads=[child]))
            result.handle_exception_on_child(failure_handler, "my-exception")
            result.handle_exception_on_child(failure_handler)

        wf_spec = Workflow("some-wf", wf_func).compile()

        self.assertEqual(len(wf_spec.thread_specs), 4)
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        node = entrypoint.nodes["2-threads-WAIT_FOR_THREADS"]
        wftn = node.wait_for_threads

        self.assertEqual(len(wftn.per_thread_failure_handlers), 2)
        specific_handler = wftn.per_thread_failure_handlers[0]
        any_handler = wftn.per_thread_failure_handlers[1]

        self.assertEqual(specific_handler.specific_failure, "my-exception")
        self.assertEqual(
            specific_handler.handler_spec_name,
            "exn-handler-2-threads-WAIT_FOR_THREADS-my-exception",
        )

        self.assertEqual(
            any_handler.any_failure_of_type,
            FailureHandlerDef.LHFailureType.FAILURE_TYPE_EXCEPTION,
        )
        self.assertEqual(
            any_handler.handler_spec_name,
            "exn-handler-2-threads-WAIT_FOR_THREADS",
        )

    def test_wait_for_threads_handle_error_on_child(self):
        def error_handler(wf: WorkflowThread) -> None:
            wf.execute("some-task")

        def child_thread(wf: WorkflowThread) -> None:
            wf.execute("some-task")

        def wf_func(wf: WorkflowThread) -> None:
            child = wf.spawn_thread(child_thread, "child")
            result = wf.wait_for_threads(SpawnedThreads(fixed_threads=[child]))
            result.handle_error_on_child(error_handler, LHErrorType.TIMEOUT)
            result.handle_error_on_child(error_handler)

        wf_spec = Workflow("some-wf", wf_func).compile()

        self.assertEqual(len(wf_spec.thread_specs), 4)
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        node = entrypoint.nodes["2-threads-WAIT_FOR_THREADS"]
        wftn = node.wait_for_threads

        self.assertEqual(len(wftn.per_thread_failure_handlers), 2)
        timeout_handler = wftn.per_thread_failure_handlers[0]
        any_error_handler = wftn.per_thread_failure_handlers[1]

        self.assertEqual(
            timeout_handler.specific_failure, LHErrorType.Name(LHErrorType.TIMEOUT)
        )
        self.assertEqual(
            timeout_handler.handler_spec_name,
            "error-handler-2-threads-WAIT_FOR_THREADS-TIMEOUT",
        )

        self.assertEqual(
            any_error_handler.any_failure_of_type,
            FailureHandlerDef.LHFailureType.FAILURE_TYPE_ERROR,
        )
        self.assertEqual(
            any_error_handler.handler_spec_name,
            "error-handler-2-threads-WAIT_FOR_THREADS",
        )

    def test_wait_for_threads_handle_any_failure_on_child(self):
        def failure_handler(wf: WorkflowThread) -> None:
            wf.execute("some-task")

        def child_thread(wf: WorkflowThread) -> None:
            wf.execute("some-task")

        def wf_func(wf: WorkflowThread) -> None:
            child = wf.spawn_thread(child_thread, "child")
            result = wf.wait_for_threads(SpawnedThreads(fixed_threads=[child]))
            result.handle_any_failure_on_child(failure_handler)

        wf_spec = Workflow("some-wf", wf_func).compile()

        self.assertEqual(len(wf_spec.thread_specs), 3)
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        node = entrypoint.nodes["2-threads-WAIT_FOR_THREADS"]
        wftn = node.wait_for_threads

        self.assertEqual(len(wftn.per_thread_failure_handlers), 1)
        any_failure_handler = wftn.per_thread_failure_handlers[0]

        self.assertEqual(
            any_failure_handler.handler_spec_name,
            "failure-handler-2-threads-WAIT_FOR_THREADS-ANY_FAILURE",
        )


class DynamicTaskTest(unittest.TestCase):
    def test_dynamic_task(self):
        def wf_func(thread: WorkflowThread) -> None:
            my_var = thread.add_variable("my-var", VariableType.STR)
            thread.execute("some-static-task")

            format_str = thread.format("some-dynamic-task-{0}", my_var)
            thread.execute(format_str)
            thread.execute(my_var)

        wf = Workflow("obiwan", wf_func).compile()
        entrypoint = wf.thread_specs[wf.entrypoint_thread_name]

        static_node = entrypoint.nodes["1-some-static-task-TASK"]
        self.assertEqual(static_node.task.task_def_id.name, "some-static-task")

        format_str_node = entrypoint.nodes["2-some-dynamic-task-{0}-TASK"]
        self.assertEqual(
            format_str_node.task.dynamic_task.format_string.format.literal_value.str,
            "some-dynamic-task-{0}",
        )

        var_node = entrypoint.nodes["3-my-var-TASK"]
        self.assertEqual(var_node.task.dynamic_task.variable_name, "my-var")


class CorrelationIdTest(unittest.TestCase):
    def test_correlation_id(self):
        def wf_func(wf: WorkflowThread) -> None:
            wf.wait_for_event("some-event", correlation_id="asdf")

        wf = Workflow("obiwan", wf_func).compile()
        entrypoint = wf.thread_specs[wf.entrypoint_thread_name]

        static_node = entrypoint.nodes["1-some-event-EXTERNAL_EVENT"]
        self.assertEqual(
            static_node.external_event.correlation_key.literal_value.str,
            "asdf",
        )

    def test_correlation_id_with_var(self):
        def wf_func(wf: WorkflowThread) -> None:
            my_var = wf.declare_str("my-var")
            wf.wait_for_event("some-event", correlation_id=my_var)

        wf = Workflow("obiwan", wf_func).compile()
        entrypoint = wf.thread_specs[wf.entrypoint_thread_name]

        static_node = entrypoint.nodes["1-some-event-EXTERNAL_EVENT"]
        self.assertEqual(
            static_node.external_event.correlation_key.variable_name,
            "my-var",
        )


if __name__ == "__main__":
    unittest.main()
