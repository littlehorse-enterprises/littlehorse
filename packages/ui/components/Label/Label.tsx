export interface Props {
 children:React.ReactNode
}

export function Label({ children }: Props) {
  return <div className="label-canvas" >
    {children}
  </div>
}