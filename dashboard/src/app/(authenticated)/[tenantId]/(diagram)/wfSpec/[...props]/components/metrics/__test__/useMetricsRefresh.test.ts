import { act, renderHook } from '@testing-library/react'
import { REFRESH_SPIN_MS } from '../metricsConstants'
import { useMetricsRefresh } from '../useMetricsRefresh'

describe('useMetricsRefresh', () => {
  beforeEach(() => jest.useFakeTimers())
  afterEach(() => jest.useRealTimers())

  it('spins for REFRESH_SPIN_MS before revalidating, then bumps the redraw key', async () => {
    const revalidate = jest.fn().mockResolvedValue(undefined)
    const { result } = renderHook(() => useMetricsRefresh(revalidate))

    let refreshing = Promise.resolve()
    act(() => {
      refreshing = result.current.refresh()
    })
    expect(result.current.isManualRefreshing).toBe(true)

    await act(async () => {
      jest.advanceTimersByTime(REFRESH_SPIN_MS - 1)
    })
    expect(revalidate).not.toHaveBeenCalled()
    expect(result.current.redrawKey).toBe(0)

    await act(async () => {
      jest.advanceTimersByTime(1)
      await refreshing
    })
    expect(revalidate).toHaveBeenCalledTimes(1)
    expect(result.current.redrawKey).toBe(1)
    expect(result.current.isManualRefreshing).toBe(false)
  })

  it('stops spinning without redrawing when revalidation fails', async () => {
    const revalidate = jest.fn().mockRejectedValue(new Error('unavailable'))
    const { result } = renderHook(() => useMetricsRefresh(revalidate))

    let refreshing = Promise.resolve()
    act(() => {
      refreshing = result.current.refresh()
    })
    await act(async () => {
      jest.advanceTimersByTime(REFRESH_SPIN_MS)
      await expect(refreshing).rejects.toThrow('unavailable')
    })
    expect(result.current.redrawKey).toBe(0)
    expect(result.current.isManualRefreshing).toBe(false)
  })
})
