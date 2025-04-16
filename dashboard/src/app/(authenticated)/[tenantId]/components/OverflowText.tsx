'use client'
import { tryFormatAsJson } from '@/app/utils/tryFormatAsJson'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/dialog'
import { cn } from '@/components/utils'
import { ChevronRight } from 'lucide-react'
import { FC, useEffect, useRef, useState } from 'react'
import { CopyButton } from './CopyButton'

type OverflowTextProps = {
  text: string
  className?: string
  variant?: 'error'
}

export const OverflowText: FC<OverflowTextProps> = ({ text, className, variant }) => {
  const textRef = useRef<HTMLDivElement>(null)
  const [isOverflowing, setIsOverflowing] = useState(false)

  useEffect(() => {
    const element = textRef.current
    if (element) {
      setIsOverflowing(element.scrollWidth > element.clientWidth)
    }
  }, [text])

  const formattedText = tryFormatAsJson(text)

  if (isOverflowing) {
    return (
      <Dialog>
        <DialogTrigger asChild>
          <Button
            variant="ghost"
            className={cn(
              'flex h-full w-full items-center justify-between truncate text-nowrap p-1 font-normal hover:no-underline',
              className
            )}
          >
            <span className="truncate font-code">{formattedText}</span>
            <div className="flex flex-shrink-0 items-center gap-1 text-nowrap text-xs text-muted-foreground">
              View
              <ChevronRight className="h-4 w-4 opacity-50" />
            </div>
          </Button>
        </DialogTrigger>
        <DialogContent className="max-w-2xl gap-2 overflow-visible">
          <CopyButton value={formattedText} className="h-8 w-8 rounded-full" />
          <div
            className={cn('h-96 overflow-auto rounded-lg bg-gray-100', {
              'bg-status-failed text-red-500': variant === 'error',
            })}
          >
            <div className="max-w-full whitespace-pre-wrap break-words p-4 font-code">{formattedText}</div>
          </div>
        </DialogContent>
      </Dialog>
    )
  }
  return (
    <div ref={textRef} className={cn(className, 'flex h-full items-center justify-center font-code')}>
      {formattedText}
    </div>
  )
}
