import { CSSProperties, FC, PropsWithChildren } from 'react'

export const EdgeDetails: FC<PropsWithChildren<{ labelX: number; labelY: number; id: string }>> = ({
  children,
  labelX,
  labelY,
  id,
}) => {
  const wrapperStyle: CSSProperties = {
    position: 'absolute',
    transform: `translate(${labelX}px, ${labelY}px) translate(-50%, 0%)`,
    zIndex: 9999,
  }
  return (
    <div style={wrapperStyle} className="flex flex-col justify-center drop-shadow">
      {children}
      <div className="flex items-center justify-center">
        <div className="transform-x-1/2 transform-y-1/2 h-4 w-4 border-[0.5rem] border-transparent border-t-white bg-transparent"></div>
      </div>
    </div>
  )
}
