import { Separator } from '@/components/ui/separator'
import { ReactNode } from 'react'

export function Entry({ label, separator, children }: { label?: string; separator?: boolean; children: ReactNode }) {
  return (
    <div className="flex flex-col">
      <p className="text-xs font-light">{label}</p>
      {separator && <Separator orientation="horizontal" className="my-2" />}
      {children}
    </div>
  )
}
