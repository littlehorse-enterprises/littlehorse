"use client";
interface Props {
    children: React.ReactNode | React.ReactNode[]
}
export const HeaderBar = ({children}:Props) => {
    return <div className="header-bar">
        {children}
    </div>
}