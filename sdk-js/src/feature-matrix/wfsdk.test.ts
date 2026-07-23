/**
 * Feature matrix: wfsdk (workflow definition DSL).
 *
 * See sdk-js/PARITY_PLAN.md. Each test.todo is one capability of the Java
 * SDK's public API (referenced as `Java: Class#method`). Porting a feature
 * means converting its todo into a real test — primarily a golden test
 * asserting the compiled PutWfSpecRequest matches the Java SDK's output for
 * the same workflow. Do not delete entries; do not mark features done
 * anywhere else.
 *
 * The wfsdk is currently entirely missing from sdk-js.
 */

describe('wfsdk', () => {
  describe('workflow lifecycle', () => {
    test.todo('create a workflow from a name and entrypoint thread function — Java: Workflow.newWorkflow')
    test.todo('compile a workflow to a PutWfSpecRequest — Java: Workflow#compileWorkflow')
    test.todo('compile a workflow to JSON — Java: Workflow#compileWfToJson')
    test.todo('register a WfSpec with the server — Java: Workflow#registerWfSpec')
    test.todo('check whether a WfSpec exists (optionally by major version) — Java: Workflow#doesWfSpecExist')
    test.todo('save the compiled WfSpec to disk — Java: Workflow#compileAndSaveToDisk')
    test.todo('list TaskDef names required by the workflow — Java: Workflow#getRequiredTaskDefNames')
    test.todo('list ExternalEventDef names required by the workflow — Java: Workflow#getRequiredExternalEventDefNames')
    test.todo('list child WfSpec names required by the workflow — Java: Workflow#getRequiredChildWfSpecNames')
    test.todo('list WorkflowEventDef names required by the workflow — Java: Workflow#getRequiredWorkflowEventDefNames')
    test.todo('collect ExternalEventDefs to auto-register — Java: Workflow#getExternalEventDefsToRegister')
    test.todo('collect WorkflowEventDefs to auto-register — Java: Workflow#getWorkflowEventDefsToRegister')
    test.todo('set a parent WfSpec — Java: Workflow#setParent')
    test.todo('set the default task timeout for all task nodes — Java: Workflow#setDefaultTaskTimeout')
    test.todo('set default simple retries for all task nodes — Java: Workflow#setDefaultTaskRetries')
    test.todo('set a default exponential backoff retry policy — Java: Workflow#setDefaultTaskExponentialBackoffPolicy')
    test.todo('set the workflow retention policy — Java: Workflow#withRetentionPolicy')
    test.todo('set the default thread retention policy — Java: Workflow#withDefaultThreadRetentionPolicy')
    test.todo('restrict allowed WfSpec update types — Java: Workflow#withUpdateType')
  })

  describe('variables', () => {
    test.todo('declare a STR variable — Java: WorkflowThread#declareStr')
    test.todo('declare an INT variable — Java: WorkflowThread#declareInt')
    test.todo('declare a DOUBLE variable — Java: WorkflowThread#declareDouble')
    test.todo('declare a BOOL variable — Java: WorkflowThread#declareBool')
    test.todo('declare a BYTES variable — Java: WorkflowThread#declareBytes')
    test.todo('declare a TIMESTAMP variable — Java: WorkflowThread#declareTimestamp')
    test.todo('declare a JSON_OBJ variable — Java: WorkflowThread#declareJsonObj')
    test.todo('declare a JSON_ARR variable — Java: WorkflowThread#declareJsonArr')
    test.todo('declare a typed array variable — Java: WorkflowThread#declareArray')
    test.todo('declare a typed map variable — Java: WorkflowThread#declareMap')
    test.todo('declare a struct variable by StructDef name (and version) — Java: WorkflowThread#declareStruct')
    test.todo('declare a variable from a type or default value — Java: WorkflowThread#addVariable')
    test.todo('give a variable a default value — Java: WfRunVariable#withDefault')
    test.todo('mark a variable required as workflow input — Java: WfRunVariable#required')
    test.todo('mark a variable searchable (indexed) — Java: WfRunVariable#searchable')
    test.todo('index a JSON field of a variable for search — Java: WfRunVariable#searchableOn')
    test.todo('mask a variable value in logs/API output — Java: WfRunVariable#masked')
    test.todo('set variable access level PUBLIC_VAR — Java: WfRunVariable#asPublic')
    test.todo('set variable access level INHERITED_VAR — Java: WfRunVariable#asInherited')
    test.todo('set an explicit variable access level — Java: WfRunVariable#withAccessLevel')
    test.todo('read a nested JSON field via jsonPath — Java: WfRunVariable#jsonPath')
    test.todo('read a struct/JSON field via get(field) — Java: WfRunVariable#get(String)')
    test.todo('read an array element via get(index) — Java: WfRunVariable#get(int)')
    test.todo('assign an expression result to a variable — Java: WfRunVariable#assign')
  })

  describe('task nodes', () => {
    test.todo('execute a task by TaskDef name with args — Java: WorkflowThread#execute(String, ...)')
    test.todo(
      'execute a task whose name comes from a variable (dynamic task) — Java: WorkflowThread#execute(WfRunVariable, ...)'
    )
    test.todo('execute a task whose name is a format string — Java: WorkflowThread#execute(LHFormatString, ...)')
    test.todo('set a per-node task timeout — Java: TaskNodeOutput#timeout')
    test.todo('set per-node simple retries — Java: TaskNodeOutput#withRetries')
    test.todo('set a per-node exponential backoff retry policy — Java: TaskNodeOutput#withExponentialBackoff')
    test.todo('read task output via jsonPath — Java: NodeOutput#jsonPath')
    test.todo('read a field of task output via get — Java: NodeOutput#get')
  })

  describe('control flow', () => {
    test.todo('build a condition from lhs/comparator/rhs — Java: WorkflowThread#condition')
    test.todo('conditionally execute a body — Java: WorkflowThread#doIf')
    test.todo('chain an else-if branch — Java: WorkflowIfStatement#doElseIf')
    test.todo('chain an else branch — Java: WorkflowIfStatement#doElse')
    test.todo('if/else in one call — Java: WorkflowThread#doIfElse')
    test.todo('loop while a condition holds — Java: WorkflowThread#doWhile')
    test.todo('complete the thread early (optionally with output) — Java: WorkflowThread#complete')
    test.todo('fail the thread with a named failure and message — Java: WorkflowThread#fail')
    test.todo('sleep for N seconds — Java: WorkflowThread#sleepSeconds')
    test.todo('sleep until a timestamp variable — Java: WorkflowThread#sleepUntil')
    test.todo('block until an expression becomes true — Java: WorkflowThread#waitForCondition')
  })

  describe('expressions and mutations', () => {
    test.todo('arithmetic: add — Java: LHExpression#add')
    test.todo('arithmetic: subtract — Java: LHExpression#subtract')
    test.todo('arithmetic: multiply — Java: LHExpression#multiply')
    test.todo('arithmetic: divide — Java: LHExpression#divide')
    test.todo('arithmetic: pow — Java: LHExpression#pow')
    test.todo('extend a string/array — Java: LHExpression#extend')
    test.todo('remove an element if present — Java: LHExpression#removeIfPresent')
    test.todo('remove an array element by index — Java: LHExpression#removeIndex')
    test.todo('remove a map/JSON key — Java: LHExpression#removeKey')
    test.todo('size of a collection/string — Java: LHExpression#size')
    test.todo(
      'comparisons: isLessThan / isGreaterThan / isLessThanEq / isGreaterThanEq — Java: LHExpression, WfRunVariable'
    )
    test.todo('comparisons: isEqualTo / isNotEqualTo — Java: LHExpression#isEqualTo')
    test.todo('membership: doesContain / doesNotContain — Java: LHExpression#doesContain')
    test.todo('membership: isIn / isNotIn — Java: LHExpression#isIn')
    test.todo('boolean combinators: and / or — Java: LHExpression#and')
    test.todo('cast to INT / DOUBLE / STR / BOOL / BYTES / WF_RUN_ID — Java: LHExpression#castToInt etc.')
    test.todo('cast to an arbitrary VariableType — Java: LHExpression#castTo')
    test.todo('mutate a variable with an explicit VariableMutationType — Java: WorkflowThread#mutate')
    test.todo('build a format string from variables — Java: WorkflowThread#format')
  })

  describe('external events', () => {
    test.todo('wait for an external event — Java: WorkflowThread#waitForEvent')
    test.todo('set a timeout on an external event wait — Java: ExternalEventNodeOutput#timeout')
    test.todo('declare the event payload type for auto-registration — Java: ExternalEventNodeOutput#registeredAs')
    test.todo('correlate an event by id (optionally masked) — Java: ExternalEventNodeOutput#withCorrelationId')
    test.todo('configure correlated event behavior — Java: ExternalEventNodeOutput#withCorrelatedEventConfig')
  })

  describe('workflow events', () => {
    test.todo('throw a workflow event with content — Java: WorkflowThread#throwEvent')
    test.todo('declare the thrown event payload type for auto-registration — Java: ThrowEventNodeOutput#registeredAs')
  })

  describe('child threads', () => {
    test.todo('spawn a child thread with input variables — Java: WorkflowThread#spawnThread')
    test.todo('spawn one thread per element of an array variable — Java: WorkflowThread#spawnThreadForEach')
    test.todo('wait for all spawned threads — Java: WorkflowThread#waitForThreads')
    test.todo('wait for any of the spawned threads — Java: WorkflowThread#waitForAnyOf')
    test.todo('wait for the first of the spawned threads — Java: WorkflowThread#waitForFirstOf')
    test.todo('combine spawned threads into one handle — Java: SpawnedThreads.of')
    test.todo('access the spawned thread-number variable — Java: SpawnedThread#getThreadNumberVariable')
    test.todo('handle an error on a child thread — Java: WaitForThreadsNodeOutput#handleErrorOnChild')
    test.todo('handle a named exception on a child thread — Java: WaitForThreadsNodeOutput#handleExceptionOnChild')
    test.todo('handle any failure on a child thread — Java: WaitForThreadsNodeOutput#handleAnyFailureOnChild')
    test.todo('set a per-thread retention policy — Java: WorkflowThread#withRetentionPolicy')
  })

  describe('child workflows', () => {
    test.todo('run a child workflow with inputs — Java: WorkflowThread#runWf')
    test.todo('wait for a spawned child workflow — Java: WorkflowThread#waitForChildWf')
  })

  describe('interrupts', () => {
    test.todo('register an interrupt handler for an external event — Java: WorkflowThread#registerInterruptHandler')
  })

  describe('failure handling', () => {
    test.todo('handle any technical ERROR on a node — Java: WorkflowThread#handleError(NodeOutput, ThreadFunc)')
    test.todo(
      'handle a specific LHErrorType on a node — Java: WorkflowThread#handleError(NodeOutput, LHErrorType, ThreadFunc)'
    )
    test.todo('handle any business EXCEPTION on a node — Java: WorkflowThread#handleException(NodeOutput, ThreadFunc)')
    test.todo(
      'handle a named business EXCEPTION on a node — Java: WorkflowThread#handleException(NodeOutput, String, ThreadFunc)'
    )
    test.todo('handle any failure (error or exception) on a node — Java: WorkflowThread#handleAnyFailure')
  })

  describe('user tasks', () => {
    test.todo('assign a user task to a user and/or group — Java: WorkflowThread#assignUserTask')
    test.todo('attach notes to a user task (string, variable, or format string) — Java: UserTaskOutput#withNotes')
    test.todo('reassign a user task after a deadline — Java: WorkflowThread#reassignUserTask')
    test.todo('release an assigned task back to its group on deadline — Java: WorkflowThread#releaseToGroupOnDeadline')
    test.todo('schedule a reminder task after a delay — Java: WorkflowThread#scheduleReminderTask')
    test.todo('schedule a reminder task on assignment — Java: WorkflowThread#scheduleReminderTaskOnAssignment')
    test.todo('cancel a user task run after a delay — Java: WorkflowThread#cancelUserTaskRunAfter')
    test.todo(
      'cancel a user task run after assignment plus delay — Java: WorkflowThread#cancelUserTaskRunAfterAssignment'
    )
  })

  describe('structs', () => {
    test.todo(
      'build a struct value for a registered StructDef (by name and version) — Java: WorkflowThread#buildStruct, LHStructBuilder#put'
    )
    test.todo(
      'build an inline (schemaless) struct value — Java: WorkflowThread#buildInlineStruct, InlineLHStructBuilder#put'
    )
    test.todo('nest struct builders inside struct fields — Java: LHStructBuilder#put(String, InlineLHStructBuilder)')
  })
})
