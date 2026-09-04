import { RunWfRequest, ThreadVarDef, VariableType, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/proto'
import { fireEvent, render, waitFor } from '@testing-library/react'
import React from 'react'
import { DOT_REPLACEMENT_PATTERN } from '../../Forms/context/StructFormContext'
import { ExecuteWorkflowRun } from '../ExecuteWorkflowRun'

const runWfSpec = jest.fn().mockResolvedValue({ id: { id: 'wf-run-id' } })
const getStructDef = jest.fn()

jest.mock('../../../wfSpec/[...props]/actions/runWfSpec', () => ({
  runWfSpec: (...args: unknown[]) => runWfSpec(...args),
}))
jest.mock('../../../hooks/useModal', () => ({
  useModal: () => ({ showModal: true, setShowModal: jest.fn() }),
}))
jest.mock('next/navigation', () => ({
  useParams: () => ({ tenantId: 'default' }),
  useRouter: () => ({ push: jest.fn() }),
}))
jest.mock('sonner', () => ({ toast: { success: jest.fn(), error: jest.fn() } }))
jest.mock('@/app/actions/getStructDef', () => ({
  getStructDef: (...args: unknown[]) => getStructDef(...args),
}))

const primitive = (
  name: string,
  primitiveType: VariableType,
  required: boolean,
  extra: Partial<ThreadVarDef> & { defaultValue?: unknown } = {}
): ThreadVarDef => {
  const { defaultValue, ...rest } = extra
  return {
    varDef: {
      name,
      typeDef: { definedType: { oneofKind: 'primitiveType', primitiveType }, masked: false },
      jsonIndexes: [],
      ...(defaultValue ? { defaultValue } : {}),
    },
    required,
    searchable: false,
    jsonIndexes: [],
    ...rest,
  } as unknown as ThreadVarDef
}

const mapVariable = (name: string): ThreadVarDef =>
  ({
    varDef: {
      name,
      typeDef: {
        definedType: {
          oneofKind: 'inlineMapDef',
          inlineMapDef: {
            keyType: { definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR } },
            valueType: { definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.INT } },
          },
        },
        masked: false,
      },
      jsonIndexes: [],
    },
    required: false,
    searchable: false,
    jsonIndexes: [],
  }) as unknown as ThreadVarDef

const structVariable = (name: string, required: boolean, defaultValue?: unknown): ThreadVarDef =>
  ({
    varDef: {
      name,
      typeDef: {
        definedType: { oneofKind: 'structDefId', structDefId: { name: 'person', version: 0 } },
        masked: false,
      },
      jsonIndexes: [],
      ...(defaultValue ? { defaultValue } : {}),
    },
    required,
    searchable: false,
    jsonIndexes: [],
  }) as unknown as ThreadVarDef

const personStructDef = {
  id: { name: 'person', version: 0 },
  structDef: {
    fields: {
      name: {
        fieldType: { definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR } },
      },
    },
  },
}

const personDefaultValue = {
  value: {
    oneofKind: 'struct',
    struct: {
      structDefId: { name: 'person', version: 0 },
      struct: {
        fields: {
          name: { value: { value: { oneofKind: 'str', str: 'Ada' } }, masked: false },
        },
      },
    },
  },
}

const buildWfSpec = (variableDefs: ThreadVarDef[]): WfSpec =>
  ({
    id: { name: 'test-wf', majorVersion: 0, revision: 0 },
    entrypointThreadName: 'entrypoint',
    threadSpecs: { entrypoint: { variableDefs } },
  }) as unknown as WfSpec

const setup = (variableDefs: ThreadVarDef[]) => {
  runWfSpec.mockClear()
  const { container, getByText } = render(<ExecuteWorkflowRun type="workflowRun" data={buildWfSpec(variableDefs)} />)
  const fill = (name: string, value: string) =>
    fireEvent.change(document.querySelector(`#${CSS.escape(name)}`)!, { target: { value } })
  const submit = () => fireEvent.click(getByText('Execute Workflow'))
  const sentVariables = async (): Promise<NonNullable<RunWfRequest['variables']>> => {
    await waitFor(() => expect(runWfSpec).toHaveBeenCalledTimes(1))
    return runWfSpec.mock.calls[0][0].variables
  }
  return { container, fill, submit, sentVariables }
}

describe('ExecuteWorkflowRun variables payload', () => {
  it('sends every variable the user filled in, whether required or optional', async () => {
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      primitive('optional-str', VariableType.STR, false),
      primitive('optional-int', VariableType.INT, false),
      primitive('optional-double', VariableType.DOUBLE, false),
    ])

    fill('required-str', 'req')
    fill('optional-str', 'opt')
    fill('optional-int', '42')
    fill('optional-double', '1.5')
    submit()

    const variables = await sentVariables()
    expect(variables['required-str'].value).toEqual({ oneofKind: 'str', str: 'req' })
    expect(variables['optional-str'].value).toEqual({ oneofKind: 'str', str: 'opt' })
    expect(variables['optional-int'].value).toEqual({ oneofKind: 'int', int: '42' })
    expect(variables['optional-double'].value).toEqual({ oneofKind: 'double', double: 1.5 })
  })

  it('omits optional variables the user never touched', async () => {
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      primitive('untouched-optional', VariableType.STR, false),
    ])

    fill('required-str', 'req')
    submit()

    const variables = await sentVariables()
    expect(variables['required-str']).toBeDefined()
    expect(variables['untouched-optional']).toBeUndefined()
  })

  const greetingWithDefault = () =>
    primitive('greeting', VariableType.STR, false, {
      defaultValue: { value: { oneofKind: 'str', str: 'hello' } },
    })

  it('omits a defaulted variable the user leaves alone', async () => {
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      greetingWithDefault(),
    ])

    fill('required-str', 'req')
    submit()

    expect((await sentVariables())['greeting']).toBeUndefined()
  })

  it('sends a defaulted variable once the user overrides it', async () => {
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      greetingWithDefault(),
    ])

    fill('required-str', 'req')
    fill('greeting', 'howdy')
    submit()

    expect((await sentVariables())['greeting'].value).toEqual({ oneofKind: 'str', str: 'howdy' })
  })

  it('blocks submit when a required variable is left blank', async () => {
    const { fill, submit } = setup([
      primitive('required-str', VariableType.STR, true),
      primitive('required-blank', VariableType.STR, true),
    ])

    fill('required-str', 'req')
    submit()

    await waitFor(() => expect(document.body.textContent).toContain('required-blank is required'))
    expect(runWfSpec).not.toHaveBeenCalled()
  })

  it('sends a required variable the user left at its WfSpec default', async () => {
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      primitive('required-defaulted', VariableType.STR, true, {
        defaultValue: { value: { oneofKind: 'str', str: 'from-wfspec' } },
      }),
    ])

    fill('required-str', 'req')
    submit()

    const variables = await sentVariables()
    expect(variables['required-defaulted'].value).toEqual({ oneofKind: 'str', str: 'from-wfspec' })
  })

  it('sends a dotted variable name with its declared type', async () => {
    const { fill, submit, sentVariables } = setup([primitive('customer.id', VariableType.STR, true)])

    fill(`customer${DOT_REPLACEMENT_PATTERN}id`, 'cust-123')
    submit()

    expect((await sentVariables())['customer.id'].value).toEqual({ oneofKind: 'str', str: 'cust-123' })
  })

  it('omits an untouched optional struct with defaults', async () => {
    getStructDef.mockResolvedValue(personStructDef)
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      structVariable('profile', false, personDefaultValue),
    ])

    fill('required-str', 'req')
    await waitFor(() => expect(document.querySelector(`#${CSS.escape('structValues.profile.name')}`)).not.toBeNull())
    submit()

    expect((await sentVariables())['profile']).toBeUndefined()
  })

  it('sends an optional struct after the user overrides a default', async () => {
    getStructDef.mockResolvedValue(personStructDef)
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      structVariable('profile', false, personDefaultValue),
    ])

    fill('required-str', 'req')
    const nameFieldId = 'structValues.profile.name'
    await waitFor(() => expect(document.querySelector(`#${CSS.escape(nameFieldId)}`)).not.toBeNull())
    fill(nameFieldId, 'Grace')
    submit()

    expect((await sentVariables())['profile'].value).toEqual({
      oneofKind: 'struct',
      struct: {
        structDefId: { name: 'person', version: 0 },
        struct: {
          fields: {
            name: { value: { value: { oneofKind: 'str', str: 'Grace' } }, masked: false },
          },
        },
      },
    })
  })

  it('sends JSON and Map values entered in a textarea', async () => {
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      primitive('payload', VariableType.JSON_OBJ, false),
      mapVariable('lookup'),
    ])

    fill('required-str', 'req')
    fill('payload', '{"apples":42}')
    fill('lookup', '{"one":1}')
    submit()

    const variables = await sentVariables()
    expect(variables['payload'].value).toEqual({ oneofKind: 'jsonObj', jsonObj: '{"apples":42}' })
    expect(variables['lookup'].value?.oneofKind).toBe('map')
  })

  it('submits a decimal entered for a DOUBLE variable', async () => {
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      primitive('ratio', VariableType.DOUBLE, false),
    ])

    fill('required-str', 'req')
    fill('ratio', '1.5')
    submit()

    expect((await sentVariables())['ratio'].value).toEqual({ oneofKind: 'double', double: 1.5 })
  })

  it('blocks submit when a DOUBLE variable is not a number', async () => {
    const { fill, submit } = setup([
      primitive('required-str', VariableType.STR, true),
      primitive('ratio', VariableType.DOUBLE, false),
    ])

    fill('required-str', 'req')
    fill('ratio', 'not-a-number')
    submit()

    await waitFor(() => expect(document.body.textContent).toContain('Must be a number'))
    expect(runWfSpec).not.toHaveBeenCalled()
  })

  it('never sends inherited variables', async () => {
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      primitive('from-parent', VariableType.STR, false, {
        accessLevel: WfRunVariableAccessLevel.INHERITED_VAR,
      }),
    ])

    fill('required-str', 'req')
    submit()

    const variables = await sentVariables()
    expect(variables['from-parent']).toBeUndefined()
  })
})
