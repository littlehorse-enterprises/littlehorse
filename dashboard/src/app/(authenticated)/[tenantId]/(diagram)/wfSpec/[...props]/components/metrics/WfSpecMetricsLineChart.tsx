'use client'

import { Button } from '@/components/ui/button'
import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from '@/components/ui/chart'
import { type CSSProperties, FC, useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react'
import { CartesianGrid, Line, LineChart, XAxis, YAxis } from 'recharts'
import { LATENCY_CHART_CONFIG } from './metricsConstants'
import { CountDataPoint, LatencyDataPoint } from './metricsData'
import { ViewMode } from './wfSpecMetricsTypes'

/** Approximate horizontal inset of the plot vs container (Y axis + margins). */
const PLOT_LEFT_INSET_PX = 52
const PLOT_RIGHT_INSET_PX = 18
const MIN_DRAG_PX = 10

type PlotBounds = { left: number; top: number; width: number; height: number }

export type WfSpecMetricsLineChartProps = {
  viewMode: ViewMode
  chartConfig: ChartConfig
  countData: CountDataPoint[]
  latencyData: LatencyDataPoint[]
}

export const WfSpecMetricsLineChart: FC<WfSpecMetricsLineChartProps> = ({
  viewMode,
  chartConfig,
  countData,
  latencyData,
}) => {
  const fullData = viewMode === 'count' ? countData : latencyData
  const [zoom, setZoom] = useState<{ start: number; end: number } | null>(null)
  const [drag, setDrag] = useState<{ startX: number; currentX: number } | null>(null)
  const dragRef = useRef<{ startX: number; currentX: number } | null>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const [plotBounds, setPlotBounds] = useState<PlotBounds | null>(null)

  const displayData = useMemo(() => {
    if (!zoom || fullData.length === 0) return fullData
    return fullData.slice(zoom.start, zoom.end + 1)
  }, [fullData, zoom])

  const measurePlotBounds = useCallback(() => {
    const root = containerRef.current
    if (!root) return
    const grid = root.querySelector('.recharts-cartesian-grid')
    if (!grid) {
      setPlotBounds(null)
      return
    }
    const rootRect = root.getBoundingClientRect()
    const gridRect = grid.getBoundingClientRect()
    setPlotBounds({
      left: gridRect.left - rootRect.left,
      top: gridRect.top - rootRect.top,
      width: gridRect.width,
      height: gridRect.height,
    })
  }, [])

  useLayoutEffect(() => {
    const id = requestAnimationFrame(() => {
      requestAnimationFrame(measurePlotBounds)
    })
    return () => cancelAnimationFrame(id)
  }, [measurePlotBounds, displayData, viewMode, zoom])

  useEffect(() => {
    const root = containerRef.current
    if (!root) return
    const ro = new ResizeObserver(() => {
      requestAnimationFrame(measurePlotBounds)
    })
    ro.observe(root)
    return () => ro.disconnect()
  }, [measurePlotBounds])

  useEffect(() => {
    setZoom(null)
  }, [viewMode])

  const baseOffset = zoom?.start ?? 0

  /** Map x position (px from container left) to global series index, using measured plot width when available. */
  const xInContainerToGlobalIndex = useCallback(
    (xInContainer: number) => {
      const el = containerRef.current
      if (!el || fullData.length === 0) return 0
      const pb = plotBounds
      const innerLeft = pb?.left ?? PLOT_LEFT_INSET_PX
      const innerW =
        pb?.width ?? Math.max(1, el.getBoundingClientRect().width - PLOT_LEFT_INSET_PX - PLOT_RIGHT_INSET_PX)
      const clamped = Math.max(0, Math.min(innerW, xInContainer - innerLeft))
      const localIdx = displayData.length <= 1 ? 0 : Math.round((clamped / innerW) * (displayData.length - 1))
      return Math.min(fullData.length - 1, baseOffset + localIdx)
    },
    [baseOffset, displayData.length, fullData.length, plotBounds]
  )

  const onPointerDown = useCallback(
    (e: React.PointerEvent) => {
      if (e.button !== 0 || fullData.length < 2) return
      if ((e.target as HTMLElement).closest('button')) return
      const el = containerRef.current
      if (!el) return
      const rect = el.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top
      if (plotBounds && (y < plotBounds.top || y > plotBounds.top + plotBounds.height)) {
        return
      }
      const next = { startX: x, currentX: x }
      dragRef.current = next
      setDrag(next)
      el.setPointerCapture(e.pointerId)
    },
    [fullData.length, plotBounds]
  )

  const onPointerMove = useCallback((e: React.PointerEvent) => {
    if (!dragRef.current) return
    const el = containerRef.current
    if (!el) return
    const rect = el.getBoundingClientRect()
    const x = Math.max(0, Math.min(rect.width, e.clientX - rect.left))
    const next = { ...dragRef.current, currentX: x }
    dragRef.current = next
    setDrag(next)
  }, [])

  const endDrag = useCallback(
    (e: React.PointerEvent) => {
      const el = containerRef.current
      const d = dragRef.current
      dragRef.current = null
      if (d && el && fullData.length >= 2) {
        const leftPx = Math.min(d.startX, d.currentX)
        const rightPx = Math.max(d.startX, d.currentX)
        if (rightPx - leftPx >= MIN_DRAG_PX) {
          const iLeft = xInContainerToGlobalIndex(leftPx)
          const iRight = xInContainerToGlobalIndex(rightPx)
          const start = Math.max(0, Math.min(iLeft, iRight))
          const end = Math.min(fullData.length - 1, Math.max(iLeft, iRight))
          if (end > start) {
            setZoom({ start, end })
          }
        }
      }
      setDrag(null)
      if (el) {
        try {
          el.releasePointerCapture(e.pointerId)
        } catch {
          /* already released */
        }
      }
    },
    [fullData.length, xInContainerToGlobalIndex]
  )

  const resetZoom = useCallback(() => setZoom(null), [])

  const dragHighlightStyle = useMemo(() => {
    if (!drag || !plotBounds) return null
    const pb = plotBounds
    const rawLeft = Math.min(drag.startX, drag.currentX)
    const rawRight = Math.max(drag.startX, drag.currentX)
    const barLeft = Math.max(pb.left, rawLeft)
    const barRight = Math.min(pb.left + pb.width, rawRight)
    const w = barRight - barLeft
    if (w <= 0) return null
    return {
      left: barLeft,
      top: pb.top,
      width: w,
      height: pb.height,
    }
  }, [drag, plotBounds])

  return (
    <div
      ref={containerRef}
      className="relative w-full cursor-crosshair select-none"
      style={{ touchAction: 'none' }}
      onPointerDown={onPointerDown}
      onPointerMove={onPointerMove}
      onPointerUp={endDrag}
      onPointerCancel={endDrag}
      onDoubleClick={resetZoom}
      role="presentation"
      aria-label="Line chart. Drag horizontally to zoom a time range. Double-click to reset."
    >
      {zoom && (
        <div className="absolute right-2 top-0 z-20">
          <Button
            type="button"
            variant="secondary"
            size="sm"
            className="h-7 text-xs shadow-sm"
            onClick={resetZoom}
            onPointerDown={e => e.stopPropagation()}
          >
            Reset zoom
          </Button>
        </div>
      )}
      <ChartContainer config={chartConfig} className="h-[300px] w-full">
        {viewMode === 'count' ? (
          <LineChart data={displayData} margin={{ top: 5, right: 10, left: 10, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
            <XAxis dataKey="time" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
            <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} allowDecimals={false} />
            <ChartTooltip content={<ChartTooltipContent />} />
            <ChartLegend content={<ChartLegendContent />} />
            <Line type="monotone" dataKey="started" stroke="var(--color-started)" strokeWidth={2} dot={false} />
            <Line type="monotone" dataKey="completed" stroke="var(--color-completed)" strokeWidth={2} dot={false} />
            <Line type="monotone" dataKey="error" stroke="var(--color-error)" strokeWidth={2} dot={false} />
            <Line type="monotone" dataKey="exception" stroke="var(--color-exception)" strokeWidth={2} dot={false} />
          </LineChart>
        ) : (
          <LineChart data={displayData} margin={{ top: 5, right: 10, left: 10, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
            <XAxis dataKey="time" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
            <YAxis
              tick={{ fontSize: 11 }}
              tickLine={false}
              axisLine={false}
              tickFormatter={v => (v >= 1000 ? `${(v / 1000).toFixed(1)}s` : `${v}ms`)}
            />
            <ChartTooltip
              content={
                <ChartTooltipContent
                  formatter={(value, name, item) => {
                    const ms = Number(value)
                    const formatted = ms >= 1000 ? `${(ms / 1000).toFixed(2)}s` : `${ms}ms`
                    return (
                      <>
                        <div
                          className="h-2.5 w-2.5 shrink-0 rounded-[2px] border-[--color-border] bg-[--color-bg]"
                          style={{ '--color-bg': item.color, '--color-border': item.color } as CSSProperties}
                        />
                        <div className="flex flex-1 items-center justify-between leading-none">
                          <span className="text-muted-foreground">
                            {LATENCY_CHART_CONFIG[name as keyof typeof LATENCY_CHART_CONFIG]?.label ?? name}
                          </span>
                          <span className="ml-2 font-mono font-medium tabular-nums text-foreground">{formatted}</span>
                        </div>
                      </>
                    )
                  }}
                />
              }
            />
            <ChartLegend content={<ChartLegendContent />} />
            <Line
              type="monotone"
              dataKey="completedAvg"
              stroke="var(--color-completedAvg)"
              strokeWidth={2}
              dot={false}
            />
            <Line
              type="monotone"
              dataKey="completedMax"
              stroke="var(--color-completedMax)"
              strokeWidth={2}
              dot={false}
              strokeDasharray="5 5"
            />
            <Line type="monotone" dataKey="errorAvg" stroke="var(--color-errorAvg)" strokeWidth={2} dot={false} />
            <Line
              type="monotone"
              dataKey="errorMax"
              stroke="var(--color-errorMax)"
              strokeWidth={2}
              dot={false}
              strokeDasharray="5 5"
            />
          </LineChart>
        )}
      </ChartContainer>
      {dragHighlightStyle && (
        <div
          className="pointer-events-none absolute z-10 border border-primary/80 bg-primary/15"
          style={dragHighlightStyle}
        />
      )}
    </div>
  )
}
