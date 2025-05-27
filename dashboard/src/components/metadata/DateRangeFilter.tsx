import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { CalendarIcon } from "lucide-react"
import { format } from "date-fns"
import { DateRange } from "react-day-picker"

interface DateRangeFilterProps {
    label: string
    dateRange: DateRange | undefined
    onDateRangeChange: (range: DateRange | undefined) => void
}

export function DateRangeFilter({ label, dateRange, onDateRangeChange }: DateRangeFilterProps) {
    return (
        <div className="flex flex-col gap-2">
            {dateRange && (
                <Badge variant="outline" className="text-xs flex items-center mb-1">
                    {dateRange.from && format(dateRange.from, "MM/dd/yyyy")}
                    {dateRange.from && dateRange.to && " - "}
                    {dateRange.to && format(dateRange.to, "MM/dd/yyyy")}
                    <Button
                        variant="ghost"
                        size="icon"
                        className="h-4 w-4 ml-1 p-0"
                        onClick={() => onDateRangeChange(undefined)}
                    >
                        <span className="sr-only">Clear filter</span>
                        &times;
                    </Button>
                </Badge>
            )}
            <Popover>
                <PopoverTrigger asChild>
                    <Button variant="ghost" size="sm" className="h-8 px-2 flex gap-1 justify-between w-full">
                        <span>{label}</span>
                        <CalendarIcon className="h-3 w-3" />
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="flex flex-col gap-2 p-2 w-auto" align="start" side="bottom" sideOffset={5}>
                    <div className="grid gap-2" style={{ maxWidth: "280px" }}>
                        <p className="text-xs font-medium">Select date range:</p>
                        <Calendar
                            mode="range"
                            selected={dateRange}
                            onSelect={onDateRangeChange}
                            className="rounded-md border"
                            numberOfMonths={1}
                            initialFocus
                        />
                    </div>
                </PopoverContent>
            </Popover>
        </div>
    )
} 