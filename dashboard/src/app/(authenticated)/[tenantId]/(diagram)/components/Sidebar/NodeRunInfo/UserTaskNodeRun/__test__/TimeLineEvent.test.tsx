import React from 'react'
import { render } from '@testing-library/react'
import '@testing-library/jest-dom'
import { TimeLineEvent } from '../TimeLineEvent'

describe('TimeLineEvent', () => {
  test('renders default dot color (bg-blue-500)', () => {
    const { container } = render(
      <TimeLineEvent isLast={false}>
        <span>dot-test</span>
      </TimeLineEvent>
    )
    expect(container.querySelector('.bg-blue-500')).toBeInTheDocument()
  })

  test('renders custom dot color when dotColor prop is provided', () => {
    const { container } = render(
      <TimeLineEvent dotColor="bg-red-500" isLast={false}>
        <span>dot-test</span>
      </TimeLineEvent>
    )
    expect(container.querySelector('.bg-red-500')).toBeInTheDocument()
  })

  test('shows vertical line when isLast is false', () => {
    const { container } = render(
      <TimeLineEvent isLast={false}>
        <span>line-test</span>
      </TimeLineEvent>
    )
    expect(container.querySelector('.bg-gray-300')).toBeInTheDocument()
  })

  test('does not show vertical line when isLast is true', () => {
    const { container } = render(
      <TimeLineEvent isLast={true}>
        <span>no-line-test</span>
      </TimeLineEvent>
    )
    expect(container.querySelector('.bg-gray-300')).toBeNull()
  })
})
