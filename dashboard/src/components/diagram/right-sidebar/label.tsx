import { cn } from '@/utils/ui/utils'
import { ReactNode } from 'react'

interface LabelProps {
  label: string
  children: ReactNode
  variant?: 'default' | 'highlight'
}

export function Label({ label, children, variant = 'default' }: LabelProps) {
  const displayValue = children === undefined || children === null ? 'N/A' : children

  return (
    <div className="flex w-full gap-2">
      <span className="flex-1 overflow-hidden text-ellipsis text-[#656565]">{label}:</span>
      <span
        className={cn('flex-1 overflow-hidden text-ellipsis', variant === 'highlight' && 'font-mono text-blue-600')}
      >
        {displayValue}
      </span>
    </div>
  )
}
