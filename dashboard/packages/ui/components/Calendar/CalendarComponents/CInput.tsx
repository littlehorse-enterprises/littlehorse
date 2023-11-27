import * as React from 'react'

interface CInputProps {
  label: string;
  children?: React.ReactNode | React.ReactNode[];
  onClick?: () => void;
}
export function CInput({ label, children, onClick }: CInputProps) {

  if (onClick === undefined) {
    return (
      <label className="label">
        <span>{label}</span>
        <div className="input">{children}</div>
      </label>
    )
  }

  return (
    <label className="label" onClick={onClick}>
      <span>{label}</span>
      <div className="input">{children}</div>
    </label>
  )
}
