import React from 'react'
import { render } from '@testing-library/react'
import '@testing-library/jest-dom'
import { TimelineItem } from '../TimeLineEvent'

describe('TimelineItem', () => {


  test('renders default dot color (bg-blue-500)', () => {
    const { container } = render(
      <TimelineItem isLast={false}>
        <span>dot-test</span>
      </TimelineItem>
    )
    expect(container.querySelector('.bg-blue-500')).toBeInTheDocument()
  })

  test('renders custom dot color when dotColor prop is provided', () => {
    const { container } = render(
      <TimelineItem dotColor="bg-red-500" isLast={false}>
        <span>dot-test</span>
      </TimelineItem>
    )
    expect(container.querySelector('.bg-red-500')).toBeInTheDocument()
  })

  test('shows vertical line when isLast is false', () => {
    const { container } = render(
      <TimelineItem isLast={false}>
        <span>line-test</span>
      </TimelineItem>
    )
    expect(container.querySelector('.bg-gray-300')).toBeInTheDocument()
  })

  test('does not show vertical line when isLast is true', () => {
    const { container } = render(
      <TimelineItem isLast={true}>
        <span>no-line-test</span>
      </TimelineItem>
    )
    expect(container.querySelector('.bg-gray-300')).toBeNull()
  })
})
