'use client'

import { buildRfc3339Utc, parseRfc3339Utc, unixMillisToRfc3339 } from '@/app/utils/timestamp'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { cn } from '@/components/utils'
import { format } from 'date-fns'
import { ChevronDownIcon } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'

export type TimestampPickerProps = {
  id?: string
  value: string
  onChange: (next: string) => void
  disabled?: boolean
  className?: string
}

function utcParts(d: Date) {
  return {
    y: d.getUTCFullYear(),
    mo: d.getUTCMonth(),
    day: d.getUTCDate(),
    h: d.getUTCHours(),
    mi: d.getUTCMinutes(),
    s: d.getUTCSeconds(),
    ms: d.getUTCMilliseconds(),
  }
}

function utcDateFromParts(y: number, mo: number, day: number, h: number, mi: number, s: number, ms: number): Date {
  return new Date(Date.UTC(y, mo, day, h, mi, s, ms))
}

const msFromNanos = (nanoseconds: number) => Math.floor(nanoseconds / 1_000_000)

function utcTimeWithNanos(d: Date, nanos: number): string {
  const p = utcParts(d)
  const hms = `${String(p.h).padStart(2, '0')}:${String(p.mi).padStart(2, '0')}:${String(p.s).padStart(2, '0')}`
  if (nanos === 0) return hms
  const subSecondNanos = nanos % 1_000_000_000
  const frac = subSecondNanos.toString().padStart(9, '0').replace(/0+$/, '')
  return frac ? `${hms}.${frac}` : hms
}

const TIME_RE = /^(\d{1,2}):(\d{1,2}):(\d{1,2})(?:\.(\d{1,9}))?$/

function parseTimeWithNanos(raw: string): { h: number; mi: number; s: number; nanos: number } | null {
  const m = raw.trim().match(TIME_RE)
  if (!m) return null
  const [, hStr, miStr, sStr, fracStr] = m
  const h = parseInt(hStr, 10)
  const mi = parseInt(miStr, 10)
  const s = parseInt(sStr, 10)
  if (h > 23 || mi > 59 || s > 59) return null
  let nanos = 0
  if (fracStr) {
    const padded = fracStr.padEnd(9, '0').slice(0, 9)
    nanos = parseInt(padded, 10)
  }
  return { h, mi, s, nanos }
}

export function TimestampPicker({ id, value, onChange, disabled, className }: TimestampPickerProps) {
  const [open, setOpen] = useState(false)
  const [date, setDate] = useState<Date>(() => new Date())
  const [nanoseconds, setNanoseconds] = useState(0)
  const [timeText, setTimeText] = useState('')
  const [unixText, setUnixText] = useState('')

  const syncTimeText = useCallback((d: Date, nanos: number) => {
    setTimeText(utcTimeWithNanos(d, nanos))
  }, [])

  const syncUnixText = useCallback((d: Date) => {
    setUnixText(String(d.getTime()))
  }, [])

  const applyFromString = useCallback(
    (raw: string) => {
      const trimmed = raw.trim()
      if (!trimmed) return
      const parsed = parseRfc3339Utc(trimmed)
      if (parsed) {
        setDate(parsed.date)
        setNanoseconds(parsed.nanoseconds)
        syncTimeText(parsed.date, parsed.nanoseconds)
        syncUnixText(parsed.date)
      }
    },
    [syncTimeText, syncUnixText]
  )

  useEffect(() => {
    const trimmed = value?.trim() ?? ''
    if (!trimmed) return
    applyFromString(trimmed)
  }, [value, applyFromString])

  const emit = useCallback(
    (nextDate: Date, nextNanos: number) => {
      setDate(nextDate)
      setNanoseconds(nextNanos)
      syncTimeText(nextDate, nextNanos)
      syncUnixText(nextDate)
      onChange(buildRfc3339Utc(nextDate, nextNanos))
    },
    [onChange, syncTimeText, syncUnixText]
  )

  const onDaySelect = (day: Date | undefined) => {
    if (!day) return
    const sel = utcParts(day)
    const hasExistingValue = !!value?.trim()
    const p = hasExistingValue ? utcParts(date) : { h: 0, mi: 0, s: 0 }
    const ms = hasExistingValue ? msFromNanos(nanoseconds) : 0
    const next = utcDateFromParts(sel.y, sel.mo, sel.day, p.h, p.mi, p.s, ms)
    emit(next, hasExistingValue ? nanoseconds : 0)
    setOpen(false)
  }

  const onTimeBlur = () => {
    const parsed = parseTimeWithNanos(timeText)
    if (!parsed) {
      syncTimeText(date, nanoseconds)
      return
    }
    const { h, mi, s, nanos } = parsed
    const p = utcParts(date)
    const ms = msFromNanos(nanos)
    emit(utcDateFromParts(p.y, p.mo, p.day, h, mi, s, ms), nanos)
  }

  const onUnixBlur = () => {
    const ms = parseInt(unixText, 10)
    if (Number.isNaN(ms) || ms < 0) {
      syncUnixText(date)
      return
    }
    const rfc = unixMillisToRfc3339(ms)
    const parsed = parseRfc3339Utc(rfc)
    if (parsed) {
      emit(parsed.date, parsed.nanoseconds)
    }
  }

  const hasValue = !!value?.trim()
  const timeId = id ? `${id}-time` : 'time-picker'
  const unixId = id ? `${id}-unix` : 'unix-picker'

  return (
    <Tabs defaultValue="datetime" className={className}>
      <TabsList>
        <TabsTrigger value="datetime">Date & Time</TabsTrigger>
        <TabsTrigger value="unix">Unix</TabsTrigger>
      </TabsList>
      <TabsContent value="datetime">
        <FieldGroup className="flex-row">
          <Field>
            <FieldLabel htmlFor={id ?? 'date-picker'}>Date (UTC)</FieldLabel>
            <Popover open={open} onOpenChange={setOpen} modal>
              <PopoverTrigger asChild>
                <Button
                  id={id ?? 'date-picker'}
                  type="button"
                  variant="outline"
                  disabled={disabled}
                  className="w-40 justify-between font-normal"
                >
                  {hasValue ? format(date, 'PPP') : 'Select date'}
                  <ChevronDownIcon />
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto overflow-hidden p-0" align="start">
                <Calendar
                  mode="single"
                  selected={hasValue ? date : undefined}
                  captionLayout="dropdown"
                  defaultMonth={date}
                  onSelect={day => {
                    onDaySelect(day)
                    setOpen(false)
                  }}
                />
              </PopoverContent>
            </Popover>
          </Field>
          <Field className="w-52">
            <FieldLabel htmlFor={timeId}>Time (UTC)</FieldLabel>
            <Input
              type="text"
              id={timeId}
              disabled={disabled}
              value={timeText}
              onChange={e => setTimeText(e.target.value)}
              onBlur={onTimeBlur}
              placeholder="HH:MM:SS.nnnnnnnnn"
            />
          </Field>
        </FieldGroup>
      </TabsContent>
      <TabsContent value="unix">
        <Field>
          <FieldLabel htmlFor={unixId}>Unix time (milliseconds)</FieldLabel>
          <Input
            type="number"
            id={unixId}
            disabled={disabled}
            value={unixText}
            onChange={e => setUnixText(e.target.value)}
            onBlur={onUnixBlur}
            placeholder="e.g. 1718460645123"
            min={0}
          />
        </Field>
      </TabsContent>
    </Tabs>
  )
}
