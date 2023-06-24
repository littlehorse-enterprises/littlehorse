import * as React from "react";

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

const Button: React.ForwardRefRenderFunction<HTMLButtonElement, ButtonProps> = (props: ButtonProps) => {
  return <button {...props}>{props.children}</button>;
}

Button.displayName = "Button";

export default Button;
