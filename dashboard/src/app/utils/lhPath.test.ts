import { LHPath } from 'littlehorse-client/proto'
import { lhPathToString } from './variables'

describe('lhPathToString', () => {
  it('should create string from index selector', () => {
    const lhPath: LHPath = {
      path: [
        {
          selectorType: {
            oneofKind: 'index',
            index: 0,
          },
        },
      ],
    }
    expect(lhPathToString(lhPath)).toEqual('$[0]')
  })

  it('should create string from key selector', () => {
    const lhPath: LHPath = {
      path: [
        {
          selectorType: {
            oneofKind: 'key',
            key: 'car',
          },
        },
      ],
    }
    expect(lhPathToString(lhPath)).toEqual('$.car')
  })

  it('should create string from dynamic selector', () => {
    const lhPath: LHPath = {
      path: [
        {
          selectorType: {
            oneofKind: 'dynamic',
            dynamic: {
              source: { oneofKind: 'variableName', variableName: 'key' },
              path: { oneofKind: undefined },
            },
          },
        },
      ],
    }
    expect(lhPathToString(lhPath)).toEqual('$[{key}]')
  })

  it('should create string from nested static and dynamic selectors', () => {
    const lhPath: LHPath = {
      path: [
        {
          selectorType: {
            oneofKind: 'key',
            key: 'anotherMap',
          },
        },
        {
          selectorType: {
            oneofKind: 'dynamic',
            dynamic: {
              source: {
                oneofKind: 'literalValue',
                literalValue: { value: { oneofKind: 'str', str: 'key' } },
              },
              path: { oneofKind: undefined },
            },
          },
        },
      ],
    }
    expect(lhPathToString(lhPath)).toEqual('$.anotherMap["key"]')
  })

  it('should create string from selector list', () => {
    const lhPath: LHPath = {
      path: [
        {
          selectorType: {
            oneofKind: 'key',
            key: 'car',
          },
        },
        {
          selectorType: {
            oneofKind: 'index',
            index: 10,
          },
        },
      ],
    }
    expect(lhPathToString(lhPath)).toEqual('$.car[10]')
  })
})
