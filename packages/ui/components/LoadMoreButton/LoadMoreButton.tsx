import * as React from "react";
/* eslint-disable @next/next/no-img-element */

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  loading: boolean
}

const LoadMoreButton: React.ForwardRefRenderFunction<HTMLButtonElement, ButtonProps> = (props: ButtonProps) => {
  // return <button disabled={props.disabled} className={`load_more_btn ${props.disabled && 'disabled'}`} {...props}>{props.children} <img src="/add.svg" alt="add" /></button>;
  return <button disabled={props.disabled} className={`load_more_btn ${props.disabled && 'disabled'}`} {...props}>
      {props.children} 
      {props.loading ? 
        <img className="spinner" src="/Spinner.svg" alt="Spinner" /> 
        : 
        <img src="/add.svg" alt="add" /> 
      }
  </button>;
}

LoadMoreButton.displayName = "LoadMoreButton";

export default LoadMoreButton;
