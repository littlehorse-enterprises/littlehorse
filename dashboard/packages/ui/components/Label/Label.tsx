export interface LabelProps {
  children:React.ReactNode
}

export function Label({ children }: LabelProps) {
  return <div className="label-canvas" >
    {children}
  </div>
}
