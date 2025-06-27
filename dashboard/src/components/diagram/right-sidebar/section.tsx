import { ReactNode, Children, useState } from 'react'

interface SectionProps {
    title: string
    children?: ReactNode
}

export function Section({ title, children }: SectionProps) {
    const [isCollapsed, setIsCollapsed] = useState(false)

    const toggleCollapsed = () => {
        setIsCollapsed(!isCollapsed)
    }

    return (
        <div className="rounded-md border border-gray-200 bg-gray-50 p-2 w-full mt-2">
            <button
                onClick={toggleCollapsed}
                className="flex items-center justify-between w-full mb-2 text-xs font-medium hover:bg-gray-100 rounded p-1 transition-colors"
            >
                <span>{title}</span>
                <svg
                    width="12"
                    height="12"
                    fill="none"
                    viewBox="0 0 24 24"
                    className={`transition-transform duration-200 ${isCollapsed ? 'rotate-90' : '-rotate-90'}`}
                >
                    <path
                        d="M9 18l6-6-6-6"
                        stroke="#656565"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                </svg>
            </button>
            {!isCollapsed && (
                <div className="space-y-1 text-xs">
                    {Children.count(children) === 0 && (
                        <div className="text-gray-500">No data</div>
                    )}
                    {children}
                </div>
            )}
        </div>
    )
} 