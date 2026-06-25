import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { DateLike, utcToLocalDateTime } from '@/app/utils'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { Check, Copy } from 'lucide-react'
import { useState } from 'react'

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

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(displayText)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000) // Reset after 2 seconds
    } catch (err) {
      console.error('Failed to copy text: ', err)
    }
  }
  return (
    <div className="mb-1">
      <p className="text-[0.75em] text-slate-400">{label}</p>
      <div className="flex items-center justify-between">
        {type === 'link' ? (
          <LinkWithTenant href={link} className="truncate text-base font-medium text-blue-500">
            {displayText}
          </LinkWithTenant>
        ) : (
          <p className="truncate text-base font-medium">{displayText}</p>
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
  )
}
