export interface Props extends React.InputHTMLAttributes<HTMLInputElement> {
 icon:string
}

export function Input(props: Props) {
  return <div className="input-canvas" ><img alt={props.icon} src={props.icon} />
    <input className={`input ${props.icon ? 'icon' : undefined}`} {...props} />
  </div>
}