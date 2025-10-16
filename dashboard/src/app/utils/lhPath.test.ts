import { LHPath, LHPath_Selector } from 'littlehorse-client/proto'
import { lhPathToString } from './lhPath'

describe('lhPathToString', () => {
  it('should create string from index selector', () => {
    const lhPath: LHPath = {
      path: [
        {
          selectorType: {
            $case: 'index',
            value: 0,
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
            $case: 'key',
            value: 'car',
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
            $case: 'key',
            value: 'car',
          },
        },
        {
          selectorType: {
            $case: 'index',
            value: 10,
          },
        },
      ],
    }
    expect(lhPathToString(lhPath)).toEqual('$.car[10]')
  })
})
