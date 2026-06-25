import { LHPath } from 'littlehorse-client/proto'
import { lhPathToString } from './lhPath'

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
