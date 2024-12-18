'use client'
import { FC, useEffect, useRef, useState } from 'react'
import { cn } from '@/components/utils'
import { Button } from '@/components/ui/button'
import { ChevronRight } from 'lucide-react'
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/dialog'
import { CopyButton } from './CopyButton'
import { tryFormatAsJson } from '@/app/utils/tryFormatAsJson'

type OverflowTextProps = {
  text: string
  className?: string
}

export const OverflowText: FC<OverflowTextProps> = ({ text, className }) => {
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
              'flex h-auto w-full items-center justify-between truncate p-1 font-normal hover:no-underline',
              className
            )}
          >
            <span className="truncate">{formattedText}</span>
            <div className="flex flex-shrink-0 items-center gap-1 text-xs text-muted-foreground">
              View More
              <ChevronRight className="h-4 w-4 opacity-50" />
            </div>
          </Button>
        </DialogTrigger>
        <DialogContent className="max-w-2xl gap-2 overflow-visible">
          <CopyButton value={formattedText} className="h-8 w-8 rounded-full" />
          <div className="h-96 overflow-auto rounded-lg bg-gray-100">
            <div className="max-w-full whitespace-pre-wrap break-words p-4">{formattedText}</div>
          </div>
        </DialogContent>
      </Dialog>
    )
  }
  return (
    <div ref={textRef} className={className}>
      {formattedText}
    </div>
  )
}
