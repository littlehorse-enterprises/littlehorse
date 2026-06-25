import {
  Timestamp,
  TypeDefinition,
  VariableAssignment,
  VariableDef,
  VariableMutationType,
  VariableType,
  VariableValue,
} from 'littlehorse-client/proto'
import {
  formatTypeDefinition,
  getPrimitiveFormDefaultValue,
  getTypedVariableValue,
  getVariable,
  getVariableCaseFromTypeDef,
  getVariableDefType,
  getVariableValue,
} from './variables'
import { normalizeUtcTimestampString } from './timestamp'

describe('getVariable', () => {
  it('should return from literalValue str', () => {
    const variable: VariableAssignment = {
      path: { oneofKind: undefined },
      source: {
        oneofKind: 'literalValue',
        literalValue: {
          value: {
            oneofKind: 'str',
            str: 'string',
          },
        },
      },
    }
    expect(getVariable(variable)).toEqual('string')
  })

  it('should return from literalValue bool', () => {
    const variable: VariableAssignment = {
      path: { oneofKind: undefined },
      source: {
        oneofKind: 'literalValue',
        literalValue: {
          value: {
            oneofKind: 'bool',
            bool: true,
          },
        },
      },
    }
    expect(getVariable(variable)).toEqual('true')
  })

  it('should return variableName', () => {
    const variable: VariableAssignment = {
      path: { oneofKind: undefined },
      source: {
        oneofKind: 'variableName',
        variableName: 'variable',
      },
    }
    expect(getVariable(variable)).toEqual('{variable}')
  })

  it('should return variableName with jsonPath', () => {
    const variable: VariableAssignment = {
      path: {
        oneofKind: 'jsonPath',
        jsonPath: '$.path',
      },
      source: {
        oneofKind: 'variableName',
        variableName: 'variable',
      },
    }
    expect(getVariable(variable)).toEqual('{variable.path}')
  })

  it('should return from formatString', async () => {
    const variable: VariableAssignment = {
      path: { oneofKind: undefined },
      source: {
        oneofKind: 'formatString',
        formatString: {
          format: {
            path: { oneofKind: undefined },
            source: {
              oneofKind: 'literalValue',
              literalValue: {
                value: {
                  oneofKind: 'str',
                  str: '{0} => {1}',
                },
              },
            },
          },
          args: [
            {
              path: { oneofKind: undefined },
              source: {
                oneofKind: 'literalValue',
                literalValue: {
                  value: {
                    oneofKind: 'str',
                    str: 'first',
                  },
                },
              },
            },
            {
              path: { oneofKind: undefined },
              source: {
                oneofKind: 'literalValue',
                literalValue: {
                  value: {
                    oneofKind: 'str',
                    str: 'second',
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
      path: { oneofKind: undefined },
      source: {
        oneofKind: 'formatString',
        formatString: {
          format: {
            path: { oneofKind: undefined },
            source: {
              oneofKind: 'formatString',
              formatString: {
                format: {
                  path: { oneofKind: undefined },
                  source: {
                    oneofKind: 'literalValue',
                    literalValue: {
                      value: {
                        oneofKind: 'str',
                        str: '{0} => {1}',
                      },
                    },
                  },
                },
                args: [
                  {
                    path: { oneofKind: undefined },
                    source: {
                      oneofKind: 'literalValue',
                      literalValue: {
                        value: {
                          oneofKind: 'str',
                          str: '{1}',
                        },
                      },
                    },
                  },
                  {
                    path: { oneofKind: undefined },
                    source: {
                      oneofKind: 'literalValue',
                      literalValue: {
                        value: {
                          oneofKind: 'str',
                          str: '{0}',
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
              path: { oneofKind: undefined },
              source: {
                oneofKind: 'literalValue',
                literalValue: {
                  value: {
                    oneofKind: 'str',
                    str: 'arg1',
                  },
                },
              },
            },
            {
              path: { oneofKind: undefined },
              source: {
                oneofKind: 'variableName',
                variableName: 'arg1',
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
      path: { oneofKind: undefined },
      source: {
        oneofKind: 'expression',
        expression: {
          lhs: {
            path: { oneofKind: undefined },
            source: {
              oneofKind: 'literalValue',
              literalValue: {
                value: {
                  oneofKind: 'int',
                  int: '42',
                },
              },
            },
          },
          rhs: {
            path: { oneofKind: undefined },
            source: {
              oneofKind: 'literalValue',
              literalValue: {
                value: {
                  oneofKind: 'int',
                  int: '58',
                },
              },
            },
          },
          operation: { oneofKind: 'mutationType', mutationType: VariableMutationType.MULTIPLY },
        },
      },
    }

    expect(getVariable(variable)).toEqual('42 * 58')
  })

  it('should extend variable expressions', async () => {
    const variable: VariableAssignment = {
      path: { oneofKind: undefined },
      source: {
        oneofKind: 'expression',
        expression: {
          lhs: {
            path: { oneofKind: undefined },
            source: {
              oneofKind: 'literalValue',
              literalValue: {
                value: {
                  oneofKind: 'jsonArr',
                  jsonArr: '[1, 2, 3]',
                },
              },
            },
          },
          rhs: {
            path: { oneofKind: undefined },
            source: {
              oneofKind: 'literalValue',
              literalValue: {
                value: {
                  oneofKind: 'int',
                  int: '58',
                },
              },
            },
          },
          operation: { oneofKind: 'mutationType', mutationType: VariableMutationType.EXTEND },
        },
      },
    }

    expect(getVariable(variable)).toEqual('[1, 2, 3].extends(58)')
  })

  it('should extend strings expressions', async () => {
    const variable: VariableAssignment = {
      path: { oneofKind: undefined },
      source: {
        oneofKind: 'expression',
        expression: {
          lhs: {
            path: { oneofKind: undefined },
            source: {
              oneofKind: 'literalValue',
              literalValue: {
                value: {
                  oneofKind: 'str',
                  str: 'hello',
                },
              },
            },
          },
          rhs: {
            path: { oneofKind: undefined },
            source: {
              oneofKind: 'literalValue',
              literalValue: {
                value: {
                  oneofKind: 'str',
                  str: '-world',
                },
              },
            },
          },
          operation: { oneofKind: 'mutationType', mutationType: VariableMutationType.EXTEND },
        },
      },
    }

    expect(getVariable(variable)).toEqual('hello.extends(-world)')
  })

  it('should return null from literalValue empty', async () => {
    const variable: VariableAssignment = {
      path: { oneofKind: undefined },
      source: {
        oneofKind: 'literalValue',
        literalValue: {
          value: { oneofKind: undefined },
        },
      },
    }
    expect(getVariable(variable)).toEqual('NULL')
  })
})

describe('getTypedContent', () => {
  it('should return str', async () => {
    const content = getTypedVariableValue('str', 'test')
    expect(content).toEqual({ value: { oneofKind: 'str', str: 'test' } })
  })
})

describe('getTypedVariableValue', () => {
  it('should return wfRunId variable value', async () => {
    const variableValue = getTypedVariableValue('wfRunId', '12345')
    expect(variableValue).toStrictEqual({
      value: { oneofKind: 'wfRunId', wfRunId: { id: '12345' } },
    })
  })

  it('should return wfRunId variable value with parent', async () => {
    const variableValue = getTypedVariableValue('wfRunId', 'parent_child')
    expect(variableValue).toStrictEqual({
      value: {
        oneofKind: 'wfRunId',
        wfRunId: { id: 'child', parentWfRunId: { id: 'parent' } },
      },
    })
  })

  it('should return jsonObj variable value', async () => {
    const variableValue = getTypedVariableValue('jsonObj', '{"key": "value"}')
    expect(variableValue).toStrictEqual({ value: { oneofKind: 'jsonObj', jsonObj: '{"key":"value"}' } })
  })

  it('should return parse error for invalid json', async () => {
    expect(() => getTypedVariableValue('jsonObj', 'this is not a json')).toThrow()
  })

  it('should return jsonArr variable value', async () => {
    const variableValue = getTypedVariableValue('jsonArr', '["item1", "item2"]')
    expect(variableValue).toStrictEqual({ value: { oneofKind: 'jsonArr', jsonArr: '["item1","item2"]' } })
  })

  it('should return double variable value', async () => {
    const variableValue = getTypedVariableValue('double', '3.14')
    expect(variableValue).toStrictEqual({ value: { oneofKind: 'double', double: 3.14 } })
  })

  it('should return bool variable value', async () => {
    const variableValue = getTypedVariableValue('bool', 'true')
    expect(variableValue).toStrictEqual({ value: { oneofKind: 'bool', bool: true } })
  })

  it('should return int variable value', async () => {
    const variableValue = getTypedVariableValue('int', '42')
    expect(variableValue).toStrictEqual({ value: { oneofKind: 'int', int: '42' } })
  })

  it('should return bytes variable value', async () => {
    const variableValue = getTypedVariableValue('bytes', Buffer.from('Hello').toString())
    expect(variableValue).toStrictEqual({ value: { oneofKind: 'bytes', bytes: new Uint8Array(Buffer.from('Hello')) } })
  })

  it('should return str variable value', async () => {
    const variableValue = getTypedVariableValue('str', 'Hello World')
    expect(variableValue).toStrictEqual({ value: { oneofKind: 'str', str: 'Hello World' } })
  })

  it('should return utcTimestamp as RFC3339 string', async () => {
    const variableValue = getTypedVariableValue('utcTimestamp', '2024-06-15T14:30:45.123456789Z')
    expect(variableValue).toStrictEqual({
      value: {
        oneofKind: 'utcTimestamp',
        utcTimestamp: Timestamp.fromDate(new Date(normalizeUtcTimestampString('2024-06-15T14:30:45.123456789Z'))),
      },
    })
  })
})

describe('getPrimitiveFormDefaultValue', () => {
  it('returns undefined when no default is provided', () => {
    expect(getPrimitiveFormDefaultValue(undefined)).toBeUndefined()
    expect(getPrimitiveFormDefaultValue({} as VariableValue)).toBeUndefined()
  })

  it('returns the str value for STR defaults', () => {
    expect(getPrimitiveFormDefaultValue({ value: { oneofKind: 'str', str: 'hello' } })).toEqual('hello')
  })

  it('preserves empty string defaults', () => {
    expect(getPrimitiveFormDefaultValue({ value: { oneofKind: 'str', str: '' } })).toEqual('')
  })

  it('returns numeric values for INT/DOUBLE defaults', () => {
    expect(getPrimitiveFormDefaultValue({ value: { oneofKind: 'int', int: '42' } })).toEqual('42')
    expect(getPrimitiveFormDefaultValue({ value: { oneofKind: 'double', double: 1.5 } })).toEqual(1.5)
  })

  it('serializes BOOL defaults as form-friendly strings', () => {
    expect(getPrimitiveFormDefaultValue({ value: { oneofKind: 'bool', bool: true } })).toEqual('true')
    expect(getPrimitiveFormDefaultValue({ value: { oneofKind: 'bool', bool: false } })).toEqual('false')
  })

  it('returns the JSON string for JSON_OBJ/JSON_ARR defaults', () => {
    expect(getPrimitiveFormDefaultValue({ value: { oneofKind: 'jsonObj', jsonObj: '{"a":1}' } })).toEqual('{"a":1}')
    expect(getPrimitiveFormDefaultValue({ value: { oneofKind: 'jsonArr', jsonArr: '[1,2]' } })).toEqual('[1,2]')
  })

  it('returns undefined for non-primitive cases', () => {
    expect(
      getPrimitiveFormDefaultValue({ value: { oneofKind: 'bytes', bytes: new Uint8Array(Buffer.from('')) } })
    ).toBeUndefined()
    expect(getPrimitiveFormDefaultValue({ value: { oneofKind: 'struct', struct: {} as any } })).toBeUndefined()
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
        definedType: {
          oneofKind: 'primitiveType',
          primitiveType: VariableType.STR,
        },
        masked: false,
      },
    }

    const type = getVariableDefType(variableDef)
    expect(type).toEqual('str')
  })
})

describe('getVariableCaseFromTypeDef', () => {
  it('should return primitive type case', () => {
    const typeDef: TypeDefinition = {
      definedType: {
        oneofKind: 'primitiveType',
        primitiveType: VariableType.STR,
      },
      masked: false,
    }

    expect(getVariableCaseFromTypeDef(typeDef)).toEqual('str')
  })

  it('should return struct for structDefId', () => {
    const typeDef: TypeDefinition = {
      definedType: {
        oneofKind: 'structDefId',
        structDefId: {
          // minimal stub; fields are not used by the function
        } as any,
      },
      masked: false,
    }

    expect(getVariableCaseFromTypeDef(typeDef)).toEqual('struct')
  })

  it('should throw on unknown type', () => {
    const typeDef = {} as TypeDefinition
    expect(() => getVariableCaseFromTypeDef(typeDef)).toThrow('Unknown variable type.')
  })
})

describe('formatTypeDefinition', () => {
  it('should format primitive type', () => {
    const typeDef: TypeDefinition = {
      definedType: {
        oneofKind: 'primitiveType',
        primitiveType: VariableType.INT,
      },
      masked: false,
    }

    expect(formatTypeDefinition(typeDef)).toEqual('Integer')
  })

  it('should format nested arrays recursively', () => {
    const typeDef: TypeDefinition = {
      definedType: {
        oneofKind: 'inlineArrayDef',
        inlineArrayDef: {
          arrayType: {
            definedType: {
              oneofKind: 'inlineArrayDef',
              inlineArrayDef: {
                arrayType: {
                  definedType: {
                    oneofKind: 'primitiveType',
                    primitiveType: VariableType.INT,
                  },
                  masked: false,
                },
              },
            },
            masked: false,
          },
        },
      },
      masked: false,
    }

    expect(formatTypeDefinition(typeDef)).toEqual('Array<Array<Integer>>')
  })

  it('should format struct type', () => {
    const typeDef: TypeDefinition = {
      definedType: {
        oneofKind: 'structDefId',
        structDefId: {
          name: 'customer',
          version: 1,
        },
      },
      masked: false,
    }

    expect(formatTypeDefinition(typeDef)).toEqual('Struct<customer,1>')
  })
})

describe('getVariableValue', () => {
  it('should return NULL for empty value', () => {
    expect(getVariableValue({ value: {} } as VariableValue)).toEqual('NULL')
  })

  it('should render native array ints as JSON numbers', () => {
    const variableValue = VariableValue.create({
      value: {
        oneofKind: 'array',
        array: {
          items: [
            { value: { oneofKind: 'int', int: '1' } },
            { value: { oneofKind: 'int', int: '2' } },
            { value: { oneofKind: 'int', int: '3' } },
          ],
        },
      },
    })

    expect(getVariableValue(variableValue)).toEqual('[1,2,3]')
  })
})
