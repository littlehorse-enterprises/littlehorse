import { formatDurationMs } from './dateTime'

describe('formatDurationMs', () => {
  it('formats sub-second durations in milliseconds', () => {
    expect(formatDurationMs(0)).toBe('0ms')
    expect(formatDurationMs(999)).toBe('999ms')
  })

  it('formats seconds', () => {
    expect(formatDurationMs(1000)).toBe('1.0s')
    expect(formatDurationMs(1500)).toBe('1.5s')
  })

  it('rolls up to minutes rather than reporting 60 seconds', () => {
    expect(formatDurationMs(59_499)).toBe('59.5s')
    expect(formatDurationMs(59_999)).toBe('1m')
    expect(formatDurationMs(60_000)).toBe('1m')
  })

  it('formats minutes and hours', () => {
    expect(formatDurationMs(90_000)).toBe('1m 30s')
    expect(formatDurationMs(120_000)).toBe('2m')
    expect(formatDurationMs(3_600_000)).toBe('1h')
    expect(formatDurationMs(5_400_000)).toBe('1h 30m')
  })
})
