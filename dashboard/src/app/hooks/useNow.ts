'use client'

import { useRef, useSyncExternalStore } from 'react'

const TICK_MS = 1000

// One interval shared by all subscribers.
const listeners = new Set<() => void>()
let tick = 0
let timer: ReturnType<typeof setInterval> | undefined

const subscribe = (listener: () => void) => {
  listeners.add(listener)
  if (!timer) {
    tick = Date.now()
    timer = setInterval(() => {
      tick = Date.now()
      listeners.forEach(l => l())
    }, TICK_MS)
  }
  return () => {
    listeners.delete(listener)
    if (listeners.size === 0) {
      clearInterval(timer)
      timer = undefined
    }
  }
}

const subscribeNoop = () => () => {}
const getTick = () => tick
const getZero = () => 0

/** Current time in ms, updated every second while `active`. Freezes at the last value when inactive. */
export const useNow = (active: boolean): number => {
  const now = useSyncExternalStore(active ? subscribe : subscribeNoop, active ? getTick : getZero, getZero)
  const last = useRef(0)
  last.current = Math.max(last.current || Date.now(), now)
  return last.current
}
