import { ReactNode, Children } from 'react'

interface SectionProps {
    title: string
    children?: ReactNode
}

export function Section({ title, children }: SectionProps) {
    return (
        <div className="rounded-md border border-gray-200 bg-gray-50 p-3 w-full">
            <h4 className="mb-2 text-xs font-medium">{title}</h4>
            <div className="space-y-1 text-xs">
                {Children.count(children) === 0 && (
                    <div className="text-gray-500">No data</div>
                )}
                {children}
            </div>
        </div>
    )
} 