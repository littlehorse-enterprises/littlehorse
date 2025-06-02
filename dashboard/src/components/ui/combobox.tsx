"use client"

import { Check, ChevronsUpDown } from "lucide-react"
import * as React from "react"

import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
} from "@/components/ui/command"
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover"
import { cn } from "@/lib/utils"
import { Button } from "@littlehorse-enterprises/ui-library/button"

export interface ComboboxOption {
    value: string
    label: string
}

interface ComboboxProps {
    options: ComboboxOption[]
    value?: string
    onValueChange?: (value: string) => void
    placeholder?: string
    emptyMessage?: string
    className?: string
    popoverWidth?: string
}

export function Combobox({
    options,
    value,
    onValueChange,
    placeholder = "Select option...",
    emptyMessage = "No options found.",
    className,
    popoverWidth = "w-[200px]"
}: ComboboxProps) {
    const [open, setOpen] = React.useState(false)
    const [selectedValue, setSelectedValue] = React.useState<string | undefined>(value)
    const [searchQuery, setSearchQuery] = React.useState("")

    // Update internal state when value prop changes
    React.useEffect(() => {
        if (value !== undefined) {
            setSelectedValue(value)
        }
    }, [value])

    // Find the selected option's label to display
    const selectedOption = options.find((option) => option.value === selectedValue)

    // Filter options based on exact match of search query
    const filteredOptions = searchQuery === ""
        ? options
        : options.filter(option =>
            option.value.toLowerCase().includes(searchQuery.toLowerCase()) ||
            option.label.toLowerCase().includes(searchQuery.toLowerCase())
        )

    const handleSelect = (currentValue: string) => {
        // Only update if value is different
        if (currentValue !== selectedValue) {
            setSelectedValue(currentValue)
            onValueChange?.(currentValue)
        }
        setOpen(false)
        setSearchQuery("")
    }

    return (
        <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
                <Button
                    variant="outline"
                    role="combobox"
                    aria-expanded={open}
                    className={cn("justify-between", className)}
                >
                    {selectedOption ? `v${selectedOption.value}` : placeholder}
                    <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                </Button>
            </PopoverTrigger>
            <PopoverContent className={cn("p-0", popoverWidth)}>
                <Command shouldFilter={false}>
                    <CommandInput
                        placeholder={`Search ${placeholder.toLowerCase()}...`}
                        value={searchQuery}
                        onValueChange={setSearchQuery}
                    />
                    <CommandEmpty>{emptyMessage}</CommandEmpty>
                    <CommandGroup>
                        {filteredOptions.map((option) => (
                            <CommandItem
                                key={option.value}
                                value={option.value}
                                onSelect={handleSelect}
                            >
                                <Check
                                    className={cn(
                                        "mr-2 h-4 w-4",
                                        selectedValue === option.value ? "opacity-100" : "opacity-0"
                                    )}
                                />
                                {option.label}
                            </CommandItem>
                        ))}
                    </CommandGroup>
                </Command>
            </PopoverContent>
        </Popover>
    )
}
