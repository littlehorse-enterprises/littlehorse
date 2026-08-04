import { copyToClipboard } from '@/app/utils/copyToClipboard'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { DateLike, utcToLocalDateTime } from '@/app/utils'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { Check, Copy, Expand } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { useModal } from '../../../hooks/useModal'

export const NodeVariable = ({
  label,
  text = '',
  type = 'text',
  link = '',
  className = '',
}: {
  label: string
  text?: DateLike
  type?: string
  link?: string
  className?: string
}) => {
  const displayText = type === 'date' ? utcToLocalDateTime(text) : typeof text === 'string' ? text : String(text ?? '')
  const [copied, setCopied] = useState(false)
  const [isOverflowing, setIsOverflowing] = useState(false)
  const textRef = useRef<HTMLParagraphElement>(null)
  const { setModal, setShowModal } = useModal()

  // Show the expand affordance only when the value is actually truncated.
  useEffect(() => {
    const el = textRef.current
    if (!el) return
    const checkOverflow = () => setIsOverflowing(el.scrollWidth > el.clientWidth)
    checkOverflow()
    if (typeof ResizeObserver === 'undefined') return
    const observer = new ResizeObserver(checkOverflow)
    observer.observe(el)
    return () => observer.disconnect()
  }, [displayText])

  const handleCopy = async () => {
    try {
      await copyToClipboard(displayText)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000) // Reset after 2 seconds
    } catch (err) {
      console.error('Failed to copy text: ', err)
    }
  }

  const handleExpand = () => {
    setModal({ type: 'output', data: { message: displayText, label: label.replace(/:\s*$/, '') } })
    setShowModal(true)
  }

  return (
    <div className="mb-1">
      <p className="text-[0.75em] text-slate-400">{label}</p>
      <div className="flex items-center justify-between gap-1">
        {type === 'link' ? (
          <LinkWithTenant href={link} className="truncate text-base font-medium text-blue-500">
            {displayText}
          </LinkWithTenant>
        ) : (
          <p ref={textRef} className="min-w-0 truncate text-base font-medium">
            {displayText}
          </p>
        )}
        <div className="flex shrink-0 items-center">
          {type !== 'link' && isOverflowing && (
            <button
              onClick={handleExpand}
              aria-label="Expand value"
              className="inline-flex h-5 items-center justify-center rounded transition-colors hover:bg-gray-100"
            >
              <Expand className="h-3.5 w-3.5 text-gray-500 hover:text-gray-700" />
            </button>
          )}
          <TooltipProvider delayDuration={0}>
            <Tooltip>
              <TooltipTrigger asChild>
                <button
                  onClick={handleCopy}
                  className={`inline-flex h-5 items-center justify-center rounded transition-colors hover:bg-gray-100 ${className}`}
                >
                  {copied ? (
                    <Check className="h-3.5 w-3.5 text-green-500" />
                  ) : (
                    <Copy className="h-3.5 w-3.5 text-gray-500 hover:text-gray-700" />
                  )}
                </button>
              </TooltipTrigger>
              <TooltipContent>
                <p>{displayText}</p>
              </TooltipContent>
            </Tooltip>
          </TooltipProvider>
        </div>
      </div>
    </div>
  )
}
