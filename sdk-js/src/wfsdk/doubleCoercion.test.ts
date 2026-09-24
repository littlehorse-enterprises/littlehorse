import { describe, expect, it } from '@jest/globals'
import { Comparator } from '../proto/type_definition'
import { PutWfSpecRequest } from '../proto/service'
import { Workflow } from './Workflow'

/**
 * JS has one number type, so `100` cannot be spelled as a double the way
 * Java's `100.0` can. Wherever the counterpart's declared type says DOUBLE,
 * a whole-number literal must compile as DOUBLE; everywhere else the
 * whole-number-is-INT rule (conformance R8) is unchanged.
 */

function json(spec: PutWfSpecRequest): string {
  return PutWfSpecRequest.toJsonString(spec)
}

describe('whole-number literals compile as DOUBLE in declared-DOUBLE contexts', () => {
  it('condition: double variable vs whole-number literal', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      const total = t.declareDouble('total')
      t.doIf(t.condition(total, Comparator.GREATER_THAN_EQ, 100), b => {
        b.execute('approve')
      })
    })
    const out = json(wf.compileWorkflow())
    expect(out).toContain('"double":100')
    expect(out).not.toContain('"int":"100"')
  })

  it('condition: whole-number literal on the left of a double variable', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      const total = t.declareDouble('total')
      t.doIf(t.condition(100, Comparator.LESS_THAN, total), b => {
        b.execute('approve')
      })
    })
    const out = json(wf.compileWorkflow())
    expect(out).toContain('"double":100')
    expect(out).not.toContain('"int":"100"')
  })

  it('defaultValue: declareDouble accepts a whole-number default', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      t.declareDouble('threshold', { defaultValue: 100 })
      t.execute('noop')
    })
    const spec = wf.compileWorkflow()
    const threadVarDef = spec.threadSpecs['entrypoint'].variableDefs[0]
    expect(threadVarDef.varDef?.defaultValue?.value).toEqual({ oneofKind: 'double', double: 100 })
  })

  it('defaultValue: the fluent withDefault path coerces the same way', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      t.declareDouble('threshold').withDefault(100)
      t.execute('noop')
    })
    const threadVarDef = wf.compileWorkflow().threadSpecs['entrypoint'].variableDefs[0]
    expect(threadVarDef.varDef?.defaultValue?.value).toEqual({ oneofKind: 'double', double: 100 })
  })

  it('mutation: assigning a whole number to a double variable', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      const total = t.declareDouble('total')
      t.execute('noop')
      total.assign(100)
    })
    const out = json(wf.compileWorkflow())
    expect(out).toContain('"double":100')
    expect(out).not.toContain('"int":"100"')
  })

  it('arithmetic: whole-number operand against a double variable', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      const total = t.declareDouble('total')
      const doubled = t.declareDouble('doubled')
      t.execute('noop')
      doubled.assign(t.multiply(total, 2))
    })
    const out = json(wf.compileWorkflow())
    expect(out).toContain('"double":2')
    expect(out).not.toContain('"int":"2"')
  })
})

describe('the whole-number-is-INT rule is otherwise unchanged', () => {
  it('int variable vs whole-number literal stays INT', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      const count = t.declareInt('count')
      t.doIf(t.condition(count, Comparator.GREATER_THAN_EQ, 100), b => {
        b.execute('approve')
      })
    })
    const out = json(wf.compileWorkflow())
    expect(out).toContain('"int":"100"')
    expect(out).not.toContain('"double":100')
  })

  it('bigint stays INT even against a double variable', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      const total = t.declareDouble('total')
      t.doIf(t.condition(total, Comparator.GREATER_THAN_EQ, 100n), b => {
        b.execute('approve')
      })
    })
    const out = json(wf.compileWorkflow())
    expect(out).toContain('"int":"100"')
    expect(out).not.toContain('"double":100')
  })

  it('bare literals with no typed counterpart stay INT', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      t.declareDouble('total')
      t.sleepSeconds(30)
      t.execute('noop', 7)
    })
    const out = json(wf.compileWorkflow())
    expect(out).toContain('"int":"30"')
    expect(out).toContain('"int":"7"')
  })

  it('fractional literals still compile as DOUBLE without any context', () => {
    const wf = Workflow.newWorkflow('wf', t => {
      const count = t.declareInt('count')
      t.doIf(t.condition(count, Comparator.GREATER_THAN_EQ, 99.5), b => {
        b.execute('approve')
      })
    })
    expect(json(wf.compileWorkflow())).toContain('"double":99.5')
  })

  it('fractional default on declareInt still throws', () => {
    expect(() =>
      Workflow.newWorkflow('wf', t => {
        t.declareInt('count', { defaultValue: 5.5 })
        t.execute('noop')
      }).compileWorkflow()
    ).toThrow(/Default value type does not match/)
  })
})
