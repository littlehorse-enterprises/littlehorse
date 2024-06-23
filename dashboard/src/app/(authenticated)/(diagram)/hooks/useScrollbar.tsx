import { useState, useRef, useMemo, useCallback } from 'react'

const SCROLL_STEP = 450

export const useScrollbar = () => {
  const [scroll, setScroll] = useState(0)

  const itemsRef = useRef<HTMLDivElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const maxScroll = useMemo(() => {
    if (!itemsRef.current || !containerRef.current) return
    return itemsRef.current.getBoundingClientRect().width - containerRef.current?.getBoundingClientRect().width
  }, [itemsRef, containerRef])

  const scrollRight = useCallback(() => {
    setScroll(prev => {
      if (maxScroll === undefined) return 0
      const newVal = prev - SCROLL_STEP
      return newVal <= -maxScroll ? -maxScroll : newVal
    })
  }, [maxScroll, setScroll])

  const scrollLeft = useCallback(() => {
    setScroll(prev => {
      const newVal = prev + SCROLL_STEP
      return newVal > 0 ? 0 : newVal
    })
  }, [setScroll])

  return { scroll, maxScroll, itemsRef, containerRef, scrollRight, scrollLeft }
}
