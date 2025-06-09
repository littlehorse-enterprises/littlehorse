"use client"
import { Input } from "@/components/ui/input"
import { Search } from "lucide-react"

interface SearchHeaderProps {
    prefix: string
    onPrefixChange: (value: string) => void
}

export function SearchHeader({ prefix, onPrefixChange }: SearchHeaderProps) {
    return (
        <div className="mb-8 flex items-center justify-between">
            <h1 className="text-4xl font-bold">Metadata Search</h1>
            <div className="flex items-center gap-2">
                <div className="relative w-80">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <Input
                        placeholder="Search metadata..."
                        className="pl-10 pr-4"
                        value={prefix}
                        onChange={(e) => onPrefixChange(e.target.value)}
                    />
                </div>
            </div>
        </div>
    )
}
