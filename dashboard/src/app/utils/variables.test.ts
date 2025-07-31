import { VariableAssignment, VariableMutationType } from 'littlehorse-client/proto'
import { getTypedVariableValue, getVariable } from './variables'

describe('getVariable', () => {
  it('should return from literalValue str', () => {
    const variable: VariableAssignment = {
      source: {
        $case: 'literalValue',
        value: {
          value: {
            $case: 'str',
            value: 'string',
          },
        },
      },
    }
    expect(getVariable(variable)).toEqual('string')
  })

  it('should return from literalValue bool', () => {
    const variable: VariableAssignment = {
      source: {
        $case: 'literalValue',
        value: {
          value: {
            $case: 'bool',
            value: true,
          },
        },
      },
    }
    expect(getVariable(variable)).toEqual('true')
  })

  it('should return variableName', () => {
    const variable: VariableAssignment = {
      source: {
        $case: 'variableName',
        value: 'variable',
      },
    }
    expect(getVariable(variable)).toEqual('{variable}')
  })

  it('should return variableName with jsonPath', () => {
    const variable: VariableAssignment = {
      jsonPath: '$.path',
      source: {
        $case: 'variableName',
        value: 'variable',
      },
    }
    expect(getVariable(variable)).toEqual('{variable.path}')
  })

  it('should return from formatString', async () => {
    const variable: VariableAssignment = {
      source: {
        $case: 'formatString',
        value: {
          format: {
            source: {
              $case: 'literalValue',
              value: {
                value: {
                  $case: 'str',
                  value: '{0} => {1}',
                },
              },
            },
          },
          args: [
            {
              source: {
                $case: 'literalValue',
                value: {
                  value: {
                    $case: 'str',
                    value: 'first',
                  },
                },
              },
            },
            {
              source: {
                $case: 'literalValue',
                value: {
                  value: {
                    $case: 'str',
                    value: 'second',
                  },
                },
              },
            },
          ],
        },
      },
    }
    expect(getVariable(variable)).toEqual('first => second')
  })

  it('should return from stacked formatString', async () => {
    const variable: VariableAssignment = {
      source: {
        $case: 'formatString',
        value: {
          format: {
            source: {
              $case: 'formatString',
              value: {
                format: {
                  source: {
                    $case: 'literalValue',
                    value: {
                      value: {
                        $case: 'str',
                        value: '{0} => {1}',
                      },
                    },
                  },
                },
                args: [
                  {
                    source: {
                      $case: 'literalValue',
                      value: {
                        value: {
                          $case: 'str',
                          value: '{1}',
                        },
                      },
                    },
                  },
                  {
                    source: {
                      $case: 'literalValue',
                      value: {
                        value: {
                          $case: 'str',
                          value: '{0}',
                        },
                      },
                    },
                  },
                ],
              },
            },
          },
          args: [
            {
              source: {
                $case: 'literalValue',
                value: {
                  value: {
                    $case: 'str',
                    value: 'arg1',
                  },
                },
              },
            },
            {
              source: {
                $case: 'variableName',
                value: 'arg1',
              },
            },
          ],
        },
      },
    }
    expect(getVariable(variable)).toEqual('{arg1} => arg1')
  })

  it('should handle variable expressions', async () => {
    const variable: VariableAssignment = {
      source: {
        $case: 'expression',
        value: {
          lhs: {
            source: {
              $case: 'literalValue',
              value: {
                value: {
                  $case: 'int',
                  value: 42,
                },
              },
            },
          },
          rhs: {
            source: {
              $case: 'literalValue',
              value: {
                value: {
                  $case: 'int',
                  value: 58,
                },
              },
            },
          },
          operation: VariableMutationType.MULTIPLY,
        },
      },
    }

    expect(getVariable(variable)).toEqual('42 * 58')
  })

  it('should extend variable expressions', async () => {
    const variable: VariableAssignment = {
      source: {
        $case: 'expression',
        value: {
          lhs: {
            source: {
              $case: 'literalValue',
              value: {
                value: {
                  $case: 'jsonArr',
                  value: '[1, 2, 3]',
                },
              },
            },
          },
          rhs: {
            source: {
              $case: 'literalValue',
              value: {
                value: {
                  $case: 'int',
                  value: 58,
                },
              },
            },
          },
          operation: VariableMutationType.EXTEND,
        },
      },
    }

    expect(getVariable(variable)).toEqual('[1, 2, 3].extends(58)')
  })

  it('should extend strings expressions', async () => {
    const variable: VariableAssignment = {
      source: {
        $case: 'expression',
        value: {
          lhs: {
            source: {
              $case: 'literalValue',
              value: {
                value: {
                  $case: 'str',
                  value: 'hello',
                },
              },
            },
          },
          rhs: {
            source: {
              $case: 'literalValue',
              value: {
                value: {
                  $case: 'str',
                  value: '-world',
                },
              },
            },
          },
          operation: VariableMutationType.EXTEND,
        },
      },
    }

    expect(getVariable(variable)).toEqual('hello.extends(-world)')
  })
})

describe('getTypedContent', () => {
  it('should return str', async () => {
    const content = getTypedVariableValue('str', 'test')
    expect(content).toEqual({ value: { $case: 'str', value: 'test' } })
  })
})
