import { VariableMutation } from 'littlehorse-client/proto'
import { Info } from 'lucide-react'
import { FC, useEffect, useRef, useState } from 'react'

export const Mutation: FC<{ mutation: VariableMutation }> = ({ mutation }) => {
  const [show, setShow] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as HTMLElement)) {
        setShow(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  return (
    <div ref={ref} className="relative mb-1 mb-2 flex flex-col gap-1">
      <div className="flex flex-row items-center">
        <p className="flex-1 truncate font-mono">{mutation.lhsName}</p>
        <Info className="h-4 w-4 cursor-pointer" onClick={() => setShow(!show)} />
        {show && <div className="absolute right-0 top-3 mt-2 w-48 bg-white shadow-lg">Hello</div>}
      </div>
    </div>
  )
}
