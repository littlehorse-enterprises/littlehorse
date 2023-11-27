import * as React from 'react'
import { utc } from 'moment'
/*
 eslint-disable-next-line import/no-unresolved
 */
import 'material-icons/iconfont/material-icons.css'

export interface YearSelectorProps {
  prevMonth: () => void;
  nextMonth: () => void;
  date: string;
}
export function YearSelector({
  prevMonth,
  nextMonth,
  date
}: YearSelectorProps) {
  return (
    <div className="header">
      <div className="lfBtn" onClick={prevMonth}>
        <span className="material-icons">chevron_left</span>
      </div>
      <div className="yearSelector">
        <div className="calMonth">{utc(date).format('MMMM')}</div>
        <div className="calYear">{utc(date).format('Y')}</div>
      </div>
      <div className="lfBtn" onClick={nextMonth}>
        <span className="material-icons">chevron_right</span>
      </div>
    </div>
  )
}
