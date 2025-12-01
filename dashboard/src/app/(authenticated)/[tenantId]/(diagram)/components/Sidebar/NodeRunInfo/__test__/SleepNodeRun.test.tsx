import React from 'react'
import { render, screen } from '@testing-library/react'
import { SleepNodeRun } from '../SleepNodeRun'

describe('SleepNodeRun', () => {
  test('renders Node Type label and Sleep text', () => {
    const node = { maturationTime: '2021-01-01T00:00:00Z', matured: true } as any
    render(<SleepNodeRun node={node} />)

    expect(screen.getByText(/Node Type:/)).toBeTruthy()
    expect(screen.getByText('Sleep')).toBeTruthy()
  })

  // test('renders maturationTime label', () => {
  //     const node = { maturationTime: '2022-12-31T23:59:59Z', matured: false } as any
  //     render(<SleepNodeRun node={node} />)

  //     expect(screen.getByText(/maturationTime:/)).toBeTruthy()
  //     // the raw maturationTime string should be present (NodeVariable may format it)
  //     expect(screen.getByText(/2022-12-31/)).toBeTruthy()
  // })

  test('shows "Yes" when matured is true and "No" when matured is false', () => {
    const maturedNode = { maturationTime: '', matured: true } as any
    const notMaturedNode = { maturationTime: '', matured: false } as any

    const { rerender } = render(<SleepNodeRun node={maturedNode} />)
    expect(screen.getByText('Yes')).toBeTruthy()
    // ensure "No" is not present for matured true
    expect(screen.queryByText('No')).toBeNull()

    rerender(<SleepNodeRun node={notMaturedNode} />)
    expect(screen.getByText('No')).toBeTruthy()
    expect(screen.queryByText('Yes')).toBeNull()
  })
})
