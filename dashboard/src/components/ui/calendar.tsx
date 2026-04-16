'use client'

import { ChevronLeft, ChevronRight } from 'lucide-react'
import * as React from 'react'
import { DayPicker } from 'react-day-picker'

import { cn } from '@/lib/utils'

import 'react-day-picker/style.css'

export type CalendarProps = React.ComponentProps<typeof DayPicker>

function Calendar({ className, ...props }: CalendarProps) {
  return (
    <DayPicker
      className={cn('p-3', className)}
      components={{
        Chevron: ({ orientation }) =>
          orientation === 'left' ? <ChevronLeft className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />,
      }}
      {...props}
    />
  )
}
Calendar.displayName = 'Calendar'

export { Calendar }
