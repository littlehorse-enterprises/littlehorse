import { utcToLocalDateTime } from '@/app/utils'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'

export const NodeVariable = ({ label, text = '', type = 'text' }: { label: string; text?: string; type?: string }) => {
  if (type === 'date') {
    text = utcToLocalDateTime(text)
  }
  return (
    <div className="ml-1 mt-1 grid grid-cols-2">
      <div className=" text-sm font-bold">{label}</div>
      <div className="truncate  text-xs text-slate-400">
        <TooltipProvider delayDuration={0}>
          <Tooltip>
            <TooltipTrigger asChild>
              <span>{text}</span>
            </TooltipTrigger>
            <TooltipContent> {text}</TooltipContent>
          </Tooltip>
        </TooltipProvider>
      </div>
    </div>
  )
}
