import { ArrowLeft, ChevronDown } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { useRouter } from "next/navigation"
import { useState } from "react"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Combobox, ComboboxOption } from "@/components/ui/combobox"

interface MetadataHeaderProps {
    title: string
    type: string
    version?: string | string[]
    backUrl: string
    backText?: string
    additionalBadges?: React.ReactNode
}

export function MetadataHeader({
    title,
    type,
    version,
    backUrl,
    backText,
    additionalBadges
}: MetadataHeaderProps) {
    const router = useRouter()
    const [selectedVersion, setSelectedVersion] = useState<string | undefined>(
        Array.isArray(version) ? version[0] : version
    )

    const handleVersionChange = (newVersion: string) => {
        setSelectedVersion(newVersion)
    }

    const renderVersionBadge = () => {
        if (!version) return null

        if (Array.isArray(version)) {
            // Convert version array to combobox options
            const versionOptions: ComboboxOption[] = version.map(v => ({
                value: v,
                label: v
            }))

            return (
                <div className="min-w-[150px]">
                    <Combobox
                        options={versionOptions}
                        value={selectedVersion}
                        onValueChange={handleVersionChange}
                        placeholder="Select version"
                        className="h-8 text-xs px-3 py-0 font-mono"
                        popoverWidth="w-[200px]"
                    />
                </div>
            )
        }

        return (
            <div className="flex items-center gap-1">
                <Badge variant="outline" className="font-mono">
                    v{version}
                </Badge>
            </div>
        )
    }

    return (
        <div className="mb-6">
            <Button
                variant="ghost"
                className="gap-1 mb-4"
                onClick={() => router.push(backUrl)}
            >
                <ArrowLeft className="h-4 w-4" />
                {backText || `Go back to ${type}`}
            </Button>

            <div className="flex justify-between items-start">
                <div>
                    <div className="text-sm text-muted-foreground">{type}</div>
                    <h1 className="text-3xl font-bold">{title}</h1>
                    <div className="flex items-center gap-2 mt-1">
                        {renderVersionBadge()}
                        {additionalBadges}
                    </div>
                </div>
            </div>
        </div>
    )
} 