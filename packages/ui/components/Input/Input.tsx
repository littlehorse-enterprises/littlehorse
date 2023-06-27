export interface Props extends React.InputHTMLAttributes<HTMLInputElement> {
 icon:string
}

export const Input = (props: Props) => {
    return <div className="input-canvas" ><img src={props.icon} alt={props.icon}></img>
        <input className={`input ${!!props.icon ? 'icon' : undefined}`} {...props}></input>
    </div>
}