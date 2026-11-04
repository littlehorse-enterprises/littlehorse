import { fireEvent, render } from '@testing-library/react'
import React, { createRef } from 'react'
import { useForm } from 'react-hook-form'
import { Textarea } from '../textarea'

describe('Textarea', () => {
  it('forwards its ref to the underlying textarea', () => {
    const ref = createRef<HTMLTextAreaElement>()
    render(<Textarea ref={ref} />)

    expect(ref.current).toBeInstanceOf(HTMLTextAreaElement)
  })

  it('registers with react-hook-form so typed values reach the submitted form values', async () => {
    const onValid = jest.fn()

    const Form = () => {
      const { register, handleSubmit } = useForm<{ payload: string }>()
      return (
        <form onSubmit={handleSubmit(onValid)}>
          <Textarea data-testid="payload" {...register('payload')} />
          <button type="submit">submit</button>
        </form>
      )
    }

    const { getByTestId, getByText } = render(<Form />)
    fireEvent.change(getByTestId('payload'), { target: { value: '{"apples":42}' } })
    fireEvent.click(getByText('submit'))

    await new Promise(resolve => setTimeout(resolve, 0))

    expect(onValid).toHaveBeenCalledTimes(1)
    expect(onValid.mock.calls[0][0]).toEqual({ payload: '{"apples":42}' })
  })
})
