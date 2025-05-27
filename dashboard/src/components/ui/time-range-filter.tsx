"use client"

import type React from "react"
import { useState } from "react"
import { Clock } from "lucide-react"
import { TIME_RANGES } from "@/components/flow/mock-data"

import {
    DropdownMenu,
    DropdownMenuTrigger,
    DropdownMenuContent,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuRadioGroup,
    DropdownMenuRadioItem,
} from "@/components/ui/dropdown-menu"
import { Button } from "@/components/ui/button"

export interface TimeRangeFilterProps {
    title: string
    onFilterChange: (minutes: number | null) => void
    initialValue?: string
}

export function TimeRangeFilter({ title, onFilterChange, initialValue = "all" }: TimeRangeFilterProps) {
    const [selectedValue, setSelectedValue] = useState<string>(initialValue)

    const handleValueChange = (value: string) => {
        setSelectedValue(value)
        const selectedOption = TIME_RANGES.find(option => option.value === value)
        onFilterChange(selectedOption?.minutes ?? null)
    }

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="h-8 px-2 gap-1">
                    {title}
                    {selectedValue !== "all" && (
                        <span className="ml-1 rounded-full bg-primary w-2 h-2" />
                    )}
                    <Clock className="h-3.5 w-3.5 text-muted-foreground/70" />
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="start" className="w-[200px]">
                <DropdownMenuLabel className="text-xs">Filter by time range</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuRadioGroup value={selectedValue} onValueChange={handleValueChange}>
                    {TIME_RANGES.map((option) => (
                        <DropdownMenuRadioItem key={option.value} value={option.value}>
                            {option.label}
                        </DropdownMenuRadioItem>
                    ))}
                </DropdownMenuRadioGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    )
} 