import { Comparator } from '../proto/type_definition'
import {
  ExponentialBackoffRetryPolicy,
  UTActionTrigger,
  UTActionTrigger_UTHook,
  VariableAssignment,
  VariableAssignment_Expression,
  VariableMutation,
  VariableMutationType,
} from '../proto/common_wfspec'
import { LHErrorType, VariableType } from '../proto/common_enums'
import { AllowedUpdateType, PutWfSpecRequest } from '../proto/service'
import {
  FailureHandlerDef_LHFailureType,
  Node,
  ThreadRetentionPolicy,
  WaitForThreadsStrategy,
  WfRunVariableAccessLevel,
  WorkflowRetentionPolicy,
} from '../proto/wf_spec'
import { spawnedThreadsOf, ThreadFunc, Workflow, WorkflowThread } from '../wfsdk'
import { expectMatchesGolden } from './golden'
import { referenceWorkflows } from './referenceWorkflows'

/**
 * Feature matrix: wfsdk (workflow definition DSL).
 *
 * See sdk-js/PARITY_PLAN.md. Each test is one capability of the Java SDK's
 * public API (referenced as `Java: Class#method`). test.todo = not yet
 * implemented/proven. Real tests are proven either by a golden comparison
 * against the Java SDK's compiled output (provenByGolden) or by a focused
 * unit assertion on the compiled proto for features the goldens don't cover.
 * Do not delete entries; do not mark features done anywhere else.
 */

/** Asserts the named reference workflow still matches its Java golden. */
function provenByGolden(name: string): void {
  expectMatchesGolden(referenceWorkflows[name]().compileWorkflow(), name)
}

let wfCounter = 0
function compile(func: ThreadFunc): PutWfSpecRequest {
  return Workflow.newWorkflow(`matrix-test-${wfCounter++}`, func).compileWorkflow()
}

function entrypointOf(spec: PutWfSpecRequest) {
  return spec.threadSpecs[spec.entrypointThreadName]
}

function nodeOf(spec: PutWfSpecRequest, name: string): Node {
  const node = entrypointOf(spec).nodes[name]
  expect(node).toBeDefined()
  return node
}

function taskNodeOf(spec: PutWfSpecRequest, name: string) {
  const node = nodeOf(spec, name)
  if (node.node.oneofKind !== 'task') throw new Error(`${name} is not a TASK node`)
  return node.node.task
}

/** Compiles `target.assign(<value built by factory>)` and returns the RHS assignment. */
function compileAssignment(valueFactory: (wf: WorkflowThread) => unknown): VariableAssignment {
  const spec = compile(wf => {
    const target = wf.declareJsonObj('target')
    target.assign(valueFactory(wf))
    wf.execute('noop')
  })
  // The mutation is drained onto the edge created by the next node after
  // assign(), which node that is depends on what the factory built — scan.
  const mutations: VariableMutation[] = Object.values(entrypointOf(spec).nodes).flatMap(node =>
    node.outgoingEdges.flatMap(edge => edge.variableMutations)
  )
  expect(mutations).toHaveLength(1)
  if (mutations[0].rhsValue.oneofKind !== 'rhsAssignment') throw new Error('Expected rhsAssignment')
  return mutations[0].rhsValue.rhsAssignment
}

function expressionOf(assignment: VariableAssignment): VariableAssignment_Expression {
  if (assignment.source.oneofKind !== 'expression') throw new Error('Expected an expression assignment')
  return assignment.source.expression
}

describe('wfsdk', () => {
  describe('workflow lifecycle', () => {
    test('create a workflow from a name and entrypoint thread function — Java: Workflow.newWorkflow', () => {
      provenByGolden('basic')
    })

    test('compile a workflow to a PutWfSpecRequest — Java: Workflow#compileWorkflow', () => {
      provenByGolden('basic')
    })

    test('compile a workflow to JSON — Java: Workflow#compileWfToJson', () => {
      const json = referenceWorkflows['basic']().compileWfToJson()
      expect(JSON.parse(json).name).toBe('golden-basic')
    })

    test.todo('register a WfSpec with the server — Java: Workflow#registerWfSpec')
    test.todo('check whether a WfSpec exists (optionally by major version) — Java: Workflow#doesWfSpecExist')
    test.todo('save the compiled WfSpec to disk — Java: Workflow#compileAndSaveToDisk')

    test('list TaskDef names required by the workflow — Java: Workflow#getRequiredTaskDefNames', () => {
      expect(referenceWorkflows['basic']().getRequiredTaskDefNames()).toEqual(new Set(['greet']))
    })

    test('list ExternalEventDef names required by the workflow — Java: Workflow#getRequiredExternalEventDefNames', () => {
      expect(referenceWorkflows['external-events']().getRequiredExternalEventDefNames()).toEqual(
        new Set(['payment-received'])
      )
      expect(referenceWorkflows['interrupts']().getRequiredExternalEventDefNames()).toEqual(
        new Set(['cancel-requested'])
      )
    })

    test('list child WfSpec names required by the workflow — Java: Workflow#getRequiredChildWfSpecNames', () => {
      expect(referenceWorkflows['child-workflow']().getRequiredChildWfSpecNames()).toEqual(new Set(['shipping-wf']))
    })

    test('list WorkflowEventDef names required by the workflow — Java: Workflow#getRequiredWorkflowEventDefNames', () => {
      expect(referenceWorkflows['sleep-and-events']().getRequiredWorkflowEventDefNames()).toEqual(
        new Set(['milestone-reached'])
      )
    })

    test.todo('collect ExternalEventDefs to auto-register — Java: Workflow#getExternalEventDefsToRegister')
    test.todo('collect WorkflowEventDefs to auto-register — Java: Workflow#getWorkflowEventDefsToRegister')

    test('set a parent WfSpec — Java: Workflow#setParent', () => {
      const wf = Workflow.newWorkflow('parent-test', t => {
        t.execute('a')
      })
      wf.setParent('the-parent')
      expect(wf.compileWorkflow().parentWfSpec?.wfSpecName).toBe('the-parent')
    })

    test('set the default task timeout for all task nodes — Java: Workflow#setDefaultTaskTimeout', () => {
      const wf = Workflow.newWorkflow('timeout-test', t => {
        t.execute('a')
      })
      wf.setDefaultTaskTimeout(99)
      expect(taskNodeOf(wf.compileWorkflow(), '1-a-TASK').timeoutSeconds).toBe(99)
    })

    test('set default simple retries for all task nodes — Java: Workflow#setDefaultTaskRetries', () => {
      const wf = Workflow.newWorkflow('retries-test', t => {
        t.execute('a')
      })
      wf.setDefaultTaskRetries(2)
      expect(taskNodeOf(wf.compileWorkflow(), '1-a-TASK').retries).toBe(2)
    })

    test('set a default exponential backoff retry policy — Java: Workflow#setDefaultTaskExponentialBackoffPolicy', () => {
      const policy = ExponentialBackoffRetryPolicy.create({ baseIntervalMs: 500, maxDelayMs: '10000', multiplier: 2 })
      const wf = Workflow.newWorkflow('backoff-test', t => {
        t.execute('a')
      })
      wf.setDefaultTaskExponentialBackoffPolicy(policy)
      expect(taskNodeOf(wf.compileWorkflow(), '1-a-TASK').exponentialBackoff).toEqual(policy)
    })

    test('set the workflow retention policy — Java: Workflow#withRetentionPolicy', () => {
      const policy = WorkflowRetentionPolicy.create({
        wfGcPolicy: { oneofKind: 'secondsAfterWfTermination', secondsAfterWfTermination: '3600' },
      })
      const wf = Workflow.newWorkflow('retention-test', t => {
        t.execute('a')
      }).withRetentionPolicy(policy)
      expect(wf.compileWorkflow().retentionPolicy).toEqual(policy)
    })

    test('set the default thread retention policy — Java: Workflow#withDefaultThreadRetentionPolicy', () => {
      const policy = ThreadRetentionPolicy.create({
        threadGcPolicy: { oneofKind: 'secondsAfterThreadTermination', secondsAfterThreadTermination: '600' },
      })
      const wf = Workflow.newWorkflow('thread-retention-test', t => {
        t.execute('a')
      }).withDefaultThreadRetentionPolicy(policy)
      expect(entrypointOf(wf.compileWorkflow()).retentionPolicy).toEqual(policy)
    })

    test('restrict allowed WfSpec update types — Java: Workflow#withUpdateType', () => {
      const wf = Workflow.newWorkflow('update-type-test', t => {
        t.execute('a')
      }).withUpdateType(AllowedUpdateType.NO_UPDATES)
      expect(wf.compileWorkflow().allowedUpdates).toBe(AllowedUpdateType.NO_UPDATES)
    })
  })

  describe('variables', () => {
    test('declare a STR variable — Java: WorkflowThread#declareStr', () => {
      provenByGolden('variables')
    })

    test('declare an INT variable — Java: WorkflowThread#declareInt', () => {
      provenByGolden('variables')
    })

    test('declare a DOUBLE variable — Java: WorkflowThread#declareDouble', () => {
      provenByGolden('variables')
    })

    test('declare a BOOL variable — Java: WorkflowThread#declareBool', () => {
      provenByGolden('variables')
    })

    test('declare a BYTES variable — Java: WorkflowThread#declareBytes', () => {
      provenByGolden('variables')
    })

    test('declare a TIMESTAMP variable — Java: WorkflowThread#declareTimestamp', () => {
      provenByGolden('variables')
    })

    test('declare a JSON_OBJ variable — Java: WorkflowThread#declareJsonObj', () => {
      provenByGolden('variables')
    })

    test('declare a JSON_ARR variable — Java: WorkflowThread#declareJsonArr', () => {
      provenByGolden('variables')
    })

    test.todo('declare a typed array variable — Java: WorkflowThread#declareArray')
    test.todo('declare a typed map variable — Java: WorkflowThread#declareMap')
    test.todo('declare a struct variable by StructDef name (and version) — Java: WorkflowThread#declareStruct')

    test('declare a variable from a type or default value — Java: WorkflowThread#addVariable', () => {
      const spec = compile(wf => {
        wf.addVariable('greeting', 'hello')
        wf.execute('noop')
      })
      const varDef = entrypointOf(spec).variableDefs[0].varDef!
      expect(varDef.typeDef?.definedType).toEqual({ oneofKind: 'primitiveType', primitiveType: VariableType.STR })
      expect(varDef.defaultValue?.value).toEqual({ oneofKind: 'str', str: 'hello' })
    })

    test('give a variable a default value — Java: WfRunVariable#withDefault', () => {
      provenByGolden('variables')
    })

    test('mark a variable required as workflow input — Java: WfRunVariable#required', () => {
      provenByGolden('variables')
    })

    test('mark a variable searchable (indexed) — Java: WfRunVariable#searchable', () => {
      provenByGolden('variables')
    })

    test('index a JSON field of a variable for search — Java: WfRunVariable#searchableOn', () => {
      provenByGolden('variables')
    })

    test('mask a variable value in logs/API output — Java: WfRunVariable#masked', () => {
      provenByGolden('variables')
    })

    test('set variable access level PUBLIC_VAR — Java: WfRunVariable#asPublic', () => {
      provenByGolden('variables')
    })

    test('set variable access level INHERITED_VAR — Java: WfRunVariable#asInherited', () => {
      const spec = compile(wf => {
        wf.declareStr('inherited').asInherited()
        wf.execute('noop')
      })
      expect(entrypointOf(spec).variableDefs[0].accessLevel).toBe(WfRunVariableAccessLevel.INHERITED_VAR)
    })

    test('set an explicit variable access level — Java: WfRunVariable#withAccessLevel', () => {
      const spec = compile(wf => {
        wf.declareStr('explicit').withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR)
        wf.execute('noop')
      })
      expect(entrypointOf(spec).variableDefs[0].accessLevel).toBe(WfRunVariableAccessLevel.PUBLIC_VAR)
    })

    test('read a nested JSON field via jsonPath — Java: WfRunVariable#jsonPath', () => {
      provenByGolden('conditionals')
    })

    test('read a struct/JSON field via get(field) — Java: WfRunVariable#get(String)', () => {
      const assignment = compileAssignment(wf => wf.declareJsonObj('obj').get('someField'))
      expect(assignment.source).toEqual({ oneofKind: 'variableName', variableName: 'obj' })
      expect(assignment.path).toEqual({
        oneofKind: 'lhPath',
        lhPath: { path: [{ selectorType: { oneofKind: 'key', key: 'someField' } }] },
      })
    })

    test('read an array element via get(index) — Java: WfRunVariable#get(int)', () => {
      const assignment = compileAssignment(wf => wf.declareJsonArr('arr').get(3))
      expect(assignment.path).toEqual({
        oneofKind: 'lhPath',
        lhPath: { path: [{ selectorType: { oneofKind: 'index', index: 3 } }] },
      })
    })

    test('assign an expression result to a variable — Java: WfRunVariable#assign', () => {
      provenByGolden('expressions')
    })
  })

  describe('task nodes', () => {
    test('execute a task by TaskDef name with args — Java: WorkflowThread#execute(String, ...)', () => {
      provenByGolden('basic')
    })

    test('execute a task whose name comes from a variable (dynamic task) — Java: WorkflowThread#execute(WfRunVariable, ...)', () => {
      const spec = compile(wf => {
        const taskName = wf.declareStr('task-name')
        wf.execute(taskName)
      })
      const task = taskNodeOf(spec, '1-task-name-TASK')
      expect(task.taskToExecute.oneofKind).toBe('dynamicTask')
    })

    test('execute a task whose name is a format string — Java: WorkflowThread#execute(LHFormatString, ...)', () => {
      const spec = compile(wf => {
        const suffix = wf.declareStr('suffix')
        wf.execute(wf.format('task-{0}', suffix))
      })
      const task = taskNodeOf(spec, '1-task-{0}-TASK')
      expect(task.taskToExecute.oneofKind).toBe('dynamicTask')
    })

    test('set a per-node task timeout — Java: TaskNodeOutput#timeout', () => {
      const spec = compile(wf => {
        wf.execute('slow-task').timeout(60)
      })
      expect(taskNodeOf(spec, '1-slow-task-TASK').timeoutSeconds).toBe(60)
    })

    test('set per-node simple retries — Java: TaskNodeOutput#withRetries', () => {
      provenByGolden('failure-handling')
    })

    test('set a per-node exponential backoff retry policy — Java: TaskNodeOutput#withExponentialBackoff', () => {
      const policy = ExponentialBackoffRetryPolicy.create({ baseIntervalMs: 500, maxDelayMs: '10000', multiplier: 2 })
      const spec = compile(wf => {
        wf.execute('flaky').withExponentialBackoff(policy)
      })
      expect(taskNodeOf(spec, '1-flaky-TASK').exponentialBackoff).toEqual(policy)
    })

    test('read task output via jsonPath — Java: NodeOutput#jsonPath', () => {
      const assignment = compileAssignment(wf => wf.execute('produce').jsonPath('$.result'))
      expect(assignment.source.oneofKind).toBe('nodeOutput')
      expect(assignment.path).toEqual({ oneofKind: 'jsonPath', jsonPath: '$.result' })
    })

    test('read a field of task output via get — Java: NodeOutput#get', () => {
      const assignment = compileAssignment(wf => wf.execute('produce').get('result'))
      expect(assignment.source.oneofKind).toBe('nodeOutput')
      expect(assignment.path).toEqual({
        oneofKind: 'lhPath',
        lhPath: { path: [{ selectorType: { oneofKind: 'key', key: 'result' } }] },
      })
    })
  })

  describe('control flow', () => {
    test('build a condition from lhs/comparator/rhs — Java: WorkflowThread#condition', () => {
      provenByGolden('conditionals')
    })

    test('conditionally execute a body — Java: WorkflowThread#doIf', () => {
      provenByGolden('conditionals')
    })

    test('chain an else-if branch — Java: WorkflowIfStatement#doElseIf', () => {
      provenByGolden('conditionals')
    })

    test('chain an else branch — Java: WorkflowIfStatement#doElse', () => {
      provenByGolden('conditionals')
    })

    test('if/else in one call — Java: WorkflowThread#doIfElse', () => {
      provenByGolden('conditionals')
    })

    test('loop while a condition holds — Java: WorkflowThread#doWhile', () => {
      provenByGolden('while-loop')
    })

    test('complete the thread early (optionally with output) — Java: WorkflowThread#complete', () => {
      provenByGolden('sleep-and-events')
      const spec = compile(wf => {
        const out = wf.declareStr('out')
        wf.complete(out)
      })
      const node = nodeOf(spec, '1-complete-EXIT')
      if (node.node.oneofKind !== 'exit') throw new Error('expected exit node')
      expect(node.node.exit.result.oneofKind).toBe('returnContent')
    })

    test('fail the thread with a named failure and message — Java: WorkflowThread#fail', () => {
      provenByGolden('failure-handling')
    })

    test('sleep for N seconds — Java: WorkflowThread#sleepSeconds', () => {
      provenByGolden('sleep-and-events')
    })

    test('sleep until a timestamp variable — Java: WorkflowThread#sleepUntil', () => {
      provenByGolden('sleep-and-events')
    })

    test('block until an expression becomes true — Java: WorkflowThread#waitForCondition', () => {
      provenByGolden('sleep-and-events')
    })
  })

  describe('expressions and mutations', () => {
    test('arithmetic: add — Java: LHExpression#add', () => {
      provenByGolden('expressions')
    })

    test('arithmetic: subtract — Java: LHExpression#subtract', () => {
      provenByGolden('expressions')
    })

    test('arithmetic: multiply — Java: LHExpression#multiply', () => {
      provenByGolden('expressions')
    })

    test('arithmetic: divide — Java: LHExpression#divide', () => {
      provenByGolden('expressions')
    })

    test('arithmetic: pow — Java: LHExpression#pow', () => {
      const expr = expressionOf(compileAssignment(wf => wf.declareInt('x').pow(2)))
      expect(expr.operation).toEqual({ oneofKind: 'mutationType', mutationType: VariableMutationType.POW })
    })

    test('extend a string/array — Java: LHExpression#extend', () => {
      const expr = expressionOf(compileAssignment(wf => wf.declareJsonArr('a').extend([1])))
      expect(expr.operation).toEqual({ oneofKind: 'mutationType', mutationType: VariableMutationType.EXTEND })
    })

    test('remove an element if present — Java: LHExpression#removeIfPresent', () => {
      const expr = expressionOf(compileAssignment(wf => wf.declareJsonArr('a').removeIfPresent('x')))
      expect(expr.operation).toEqual({
        oneofKind: 'mutationType',
        mutationType: VariableMutationType.REMOVE_IF_PRESENT,
      })
    })

    test('remove an array element by index — Java: LHExpression#removeIndex', () => {
      provenByGolden('expressions')
    })

    test('remove a map/JSON key — Java: LHExpression#removeKey', () => {
      const expr = expressionOf(compileAssignment(wf => wf.declareJsonObj('o').removeKey('k')))
      expect(expr.operation).toEqual({ oneofKind: 'mutationType', mutationType: VariableMutationType.REMOVE_KEY })
    })

    test('size of a collection/string — Java: LHExpression#size', () => {
      provenByGolden('expressions')
    })

    test('comparisons: isLessThan / isGreaterThan / isLessThanEq / isGreaterThanEq — Java: LHExpression, WfRunVariable', () => {
      const cases: Array<[(wf: WorkflowThread) => unknown, Comparator]> = [
        [wf => wf.declareInt('a').isLessThan(5), Comparator.LESS_THAN],
        [wf => wf.declareInt('b').isGreaterThan(5), Comparator.GREATER_THAN],
        [wf => wf.declareInt('c').isLessThanEq(5), Comparator.LESS_THAN_EQ],
        [wf => wf.declareInt('d').isGreaterThanEq(5), Comparator.GREATER_THAN_EQ],
      ]
      for (const [factory, comparator] of cases) {
        const expr = expressionOf(compileAssignment(factory))
        expect(expr.operation).toEqual({ oneofKind: 'comparator', comparator })
      }
    })

    test('comparisons: isEqualTo / isNotEqualTo — Java: LHExpression#isEqualTo', () => {
      provenByGolden('conditionals')
      const expr = expressionOf(compileAssignment(wf => wf.declareInt('x').isNotEqualTo(5)))
      expect(expr.operation).toEqual({ oneofKind: 'comparator', comparator: Comparator.NOT_EQUALS })
    })

    test('membership: doesContain / doesNotContain — Java: LHExpression#doesContain', () => {
      // Note operand order: doesContain(x) compiles to (x IN this).
      const expr = expressionOf(compileAssignment(wf => wf.declareJsonArr('haystack').doesContain('needle')))
      expect(expr.operation).toEqual({ oneofKind: 'comparator', comparator: Comparator.IN })
      expect(expr.rhs?.source).toEqual({ oneofKind: 'variableName', variableName: 'haystack' })

      const negated = expressionOf(compileAssignment(wf => wf.declareJsonArr('h2').doesNotContain('needle')))
      expect(negated.operation).toEqual({ oneofKind: 'comparator', comparator: Comparator.NOT_IN })
    })

    test('membership: isIn / isNotIn — Java: LHExpression#isIn', () => {
      const expr = expressionOf(compileAssignment(wf => wf.declareStr('item').isIn(['a', 'b'])))
      expect(expr.operation).toEqual({ oneofKind: 'comparator', comparator: Comparator.IN })
      expect(expr.lhs?.source).toEqual({ oneofKind: 'variableName', variableName: 'item' })

      const negated = expressionOf(compileAssignment(wf => wf.declareStr('item2').isNotIn(['a', 'b'])))
      expect(negated.operation).toEqual({ oneofKind: 'comparator', comparator: Comparator.NOT_IN })
    })

    test('boolean combinators: and / or — Java: LHExpression#and', () => {
      const and = expressionOf(compileAssignment(wf => wf.declareBool('p').and(wf.declareBool('q'))))
      expect(and.operation).toEqual({ oneofKind: 'mutationType', mutationType: VariableMutationType.AND })

      const or = expressionOf(compileAssignment(wf => wf.declareBool('r').or(wf.declareBool('s'))))
      expect(or.operation).toEqual({ oneofKind: 'mutationType', mutationType: VariableMutationType.OR })
    })

    test('cast to INT / DOUBLE / STR / BOOL / BYTES / WF_RUN_ID — Java: LHExpression#castToInt etc.', () => {
      provenByGolden('expressions') // castToDouble
      const cases: Array<[(wf: WorkflowThread) => unknown, VariableType]> = [
        [wf => wf.declareStr('a').castToInt(), VariableType.INT],
        [wf => wf.declareInt('b').castToStr(), VariableType.STR],
        [wf => wf.declareStr('c').castToBool(), VariableType.BOOL],
        [wf => wf.declareStr('d').castToBytes(), VariableType.BYTES],
        [wf => wf.declareStr('e').castToWfRunId(), VariableType.WF_RUN_ID],
      ]
      for (const [factory, expected] of cases) {
        const assignment = compileAssignment(factory)
        expect(assignment.targetType?.definedType).toEqual({ oneofKind: 'primitiveType', primitiveType: expected })
      }
    })

    test('cast to an arbitrary VariableType — Java: LHExpression#castTo', () => {
      const assignment = compileAssignment(wf => wf.declareStr('ts').castTo(VariableType.TIMESTAMP))
      expect(assignment.targetType?.definedType).toEqual({
        oneofKind: 'primitiveType',
        primitiveType: VariableType.TIMESTAMP,
      })
    })

    test('mutate a variable with an explicit VariableMutationType — Java: WorkflowThread#mutate', () => {
      provenByGolden('expressions')
    })

    test('build a format string from variables — Java: WorkflowThread#format', () => {
      provenByGolden('expressions')
    })
  })

  describe('external events', () => {
    test('wait for an external event — Java: WorkflowThread#waitForEvent', () => {
      provenByGolden('external-events')
    })

    test('set a timeout on an external event wait — Java: ExternalEventNodeOutput#timeout', () => {
      provenByGolden('external-events')
    })

    test.todo('declare the event payload type for auto-registration — Java: ExternalEventNodeOutput#registeredAs')

    test('correlate an event by id (optionally masked) — Java: ExternalEventNodeOutput#withCorrelationId', () => {
      provenByGolden('external-events')
    })

    test.todo('configure correlated event behavior — Java: ExternalEventNodeOutput#withCorrelatedEventConfig')
  })

  describe('workflow events', () => {
    test('throw a workflow event with content — Java: WorkflowThread#throwEvent', () => {
      provenByGolden('sleep-and-events')
    })

    test.todo('declare the thrown event payload type for auto-registration — Java: ThrowEventNodeOutput#registeredAs')
  })

  describe('child threads', () => {
    test('spawn a child thread with input variables — Java: WorkflowThread#spawnThread', () => {
      provenByGolden('child-threads')
    })

    test('spawn one thread per element of an array variable — Java: WorkflowThread#spawnThreadForEach', () => {
      provenByGolden('child-threads')
    })

    test('wait for all spawned threads — Java: WorkflowThread#waitForThreads', () => {
      provenByGolden('child-threads')
    })

    test('wait for any of the spawned threads — Java: WorkflowThread#waitForAnyOf', () => {
      const spec = compile(wf => {
        const t = wf.spawnThread(child => child.execute('work'), 'worker', {})
        wf.waitForAnyOf(spawnedThreadsOf(t))
      })
      const node = nodeOf(spec, '2-threads-WAIT_FOR_THREADS')
      if (node.node.oneofKind !== 'waitForThreads') throw new Error('expected waitForThreads node')
      expect(node.node.waitForThreads.strategy).toBe(WaitForThreadsStrategy.WAIT_FOR_ANY)
    })

    test('wait for the first of the spawned threads — Java: WorkflowThread#waitForFirstOf', () => {
      const spec = compile(wf => {
        const t = wf.spawnThread(child => child.execute('work'), 'worker', {})
        wf.waitForFirstOf(spawnedThreadsOf(t))
      })
      const node = nodeOf(spec, '2-threads-WAIT_FOR_THREADS')
      if (node.node.oneofKind !== 'waitForThreads') throw new Error('expected waitForThreads node')
      expect(node.node.waitForThreads.strategy).toBe(WaitForThreadsStrategy.WAIT_FOR_FIRST)
    })

    test('combine spawned threads into one handle — Java: SpawnedThreads.of', () => {
      provenByGolden('child-threads')
    })

    test('access the spawned thread-number variable — Java: SpawnedThread#getThreadNumberVariable', () => {
      compile(wf => {
        const t = wf.spawnThread(child => child.execute('work'), 'worker', {})
        expect(t.getThreadNumberVariable().name).toBe('1-worker-START_THREAD')
        wf.execute('noop')
      })
    })

    test('handle an error on a child thread — Java: WaitForThreadsNodeOutput#handleErrorOnChild', () => {
      const spec = compile(wf => {
        const t = wf.spawnThread(child => child.execute('work'), 'worker', {})
        wf.waitForThreads(spawnedThreadsOf(t)).handleErrorOnChild(null, handler => handler.execute('cleanup'))
      })
      const node = entrypointOf(spec).nodes['2-threads-WAIT_FOR_THREADS']
      if (node.node.oneofKind !== 'waitForThreads') throw new Error('expected waitForThreads node')
      const handler = node.node.waitForThreads.perThreadFailureHandlers[0]
      expect(handler.handlerSpecName).toBe('error-handler-2-threads-WAIT_FOR_THREADS-FAILURE_TYPE_ERROR')
      expect(handler.failureToCatch).toEqual({
        oneofKind: 'anyFailureOfType',
        anyFailureOfType: FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR,
      })
      expect(spec.threadSpecs[handler.handlerSpecName]).toBeDefined()
    })

    test('handle a named exception on a child thread — Java: WaitForThreadsNodeOutput#handleExceptionOnChild', () => {
      const spec = compile(wf => {
        const t = wf.spawnThread(child => child.execute('work'), 'worker', {})
        wf.waitForThreads(spawnedThreadsOf(t)).handleExceptionOnChild('oops', handler => handler.execute('cleanup'))
      })
      const node = entrypointOf(spec).nodes['2-threads-WAIT_FOR_THREADS']
      if (node.node.oneofKind !== 'waitForThreads') throw new Error('expected waitForThreads node')
      const handler = node.node.waitForThreads.perThreadFailureHandlers[0]
      expect(handler.handlerSpecName).toBe('exn-handler-2-threads-WAIT_FOR_THREADS-oops')
      expect(handler.failureToCatch).toEqual({ oneofKind: 'specificFailure', specificFailure: 'oops' })
    })

    test('handle any failure on a child thread — Java: WaitForThreadsNodeOutput#handleAnyFailureOnChild', () => {
      const spec = compile(wf => {
        const t = wf.spawnThread(child => child.execute('work'), 'worker', {})
        wf.waitForThreads(spawnedThreadsOf(t)).handleAnyFailureOnChild(handler => handler.execute('cleanup'))
      })
      const node = entrypointOf(spec).nodes['2-threads-WAIT_FOR_THREADS']
      if (node.node.oneofKind !== 'waitForThreads') throw new Error('expected waitForThreads node')
      const handler = node.node.waitForThreads.perThreadFailureHandlers[0]
      expect(handler.handlerSpecName).toBe('failure-handler-2-threads-WAIT_FOR_THREADS-ANY_FAILURE')
      expect(handler.failureToCatch).toEqual({ oneofKind: undefined })
    })

    test('set a per-thread retention policy — Java: WorkflowThread#withRetentionPolicy', () => {
      const policy = ThreadRetentionPolicy.create({
        threadGcPolicy: { oneofKind: 'secondsAfterThreadTermination', secondsAfterThreadTermination: '120' },
      })
      const spec = compile(wf => {
        wf.withRetentionPolicy(policy)
        wf.execute('a')
      })
      expect(entrypointOf(spec).retentionPolicy).toEqual(policy)
    })
  })

  describe('child workflows', () => {
    test('run a child workflow with inputs — Java: WorkflowThread#runWf', () => {
      provenByGolden('child-workflow')
    })

    test('wait for a spawned child workflow — Java: WorkflowThread#waitForChildWf', () => {
      provenByGolden('child-workflow')
    })
  })

  describe('interrupts', () => {
    test('register an interrupt handler for an external event — Java: WorkflowThread#registerInterruptHandler', () => {
      provenByGolden('interrupts')
    })
  })

  describe('failure handling', () => {
    test('handle any technical ERROR on a node — Java: WorkflowThread#handleError(NodeOutput, ThreadFunc)', () => {
      provenByGolden('failure-handling')
    })

    test('handle a specific LHErrorType on a node — Java: WorkflowThread#handleError(NodeOutput, LHErrorType, ThreadFunc)', () => {
      const spec = compile(wf => {
        const out = wf.execute('risky')
        wf.handleError(out, LHErrorType.TIMEOUT, handler => handler.execute('cleanup'))
      })
      const handler = nodeOf(spec, '1-risky-TASK').failureHandlers[0]
      expect(handler.handlerSpecName).toBe('exn-handler-1-risky-TASK-TIMEOUT')
      expect(handler.failureToCatch).toEqual({ oneofKind: 'specificFailure', specificFailure: 'TIMEOUT' })
    })

    test('handle any business EXCEPTION on a node — Java: WorkflowThread#handleException(NodeOutput, ThreadFunc)', () => {
      const spec = compile(wf => {
        const out = wf.execute('risky')
        wf.handleException(out, null, handler => handler.execute('cleanup'))
      })
      const handler = nodeOf(spec, '1-risky-TASK').failureHandlers[0]
      // Java produces the literal "null" suffix here — wire-compatible on purpose.
      expect(handler.handlerSpecName).toBe('exn-handler-1-risky-TASK-null')
      expect(handler.failureToCatch).toEqual({
        oneofKind: 'anyFailureOfType',
        anyFailureOfType: FailureHandlerDef_LHFailureType.FAILURE_TYPE_EXCEPTION,
      })
    })

    test('handle a named business EXCEPTION on a node — Java: WorkflowThread#handleException(NodeOutput, String, ThreadFunc)', () => {
      provenByGolden('failure-handling')
    })

    test('handle any failure (error or exception) on a node — Java: WorkflowThread#handleAnyFailure', () => {
      provenByGolden('failure-handling')
    })
  })

  describe('user tasks', () => {
    function userTaskActions(spec: PutWfSpecRequest, nodeName: string): UTActionTrigger[] {
      const node = nodeOf(spec, nodeName)
      if (node.node.oneofKind !== 'userTask') throw new Error('expected userTask node')
      return node.node.userTask.actions
    }

    test('assign a user task to a user and/or group — Java: WorkflowThread#assignUserTask', () => {
      provenByGolden('user-tasks')
    })

    test('attach notes to a user task (string, variable, or format string) — Java: UserTaskOutput#withNotes', () => {
      provenByGolden('user-tasks')
    })

    test('reassign a user task after a deadline — Java: WorkflowThread#reassignUserTask', () => {
      const spec = compile(wf => {
        const ut = wf.assignUserTask('approve', 'alice', null)
        wf.reassignUserTask(ut, 'bob', null, 300)
      })
      const action = userTaskActions(spec, '1-approve-USER_TASK')[0]
      expect(action.action.oneofKind).toBe('reassign')
      expect(action.hook).toBe(UTActionTrigger_UTHook.ON_TASK_ASSIGNED)
    })

    test('release an assigned task back to its group on deadline — Java: WorkflowThread#releaseToGroupOnDeadline', () => {
      provenByGolden('user-tasks')
    })

    test('schedule a reminder task after a delay — Java: WorkflowThread#scheduleReminderTask', () => {
      provenByGolden('user-tasks')
    })

    test('schedule a reminder task on assignment — Java: WorkflowThread#scheduleReminderTaskOnAssignment', () => {
      const spec = compile(wf => {
        const ut = wf.assignUserTask('approve', 'alice', null)
        wf.scheduleReminderTaskOnAssignment(ut, 60, 'remind')
      })
      const action = userTaskActions(spec, '1-approve-USER_TASK')[0]
      expect(action.action.oneofKind).toBe('task')
      expect(action.hook).toBe(UTActionTrigger_UTHook.ON_TASK_ASSIGNED)
    })

    test('cancel a user task run after a delay — Java: WorkflowThread#cancelUserTaskRunAfter', () => {
      provenByGolden('user-tasks')
    })

    test('cancel a user task run after assignment plus delay — Java: WorkflowThread#cancelUserTaskRunAfterAssignment', () => {
      const spec = compile(wf => {
        const ut = wf.assignUserTask('approve', 'alice', null)
        wf.cancelUserTaskRunAfterAssignment(ut, 3600)
      })
      const action = userTaskActions(spec, '1-approve-USER_TASK')[0]
      expect(action.action.oneofKind).toBe('cancel')
      expect(action.hook).toBe(UTActionTrigger_UTHook.ON_TASK_ASSIGNED)
    })
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
