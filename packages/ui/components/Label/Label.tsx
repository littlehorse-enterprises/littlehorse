export interface Props {
 children:React.ReactNode
}

export const Label = ({children}: Props) => {
    return <div className="label-canvas" >
        {children}
    </div>
}