import * as React from 'react'

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  onClick?: () => void;
  active?: boolean;
  className?: string;
  children: React.ReactNode;
}

/*
 eslint-disable-next-line react/function-component-definition
 */
const Button: React.ForwardRefRenderFunction<HTMLButtonElement, ButtonProps> = ({ active, children, onClick, className }: ButtonProps) => {
  return (
    <button
      className={`btn ${active ? 'active-purple' : null} ${className}` }
      onClick={onClick}
    >
      {children}
    </button>
  )
}

Button.displayName = 'Button'

export default Button
