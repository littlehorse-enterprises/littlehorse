import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { utcToLocalDateTime } from '@/app/utils'
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
  text?: string
  type?: string
  link?: string
  className?: string
}) => {
  if (type === 'date') {
    text = utcToLocalDateTime(text)
  }
  const [copied, setCopied] = useState(false)

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(text)
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
            {text}
          </LinkWithTenant>
        ) : (
          <p className="truncate text-base font-medium">{text}</p>
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
              <p>{text}</p>
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      </div>
    </div>
  )
}
