import { fireEvent, render, screen } from '@testing-library/react'
import { VariableValue } from 'littlehorse-client/proto'
import { Results } from '../Results'

const setModal = jest.fn()
const setShowModal = jest.fn()

jest.mock('../../../../hooks/useModal', () => ({
  useModal: () => ({ setModal, setShowModal }),
}))

const assetTags: VariableValue = { value: { oneofKind: 'jsonObj', jsonObj: '{"monitor-1":"9007199254740993"}' } }
const item: VariableValue = { value: { oneofKind: 'str', str: 'Dell U2723QE monitor' } }

const renderOpen = () => {
  render(
    <Results
      variables={[
        ['item', item],
        ['assetTags', assetTags],
      ]}
      classTitle=""
    />
  )
  fireEvent.click(screen.getByText('Results'))
}

describe('Results', () => {
  beforeEach(() => jest.clearAllMocks())

  it('keeps each value on one truncated line', () => {
    renderOpen()
    const value = screen.getByText('{"monitor-1":"9007199254740993"}')
    expect(value).toHaveClass('truncate')
    expect(value).toHaveAttribute('title', '{"monitor-1":"9007199254740993"}')
  })

  it('shows the expand button only on hover', () => {
    renderOpen()
    expect(screen.queryByLabelText('Expand assetTags')).not.toBeInTheDocument()
    fireEvent.mouseEnter(screen.getByText('assetTags').closest('div')!)
    expect(screen.getByLabelText('Expand assetTags')).toBeInTheDocument()
  })

  it('opens the full value, formatted, in the output modal', () => {
    renderOpen()
    fireEvent.mouseEnter(screen.getByText('assetTags').closest('div')!)
    fireEvent.click(screen.getByLabelText('Expand assetTags'))
    expect(setModal).toHaveBeenCalledWith({
      type: 'output',
      data: { message: JSON.stringify({ 'monitor-1': '9007199254740993' }, null, 4), label: 'assetTags' },
    })
    expect(setShowModal).toHaveBeenCalledWith(true)
  })
})
