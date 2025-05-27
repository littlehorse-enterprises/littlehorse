import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Checkbox } from "@/components/ui/checkbox"

interface CheckboxFilterProps {
    label: string
    value: boolean | null
    labelTrue?: string
    labelFalse?: string
    onChange: (value: boolean | null) => void
}

export function CheckboxFilter({
    label,
    value,
    labelTrue = "Yes",
    labelFalse = "No",
    onChange
}: CheckboxFilterProps) {

    const handleCheckboxChange = (checked: boolean | "indeterminate") => {
        if (checked === "indeterminate") return

        if (checked) {
            onChange(true)
        } else if (value === true) {
            onChange(null)
        } else {
            onChange(false)
        }
    }

    return (
        <div className="flex flex-col gap-2">
            {value !== null && (
                <Badge className="mb-1 flex items-center">
                    {value ? labelTrue : labelFalse}
                    <Button
                        variant="ghost"
                        size="icon"
                        className="h-4 w-4 ml-1 p-0"
                        onClick={() => onChange(null)}
                    >
                        <span className="sr-only">Clear filter</span>
                        &times;
                    </Button>
                </Badge>
            )}
            <div className="flex items-center gap-2">
                <span>{label}</span>
                <Checkbox
                    checked={value === true}
                    onCheckedChange={handleCheckboxChange}
                />
            </div>
        </div>
    )
} 