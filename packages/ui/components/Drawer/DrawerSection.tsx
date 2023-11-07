'use client'
interface Props {
    title:string
    children?: React.ReactNode | React.ReactNode[]
}
export function DrawerSection({ title, children }:Props) {
  return <div className="drawer-section">
    <div className="title">{title}</div>
    {children}
  </div>
}