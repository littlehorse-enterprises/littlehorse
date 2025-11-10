'use client'
import { Button } from '@/components/ui/button'
import { cn } from '@/components/utils'
import { CheckIcon, CopyIcon } from 'lucide-react'
import { FC, useState } from 'react'

interface CopyButtonProps {
  value: string
  className?: string
}

export const CopyButton: FC<CopyButtonProps> = ({ value, className }) => {
  const [copied, setCopied] = useState(false)

  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={() => {
        navigator.clipboard.writeText(value)
        setCopied(true)
        setTimeout(() => setCopied(false), 1000)
      }}
      className={cn(className, 'cursor-pointer')}
    >
      {copied ? <CheckIcon className="h-4 w-4 text-green-500" /> : <CopyIcon className="h-4 w-4" />}
    </Button>
  )
}
