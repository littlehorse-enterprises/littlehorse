import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Filter } from "lucide-react"

interface StatusOption {
    value: string
    label: string
}

interface StatusFilterProps {
    label: string
    value: string | undefined
    options: StatusOption[]
    onChange: (value: string | undefined) => void
}

export function StatusFilter({ label, value, options, onChange }: StatusFilterProps) {
    return (
        <div className="flex flex-col gap-2">
            {value && (
                <Badge className="mb-1 flex items-center">
                    {value}
                    <Button
                        variant="ghost"
                        size="icon"
                        className="h-4 w-4 ml-1 p-0"
                        onClick={() => onChange(undefined)}
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
                        <Filter className="h-3 w-3" />
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="w-64 p-2" align="start" side="bottom">
                    <div className="space-y-2">
                        {options.map(option => (
                            <Button
                                key={option.value}
                                variant={value === option.value ? "default" : "outline"}
                                size="sm"
                                className="w-full"
                                onClick={() => onChange(option.value)}
                            >
                                {option.label}
                            </Button>
                        ))}
                    </div>
                </PopoverContent>
            </Popover>
        </div>
    )
} 