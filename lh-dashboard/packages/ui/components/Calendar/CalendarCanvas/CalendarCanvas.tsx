'use client'

import React, { useState, useEffect } from 'react'
import moment from 'moment'
import { Content } from '../CalendarComponents'

interface CalendarCanvasProps {
  type: string;
  lastDate: Date;
  setEndDT: (dt?: Date) => void;
  setStartDT: (dt?: Date) => void;
  onApply: () => void;
  outsideCalendarClickRef: React.LegacyRef<HTMLDivElement>;
}

// Calendar Canvas will be drilled with outsideCalendarClickRef so the click outside custom hook anchors the element and doesn't reopen
export function CalendarCanvas({ type, setEndDT, setStartDT, onApply, lastDate, outsideCalendarClickRef }: CalendarCanvasProps) {

  const [ date, setDate ] = useState<Date>(moment().toDate())
  const [ selected, setSelected ] = useState<Date | undefined>(lastDate)
  const [ endSelected, setEndSelected ] = useState<Date | undefined>()

  const updateEndSelectedH = (H: string) => {
    setEndSelected(
      endSelected
        ? moment(endSelected).set('hour', Number(H)).toDate()
        : moment(selected).set('hour', Number(H)).toDate()
    )
  }
  const updateEndSelectedM = (M: string) => {
    setEndSelected(
      endSelected
        ? moment(endSelected).set('minute', Number(M)).toDate()
        : moment(selected).set('minute', Number(M)).toDate()
    )
  }
  const updateSelectedH = (H: string) => {
    setSelected(moment(selected).set('hour', Number(H)).toDate())
  }
  const updateSelectedM = (M: string) => {
    setSelected(moment(selected).set('minute', Number(M)).toDate())
  }
  const selectDate = (date: Date) => {
    if (endSelected) {
      setSelected(date)
      setStartDT(date)
      setEndSelected(undefined)
      setEndDT(undefined)
    } else if (selected) {
      if (date > selected) {
        setEndDT(date)
        setEndSelected(date)
      } else {
        setEndDT(selected)
        setEndSelected(selected)
        setSelected(date)
        setStartDT(date)
      }
    } else {
      setSelected(date)
      setStartDT(date)
    }
  }

  useEffect(() => { setEndDT(endSelected) }, [ endSelected ])
  useEffect(() => { setStartDT(selected) }, [ selected ])

  const nextMonth = () => {
    setDate((date) => moment(date).add(1, 'month').toDate())
  }
  const prevMonth = () => {
    setDate((date) => moment(date).subtract(1, 'month').toDate())
  }

  return (
    <div className="flex float" ref={outsideCalendarClickRef}>
      <Content
        endSelected={endSelected}
        init={date}
        nextMonth={nextMonth}
        onApply={onApply}
        prevMonth={prevMonth}
        selectDay={selectDate}
        selected={selected}
        type={type}
        updateEndSelectedH={updateEndSelectedH}
        updateEndSelectedM={updateEndSelectedM}
        updateSelectedH={updateSelectedH}
        updateSelectedM={updateSelectedM}
      />
    </div>
  )
}
