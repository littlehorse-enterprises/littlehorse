import * as React from "react";

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  active?:boolean
}

const Button: React.ForwardRefRenderFunction<HTMLButtonElement, ButtonProps> = (props: ButtonProps) => {
  return <button className={`btn ${props.active && 'active-purple'}  `} {...props}>{props.children}</button>;
}

Button.displayName = "Button";

export default Button;
