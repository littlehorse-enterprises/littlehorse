import { act, render, screen } from '@testing-library/react'
import { LHStatus, Timestamp } from 'littlehorse-client/proto'
import { WfRunDuration } from '../WfRunDuration'

const NOW = new Date('2026-01-01T12:00:00Z').getTime()
const ts = (msAgo: number) => Timestamp.fromDate(new Date(NOW - msAgo))

describe('WfRunDuration', () => {
  beforeEach(() => {
    jest.useFakeTimers()
    jest.setSystemTime(NOW)
  })
  afterEach(() => jest.useRealTimers())

  const advance = (ms: number) => act(() => jest.advanceTimersByTime(ms))

  it('ticks every second while RUNNING', () => {
    render(<WfRunDuration startTime={ts(59_000)} status={LHStatus.RUNNING} />)
    expect(screen.getByText('59s')).toBeInTheDocument()
    advance(1000)
    expect(screen.getByText('1m 0s')).toBeInTheDocument()
    advance(5000)
    expect(screen.getByText('1m 5s')).toBeInTheDocument()
  })

  it('shares a single interval across all RUNNING cells and clears it when none remain', () => {
    const { unmount } = render(
      <>
        <WfRunDuration startTime={ts(1000)} status={LHStatus.RUNNING} />
        <WfRunDuration startTime={ts(2000)} status={LHStatus.RUNNING} />
        <WfRunDuration startTime={ts(3000)} status={LHStatus.RUNNING} />
      </>
    )
    expect(jest.getTimerCount()).toBe(1)
    unmount()
    expect(jest.getTimerCount()).toBe(0)
  })

  it('shows a fixed Started → Ended duration for terminal WfRuns without ticking', () => {
    render(<WfRunDuration startTime={ts(90_000)} endTime={ts(30_000)} status={LHStatus.COMPLETED} />)
    expect(jest.getTimerCount()).toBe(0)
    advance(10_000)
    expect(screen.getByText('1m 0s')).toBeInTheDocument()
  })

  it('stops ticking and settles on the end time when the WfRun leaves RUNNING', () => {
    const { rerender } = render(<WfRunDuration startTime={ts(10_000)} status={LHStatus.RUNNING} />)
    advance(3000)
    expect(screen.getByText('13s')).toBeInTheDocument()

    rerender(<WfRunDuration startTime={ts(10_000)} endTime={ts(-2000)} status={LHStatus.COMPLETED} />)
    expect(screen.getByText('12s')).toBeInTheDocument()
    expect(jest.getTimerCount()).toBe(0)
    advance(5000)
    expect(screen.getByText('12s')).toBeInTheDocument()
  })

  it('freezes at the last value when HALTED without an end time', () => {
    const { rerender } = render(<WfRunDuration startTime={ts(10_000)} status={LHStatus.RUNNING} />)
    advance(4000)
    rerender(<WfRunDuration startTime={ts(10_000)} status={LHStatus.HALTED} />)
    expect(jest.getTimerCount()).toBe(0)
    advance(5000)
    expect(screen.getByText('14s')).toBeInTheDocument()
  })

  it('renders a placeholder without a start time', () => {
    render(<WfRunDuration status={LHStatus.RUNNING} />)
    expect(screen.getByText('—')).toBeInTheDocument()
    expect(jest.getTimerCount()).toBe(0)
  })
})
