export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  icon:string
}

export function Input(props: InputProps) {
  return <div className="input-canvas" ><img alt={props.icon} src={props.icon} />
    <input className={`input ${props.icon ? 'icon' : undefined}`} {...props} />
  </div>
}
