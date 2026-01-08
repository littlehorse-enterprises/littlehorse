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
  const [isHovered, setIsHovered] = useState(false)

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
    <div
      className="ml-1 grid grid-cols-2 rounded pt-1 hover:bg-gray-100 "
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className=" text-sm font-bold">{label}</div>
      <div className="truncate  text-xs text-slate-400">
        <div className="flex justify-between">
          {type === 'link' ? (
            <LinkWithTenant href={link} className="truncate text-blue-500 ">
              {text}
            </LinkWithTenant>
          ) : (
            <div className="truncate"> {text}</div>
          )}
          {isHovered && (
            <TooltipProvider delayDuration={0}>
              <Tooltip>
                <TooltipTrigger asChild>
                  <button
                    onClick={handleCopy}
                    className={` inline-flex h-4 items-center justify-center rounded transition-colors hover:bg-gray-100 ${className}`}
                  >
                    {copied ? (
                      <Check className="h-4 w-4 text-green-500" />
                    ) : (
                      <Copy className="h-4 w-4 text-gray-500 hover:text-gray-700" />
                    )}
                  </button>
                </TooltipTrigger>
                <TooltipContent>
                  <p>{text}</p>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          )}
        </div>
      </div>
    </div>
  )
}
