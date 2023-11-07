'use client'
interface Props {
    children: React.ReactNode | React.ReactNode[]
}
export function HeaderBar({ children }:Props) {
  return <div className="header-bar">
    {children}
  </div>
}