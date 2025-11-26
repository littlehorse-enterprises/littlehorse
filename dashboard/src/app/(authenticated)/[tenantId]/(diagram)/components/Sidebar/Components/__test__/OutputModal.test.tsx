import React from 'react'
import '@testing-library/jest-dom'
import { render, screen, fireEvent } from '@testing-library/react'
import { useModal } from '../../../../hooks/useModal'
import { OutputModal } from '../OutputModal'

jest.mock('../../../../hooks/useModal', () => ({
  useModal: jest.fn(),
}))

jest.mock('lucide-react', () => ({
  Expand: (props: React.JSX.IntrinsicAttributes & React.SVGProps<SVGSVGElement>) => <svg data-testid="expand" {...props} />,
}))

describe('OutputModal', () => {
  const setModal = jest.fn()
  const setShowModal = jest.fn()
  const mockedUseModal = useModal as jest.MockedFunction<typeof useModal>

  beforeEach(() => {
    jest.clearAllMocks()
    mockedUseModal.mockReturnValue({
        setModal,
        setShowModal,
        modal: null,
        showModal: false
    } )
  })

  it('renders label and message', () => {
    render(<OutputModal label="MyLabel" message="Hello world" />)
    expect(screen.getByText('MyLabel')).toBeInTheDocument()
    expect(screen.getByText('Hello world')).toBeInTheDocument()
    expect(screen.getByTestId('expand')).toBeInTheDocument()
  })

  it('opens modal with correct payload when expand clicked and message is present', () => {
    render(<OutputModal label="MyLabel" message="Hello world" />)
    fireEvent.click(screen.getByTestId('expand'))
    expect(setModal).toHaveBeenCalledTimes(1)
    expect(setModal).toHaveBeenCalledWith({
      type: 'output',
      data: { message: 'Hello world', label: 'Output' },
    })
    expect(setShowModal).toHaveBeenCalledWith(true)
  })

  it('does nothing when expand clicked and message is empty', () => {
    render(<OutputModal label="EmptyLabel" message="" />)
    fireEvent.click(screen.getByTestId('expand'))
    expect(setModal).not.toHaveBeenCalled()
    expect(setShowModal).not.toHaveBeenCalled()
  })
})
