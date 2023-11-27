'use client'
interface DrawerSectionProps {
  title:string
  children?: React.ReactNode | React.ReactNode[]
}
export function DrawerSection({ title, children }:DrawerSectionProps) {
  return <div className="drawer-section">
    <div className="title">{title}</div>
    {children}
  </div>
}