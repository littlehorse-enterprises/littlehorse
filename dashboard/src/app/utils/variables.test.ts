import { VariableAssignment, VariableDef, VariableMutationType, VariableType } from 'littlehorse-client/proto'
import { getTypedVariableValue, getVariable, getVariableDefType } from './variables'

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

  it('should return null from literalValue empty', async () => {
    const variable: VariableAssignment = {
      source: {
        $case: 'literalValue',
        value: {},
      },
    }
    expect(getVariable(variable)).toEqual('null')
  })
})

describe('getTypedContent', () => {
  it('should return str', async () => {
    const content = getTypedVariableValue('str', 'test')
    expect(content).toEqual({ value: { $case: 'str', value: 'test' } })
  })
})

describe('getTypedVariableValue', () => {
  it('should return wfRunId variable value', async () => {
    const variableValue = getTypedVariableValue('wfRunId', '12345')
    expect(variableValue).toStrictEqual({
      value: { $case: 'wfRunId', value: { id: '12345', parentWfRunId: undefined } },
    })
  })

  it('should return wfRunId variable value with parent', async () => {
    const variableValue = getTypedVariableValue('wfRunId', 'parent_child')
    expect(variableValue).toStrictEqual({
      value: { $case: 'wfRunId', value: { id: 'child', parentWfRunId: { id: 'parent', parentWfRunId: undefined } } },
    })
  })

  it('should return jsonObj variable value', async () => {
    const variableValue = getTypedVariableValue('jsonObj', '{"key": "value"}')
    expect(variableValue).toStrictEqual({ value: { $case: 'jsonObj', value: '{"key":"value"}' } })
  })

  it('should return parse error for invalid json', async () => {
    expect(() => getTypedVariableValue('jsonObj', 'this is not a json')).toThrow()
  })

  it('should return jsonArr variable value', async () => {
    const variableValue = getTypedVariableValue('jsonArr', '["item1", "item2"]')
    expect(variableValue).toStrictEqual({ value: { $case: 'jsonArr', value: '["item1","item2"]' } })
  })

  it('should return double variable value', async () => {
    const variableValue = getTypedVariableValue('double', '3.14')
    expect(variableValue).toStrictEqual({ value: { $case: 'double', value: 3.14 } })
  })

  it('should return bool variable value', async () => {
    const variableValue = getTypedVariableValue('bool', 'true')
    expect(variableValue).toStrictEqual({ value: { $case: 'bool', value: true } })
  })

  it('should return int variable value', async () => {
    const variableValue = getTypedVariableValue('int', '42')
    expect(variableValue).toStrictEqual({ value: { $case: 'int', value: 42 } })
  })

  it('should return bytes variable value', async () => {
    const variableValue = getTypedVariableValue('bytes', Buffer.from('Hello').toString())
    expect(variableValue).toStrictEqual({ value: { $case: 'bytes', value: Buffer.from('Hello') } })
  })

  it('should return str variable value', async () => {
    const variableValue = getTypedVariableValue('str', 'Hello World')
    expect(variableValue).toStrictEqual({ value: { $case: 'str', value: 'Hello World' } })
  })
})

describe('getVariableDefType', () => {
  it('should support pre 0.13.2 variable type', async () => {
    const variableDef: VariableDef = {
      name: 'testVariable',
      type: VariableType.STR,
    }

    const type = getVariableDefType(variableDef)
    expect(type).toEqual('str')
  })

  it('should return ', async () => {
    const variableDef: VariableDef = {
      name: 'testVariable',
      typeDef: {
        type: VariableType.STR,
        masked: false,
      },
    }

    const type = getVariableDefType(variableDef)
    expect(type).toEqual('str')
  })
})
