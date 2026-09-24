import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { ThreadVarDef, VariableType, WfSpec } from 'littlehorse-client/proto'
import React, { createRef } from 'react'
import { FormValues, WfRunForm, WfRunFormSubmitMeta } from '../WfRunForm'

jest.mock('../components/StructDefGroup', () => ({ StructDefGroup: () => null }))

const primitiveVariable = (name: string, primitiveType: VariableType, required: boolean): ThreadVarDef =>
  ({
    varDef: {
      name,
      typeDef: { definedType: { oneofKind: 'primitiveType', primitiveType }, masked: false },
      jsonIndexes: [],
    },
    required,
    searchable: false,
    jsonIndexes: [],
  }) as unknown as ThreadVarDef

const arrayVariable = (name: string, required: boolean): ThreadVarDef =>
  ({
    varDef: {
      name,
      typeDef: {
        definedType: {
          oneofKind: 'inlineArrayDef',
          inlineArrayDef: {
            elementType: { definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR } },
          },
        },
        masked: false,
      },
      jsonIndexes: [],
    },
    required,
    searchable: false,
    jsonIndexes: [],
  }) as unknown as ThreadVarDef

const wfSpec = { id: { name: 'test-wf', majorVersion: 0, revision: 0 } } as unknown as WfSpec

const renderForm = (variables: ThreadVarDef[]) => {
  const onSubmit = jest.fn<void, [FormValues, WfRunFormSubmitMeta]>()
  const formRef = createRef<HTMLFormElement>()
  const { container } = render(
    <WfRunForm wfSpecVariables={variables} wfSpec={wfSpec} onSubmit={onSubmit} ref={formRef} />
  )
  const fill = (name: string, value: string) =>
    fireEvent.change(container.querySelector(`#${name}`)!, { target: { value } })
  return { onSubmit, formRef, fill }
}

/**
 * A variable flagged `required` in the WfSpec must never reach the server as an empty value.
 *
 * The `Required` badge and the validation rule are driven by two different props on FormField
 * (`protoRequired` / `formRequired`), so they can drift apart silently: in #2459 every primitive
 * was labelled `Required` yet submitted `""` without complaint, while TIMESTAMP and inline
 * containers — which build their rule from a prop that was actually passed — kept working.
 */
describe('required WfSpec variables', () => {
  const primitives: [string, VariableType][] = [
    ['STR', VariableType.STR],
    ['BOOL', VariableType.BOOL],
    ['INT', VariableType.INT],
    ['DOUBLE', VariableType.DOUBLE],
    ['BYTES', VariableType.BYTES],
    ['WF_RUN_ID', VariableType.WF_RUN_ID],
    ['TIMESTAMP', VariableType.TIMESTAMP],
    ['JSON_OBJ', VariableType.JSON_OBJ],
    ['JSON_ARR', VariableType.JSON_ARR],
  ]

  it.each(primitives)('blocks submission when a required %s variable is blank', async (_label, primitiveType) => {
    const { onSubmit, formRef } = renderForm([primitiveVariable('needs-value', primitiveType, true)])

    fireEvent.submit(formRef.current!)

    await waitFor(() => expect(screen.getByText('needs-value is required')).toBeInTheDocument())
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('blocks submission when a required inline container variable is blank', async () => {
    const { onSubmit, formRef } = renderForm([arrayVariable('needs-list', true)])

    fireEvent.submit(formRef.current!)

    await waitFor(() => expect(screen.getByText('needs-list is required')).toBeInTheDocument())
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits once every required variable has a value', async () => {
    const { onSubmit, formRef, fill } = renderForm([
      primitiveVariable('needs-value', VariableType.STR, true),
      primitiveVariable('optional-value', VariableType.STR, false),
    ])

    fill('needs-value', 'jake')
    fireEvent.submit(formRef.current!)

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(screen.queryByText('needs-value is required')).not.toBeInTheDocument()
    expect(onSubmit.mock.calls[0][0]['needs-value']).toBe('jake')
  })

  it('does not block submission on blank optional variables', async () => {
    const { onSubmit, formRef } = renderForm([
      primitiveVariable('optional-str', VariableType.STR, false),
      primitiveVariable('optional-int', VariableType.INT, false),
    ])

    fireEvent.submit(formRef.current!)

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(screen.queryByText('optional-str is required')).not.toBeInTheDocument()
  })
})
