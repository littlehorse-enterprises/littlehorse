import { ThreadVarDef, VariableType, WfSpec } from 'littlehorse-client/proto'
import { fireEvent, render, waitFor } from '@testing-library/react'
import React, { createRef } from 'react'
import { FormValues, WfRunForm, WfRunFormSubmitMeta } from '../WfRunForm'

jest.mock('../components/StructDefGroup', () => ({ StructDefGroup: () => null }))

const strVariable = (name: string, required: boolean): ThreadVarDef =>
  ({
    varDef: {
      name,
      typeDef: { definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR }, masked: false },
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

describe('WfRunForm', () => {
  it('reports user-entered optional variables as dirty', async () => {
    const { onSubmit, formRef, fill } = renderForm([
      strVariable('required-control', true),
      strVariable('lh-cluster-name', false),
    ])

    fill('required-control', 'CONTROL-VALUE')
    fill('lh-cluster-name', 'my-cluster-lh')
    fireEvent.submit(formRef.current!)

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))

    const [values, meta] = onSubmit.mock.calls[0]
    expect(values['lh-cluster-name']).toBe('my-cluster-lh')
    expect(meta.dirtyFields['lh-cluster-name']).toBe(true)
    expect(meta.dirtyFields['required-control']).toBe(true)
  })

  it('leaves untouched optional variables out of dirtyFields so the server default applies', async () => {
    const { onSubmit, formRef, fill } = renderForm([
      strVariable('required-control', true),
      strVariable('lh-cluster-name', false),
    ])

    fill('required-control', 'CONTROL-VALUE')
    fireEvent.submit(formRef.current!)

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))

    const [, meta] = onSubmit.mock.calls[0]
    expect(meta.dirtyFields['required-control']).toBe(true)
    expect(meta.dirtyFields['lh-cluster-name']).toBeUndefined()
  })
})
