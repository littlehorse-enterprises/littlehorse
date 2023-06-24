import * as React from "react";
interface Props {
    label: string
    children?: React.ReactNode | React.ReactNode[]
    onClick?: () => void
}
export const CInput = ({label, children, onClick=() => {}}:Props) => {
    return  <label className="label" onClick={onClick}>
        <span>{label}</span>
        <div className="input">{children}</div>
    </label>
}