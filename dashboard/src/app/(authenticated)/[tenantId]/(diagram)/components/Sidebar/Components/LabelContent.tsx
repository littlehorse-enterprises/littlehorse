import { FC } from 'react'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'

type Props = {
  label: string
  content?: string
}

export const LabelContent: FC<Props> = ({ label, content }) => {
  return (
    <div className="mb-2">
      <p className="text-[0.75em] text-slate-400">{label}</p>

      <TooltipProvider delayDuration={0}>
        <Tooltip>
          <TooltipTrigger asChild>
            {content && <p className="truncate text-lg font-medium">{content}</p>}
          </TooltipTrigger>
          <TooltipContent>{content} </TooltipContent>
        </Tooltip>
      </TooltipProvider>
    </div>
  )
}
