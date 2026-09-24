'use client'
import { tryFormatAsJson } from '@/app/utils/tryFormatAsJson'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { cn } from '@/components/utils'
import { ChevronRight } from 'lucide-react'
import { FC, useEffect, useRef, useState } from 'react'
import { CopyButton } from './CopyButton'

type OverflowTextProps = {
  text: string
  className?: string
  variant?: 'error'
  /** Renders as regular prose instead of code: left-aligned, default font, no JSON formatting. */
  prose?: boolean
  /** In prose mode, how many lines to show before truncating. Defaults to 2. */
  clampLines?: 1 | 2
}

export const OverflowText: FC<OverflowTextProps> = ({ text, className, variant, prose, clampLines = 2 }) => {
  const textRef = useRef<HTMLDivElement>(null)
  const [isOverflowing, setIsOverflowing] = useState(false)

  useEffect(() => {
    const element = textRef.current
    if (!element) return
    // line-clamp (multi-line) wraps, so measure height; everything else stays one line, measure width.
    setIsOverflowing(
      prose && clampLines !== 1
        ? element.scrollHeight > element.clientHeight
        : element.scrollWidth > element.clientWidth
    )
  }, [text, prose, clampLines])

  const formattedText = prose ? text : tryFormatAsJson(text)

  if (isOverflowing) {
    return (
      <Dialog>
        <DialogTrigger asChild>
          <Button
            variant="ghost"
            className={cn(
              'flex w-full p-1 font-normal hover:no-underline',
              prose ? 'h-auto min-w-0 items-end gap-1' : 'h-full items-center justify-between truncate text-nowrap',
              className
            )}
          >
            <span
              className={cn(
                prose && clampLines !== 1
                  ? 'line-clamp-2 min-w-0 flex-1 whitespace-normal break-words text-left'
                  : prose
                    ? 'min-w-0 flex-1 truncate'
                    : 'truncate font-code'
              )}
            >
              {formattedText}
            </span>
            <div className="flex flex-shrink-0 items-center gap-1 text-nowrap text-xs text-muted-foreground">
              View
              <ChevronRight className="h-4 w-4 opacity-50" />
            </div>
          </Button>
        </DialogTrigger>
        <DialogContent className="max-w-2xl gap-2 overflow-visible">
          <DialogTitle className="sr-only">Full text</DialogTitle>
          <CopyButton value={formattedText} className="h-8 w-8 rounded-full" />
          <div
            className={cn('h-96 overflow-auto rounded-lg bg-gray-100', {
              'bg-status-failed text-red-500': variant === 'error',
            })}
          >
            <div className={cn('max-w-full whitespace-pre-wrap break-words p-4', !prose && 'font-code')}>
              {formattedText}
            </div>
          </div>
        </DialogContent>
      </Dialog>
    )
  }
  return (
    <div
      ref={textRef}
      className={cn(
        className,
        prose && clampLines !== 1
          ? 'line-clamp-2 w-full min-w-0 whitespace-normal break-words'
          : prose
            ? 'w-full min-w-0 truncate'
            : 'flex h-full items-center justify-center font-code'
      )}
    >
      {formattedText}
    </div>
  )
}
