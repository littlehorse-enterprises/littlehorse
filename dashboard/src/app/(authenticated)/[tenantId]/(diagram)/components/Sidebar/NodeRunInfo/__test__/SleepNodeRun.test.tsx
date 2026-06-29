import React from 'react'
import { render, screen } from '@testing-library/react'
import { SleepNodeRun } from '../SleepNodeRun'
import { SleepNodeRun as SleepNodeRunProto, Timestamp } from 'littlehorse-client/proto'

describe('SleepNodeRun', () => {
  test('renders Node Type label , Sleep text and maturationTime given correct data', () => {
    const node: SleepNodeRunProto = {
      maturationTime: Timestamp.fromDate(new Date('2021-01-01T00:00:00Z')),
      matured: true,
    }
    render(<SleepNodeRun node={node} />)

    expect(screen.getByText(/Node Type:/)).toBeTruthy()
    expect(screen.getByText('Sleep')).toBeTruthy()
    expect(screen.getByText(/maturationTime:/)).toBeTruthy()
    expect(screen.getByText(/matured:/)).toBeTruthy()
  })

  test('shows "Yes" when matured is true and "No" when matured is false', () => {
    const maturedNode: SleepNodeRunProto = { maturationTime: Timestamp.create(), matured: true }
    const notMaturedNode: SleepNodeRunProto = { maturationTime: Timestamp.create(), matured: false }

    const { rerender } = render(<SleepNodeRun node={maturedNode} />)
    expect(screen.getByText('Yes')).toBeTruthy()
    expect(screen.queryByText('No')).toBeNull()

    rerender(<SleepNodeRun node={notMaturedNode} />)
    expect(screen.getByText('No')).toBeTruthy()
    expect(screen.queryByText('Yes')).toBeNull()
  })
})
