'use client'
import { copyToClipboard } from '@/app/utils/copyToClipboard'
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
      onClick={async () => {
        try {
          await copyToClipboard(value)
          setCopied(true)
          setTimeout(() => setCopied(false), 1000)
        } catch (err) {
          console.error('Failed to copy text: ', err)
        }
      }}
      className={cn(className, 'cursor-pointer')}
    >
      {copied ? <CheckIcon className="h-4 w-4 text-green-500" /> : <CopyIcon className="h-4 w-4" />}
    </Button>
  )
}
