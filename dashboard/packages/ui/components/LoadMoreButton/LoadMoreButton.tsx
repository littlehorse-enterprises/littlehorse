import * as React from 'react'


interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  loading?: boolean;
  disabled?: boolean;
  children: React.ReactNode;
}

const LoadMoreButton: React.ForwardRefRenderFunction<
HTMLButtonElement,
ButtonProps
/*
 eslint-disable-next-line react/function-component-definition
 */
> = ({ loading, disabled, children, ...props }: ButtonProps) => {
  // return <button disabled={props.disabled} className={`load_more_btn ${props.disabled && 'disabled'}`} {...props}>{props.children} <img src="/add.svg" alt="add" /></button>;
  return (
    <button
      className={`load_more_btn ${disabled ? 'disabled' : null}`}
      disabled={disabled}
      {...props}
    >
      {children}
      {loading ? (
        <img alt="Spinner" className="spinner" src="/Spinner.svg" />
      ) : (
        <img alt="add" src="/add.svg" />
      )}
    </button>
  )
}

LoadMoreButton.displayName = 'LoadMoreButton'

export default LoadMoreButton
