import React from 'react'
import { render, screen } from '@testing-library/react'
import { Divider } from '../Divider'

describe('Divider', () => {
  it('renders the provided title', () => {
    render(<Divider title="Section Title" />)
    expect(screen.getByText('Section Title')).toBeInTheDocument()
  })

  it('renders two divider lines with the expected class', () => {
    const { container } = render(<Divider title="T" />)
    const lines = container.querySelectorAll('.h-px')
    expect(lines).toHaveLength(2)
    lines.forEach(line => {
      expect(line).toHaveClass('bg-gray-300')
    })
  })
})
