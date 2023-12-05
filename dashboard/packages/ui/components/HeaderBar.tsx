'use client'
interface HeaderBarProps {
    children: React.ReactNode | React.ReactNode[]
}
export function HeaderBar({ children }:HeaderBarProps) {
    return <div className="header-bar">
        {children}
    </div>
}
