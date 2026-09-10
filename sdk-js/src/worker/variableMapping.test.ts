import { describe, expect, it } from '@jest/globals'
import { VariableType } from '../proto/common_enums'
import { LHTypeAdapterRegistry } from '../common/typeAdapters'
import { toVariableValue } from './variableMapping'

class Money {
  constructor(readonly cents: number) {}
}

const registry = new LHTypeAdapterRegistry().add({
  name: 'money',
  matches: value => value instanceof Money,
  serialize: value => ({ value: { oneofKind: 'str', str: `money:${(value as Money).cents}` } }),
  deserialize: value => new Money(Number((value.value as { str: string }).str.split(':')[1])),
})

describe('toVariableValue type adapters', () => {
  it('uses a matching adapter to serialize', () => {
    const encoded = toVariableValue(new Money(125), undefined, registry)
    expect(encoded.value).toEqual({ oneofKind: 'str', str: 'money:125' })
  })

  it('an adapter wins over a declared type hint', () => {
    const encoded = toVariableValue(new Money(125), VariableType.DOUBLE, registry)
    expect(encoded.value).toEqual({ oneofKind: 'str', str: 'money:125' })
  })

  it('falls back to the built-ins when no adapter matches', () => {
    const encoded = toVariableValue(42, undefined, registry)
    expect(encoded.value).toEqual({ oneofKind: 'int', int: '42' })
  })
})
