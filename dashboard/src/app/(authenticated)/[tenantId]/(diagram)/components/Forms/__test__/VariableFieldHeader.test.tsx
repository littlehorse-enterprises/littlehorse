import { fireEvent, render, screen } from '@testing-library/react'
import VariableFieldHeader from '../components/VariableFieldHeader'

describe('VariableFieldHeader', () => {
  it('only invokes its action when the action is clicked', () => {
    const onAction = jest.fn()

    render(
      <VariableFieldHeader
        name="profile"
        typeBadge={<span>InlineStruct</span>}
        action={<button onClick={onAction}>Enter Value</button>}
      />
    )

    fireEvent.click(screen.getByText('profile'))
    fireEvent.click(screen.getByText('InlineStruct'))
    expect(onAction).not.toHaveBeenCalled()

    fireEvent.click(screen.getByRole('button', { name: 'Enter Value' }))
    expect(onAction).toHaveBeenCalledTimes(1)
  })
})
