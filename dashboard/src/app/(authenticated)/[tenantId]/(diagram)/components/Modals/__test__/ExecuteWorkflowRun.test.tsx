import { RunWfRequest, ThreadVarDef, VariableType, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/proto'
import { fireEvent, render, waitFor } from '@testing-library/react'
import React from 'react'
import { ExecuteWorkflowRun } from '../ExecuteWorkflowRun'

const runWfSpec = jest.fn().mockResolvedValue({ id: { id: 'wf-run-id' } })

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
jest.mock('../../Forms/components/StructDefGroup', () => ({ StructDefGroup: () => null }))

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

  it('still sends a required variable the user leaves blank', async () => {
    const { fill, submit, sentVariables } = setup([
      primitive('required-str', VariableType.STR, true),
      primitive('required-blank', VariableType.STR, true),
    ])

    fill('required-str', 'req')
    submit()

    const variables = await sentVariables()
    expect(variables['required-blank'].value).toEqual({ oneofKind: 'str', str: '' })
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
