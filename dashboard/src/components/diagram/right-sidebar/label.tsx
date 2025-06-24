import { ReactNode } from 'react'

interface LabelProps {
    label: string
    children: ReactNode
    valueClassName?: string
}

export function Label({ label, children, valueClassName = "font-mono" }: LabelProps) {
    const displayValue = children === undefined || children === null ? 'N/A' : children

    return (
        <div className="flex justify-between">
            <span className="text-[#656565]">{label}:</span>
            <span className={valueClassName}>{displayValue}</span>
        </div>
    )
} 