import { VariableAssignment } from 'littlehorse-client/proto'
import { getVariable } from './variables'

describe('getVariable', () => {
  it('should return from literalValue str', () => {
    const variable: VariableAssignment = {
      literalValue: {
        str: 'string',
      },
    }
    expect(getVariable(variable)).toEqual('string')
  })

  it('should return from literalValue bool', () => {
    const variable: VariableAssignment = {
      literalValue: {
        bool: true,
      },
    }
    expect(getVariable(variable)).toEqual(true)
  })

  it('should return variableName', () => {
    const variable: VariableAssignment = {
      variableName: 'variable',
    }
    expect(getVariable(variable)).toEqual('{variable}')
  })

  it('should return variableName with jsonPath', () => {
    const variable: VariableAssignment = {
      variableName: 'variable',
      jsonPath: '$.path',
    }
    expect(getVariable(variable)).toEqual('{variable.path}')
  })

  it('should return from formatString', async () => {
    const variable: VariableAssignment = {
      formatString: {
        format: {
          literalValue: {
            str: '{0} => {1}',
          },
        },
        args: [
          {
            literalValue: {
              str: 'first',
            },
          },
          {
            literalValue: {
              str: 'second',
            },
          },
        ],
      },
    }
    expect(getVariable(variable)).toEqual('first => second')
  })

  it('should return from stacked formatString', async () => {
    const variable: VariableAssignment = {
      formatString: {
        format: {
          formatString: {
            format: {
              literalValue: {
                str: '{0} => {1}',
              },
            },
            args: [
              {
                literalValue: {
                  str: '{1}',
                },
              },
              {
                literalValue: {
                  str: '{0}',
                },
              },
            ],
          },
        },
        args: [
          {
            literalValue: {
              str: 'arg1',
            },
          },
          {
            variableName: 'arg1',
          },
        ],
      },
    }
    expect(getVariable(variable)).toEqual('{arg1} => arg1')
  })
})
