import { VariableValue } from 'littlehorse-client/proto'
import { getUserTaskResultEntries } from './userTaskResults'

const str = (value: string): VariableValue => ({ value: { oneofKind: 'str', str: value } })

const structOutput = (fields: Record<string, VariableValue | undefined>): VariableValue => ({
  value: {
    oneofKind: 'struct',
    struct: {
      structDefId: { name: 'item-request-form', version: 0 },
      struct: {
        fields: Object.fromEntries(Object.entries(fields).map(([name, value]) => [name, { value, masked: false }])),
      },
    },
  },
})

describe('getUserTaskResultEntries', () => {
  it('returns nothing when there is no source', () => {
    expect(getUserTaskResultEntries(undefined)).toEqual([])
    expect(getUserTaskResultEntries({ results: {} })).toEqual([])
  })

  it('returns the legacy results map of a field-backed user task', () => {
    expect(getUserTaskResultEntries({ results: { requestedItem: str('laptop') } })).toEqual([
      ['requestedItem', str('laptop')],
    ])
  })

  it('returns the Struct fields of a struct-backed user task', () => {
    const output = structOutput({ requestedItem: str('laptop'), justification: str('new hire') })

    expect(getUserTaskResultEntries({ results: {}, output })).toEqual([
      ['requestedItem', str('laptop')],
      ['justification', str('new hire')],
    ])
  })

  it('keeps explicit nulls and drops fields without a value', () => {
    const explicitNull: VariableValue = { value: { oneofKind: undefined } }
    const output = structOutput({ notes: explicitNull, missing: undefined })

    expect(getUserTaskResultEntries({ results: {}, output })).toEqual([['notes', explicitNull]])
  })

  it('returns nothing for an empty Struct, which is how a cleared draft is stored', () => {
    expect(getUserTaskResultEntries({ results: {}, output: structOutput({}) })).toEqual([])
  })

  it('prefers the Struct output over the deprecated results map', () => {
    const output = structOutput({ requestedItem: str('laptop') })

    expect(getUserTaskResultEntries({ results: { stale: str('x') }, output })).toEqual([
      ['requestedItem', str('laptop')],
    ])
  })
})
