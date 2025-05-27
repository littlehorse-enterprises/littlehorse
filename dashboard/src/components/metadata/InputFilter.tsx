import { Input } from "@/components/ui/input"
import { cn } from "@/utils/utils"
interface InputFilterProps {
    label: string
    value: string
    placeholder?: string
    onChange: (value: string) => void
    className?: string
}

export function InputFilter({ label, value, placeholder = "Filter...", onChange, className }: InputFilterProps) {
    return (
        <div className={cn("flex items-center gap-2", className)}>
            <span className="whitespace-nowrap">{label}</span>
            <Input
                placeholder={placeholder}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                className={"h-8 text-xs flex-1"}
            />
        </div>
    )
} 