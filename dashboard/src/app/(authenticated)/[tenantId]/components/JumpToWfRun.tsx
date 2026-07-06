'use client'

import { parseWfRunIdInput, wfRunIdToPath } from '@/app/utils'
import { routes, withTenant } from '@/app/routes'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { ArrowRightIcon, CornerDownLeft, Workflow } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { ChangeEvent, FC, FormEvent, useState } from 'react'
import { toast } from 'sonner'

const WORKFLOW_ICON_CLASS =
  'pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400'

type Props = {
  variant?: 'global' | 'inline'
}

export const JumpToWfRun: FC<Props> = ({ variant = 'global' }) => {
  const isInline = variant === 'inline'
  const { tenantId } = useWhoAmI()
  const router = useRouter()
  const [value, setValue] = useState('')
  const [error, setError] = useState<string | null>(null)

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setValue(event.target.value)
    if (error) setError(null)
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    setError(null)

    try {
      const wfRunId = parseWfRunIdInput(value)
      router.push(withTenant(tenantId, routes.wfRun.detail(wfRunIdToPath(wfRunId))))
      setValue('')
    } catch (submitError) {
      const message = submitError instanceof Error ? submitError.message : 'Invalid WfRun ID'
      if (isInline) {
        setError(message)
      } else {
        toast.error(message)
      }
    }
  }

  if (isInline) {
    return (
      <div className="flex w-full flex-col gap-1.5 lg:w-auto lg:min-w-[360px]">
        <label htmlFor="open-wfrun" className="text-xs font-medium uppercase tracking-wide text-gray-500">
          Open WfRun
        </label>
        <form onSubmit={handleSubmit} className="flex gap-2">
          <div className="relative min-w-0 flex-1">
            <Workflow className={WORKFLOW_ICON_CLASS} />
            <Input
              id="open-wfrun"
              type="text"
              value={value}
              onChange={handleChange}
              placeholder="WfRun ID or URL"
              aria-invalid={error ? true : undefined}
              className="pl-9"
            />
          </div>
          <Button type="submit" variant="outline" className="shrink-0">
            Open
            <ArrowRightIcon className="h-4 w-4" />
          </Button>
        </form>
        {error && <p className="text-sm text-red-600">{error}</p>}
      </div>
    )
  }

  return (
    <TooltipProvider delayDuration={300}>
      <form onSubmit={handleSubmit} className="relative w-full">
        <Workflow className={WORKFLOW_ICON_CLASS} />
        <input
          type="text"
          value={value}
          onChange={handleChange}
          placeholder="Open WfRun by ID or URL"
          aria-label="Open WfRun by ID or URL"
          className="h-10 w-full rounded-md border border-white/15 bg-white/5 py-2 pl-10 pr-11 text-sm text-white placeholder:text-gray-500 transition-colors focus:border-white/30 focus:outline-none focus:ring-1 focus:ring-white/20"
        />
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              type="submit"
              size="icon"
              variant="ghost"
              className="absolute right-1 top-1/2 h-8 w-8 -translate-y-1/2 text-gray-400 hover:bg-white/10 hover:text-white"
              aria-label="Open WfRun"
            >
              <CornerDownLeft className="h-4 w-4" />
            </Button>
          </TooltipTrigger>
          <TooltipContent side="bottom">Open WfRun</TooltipContent>
        </Tooltip>
      </form>
    </TooltipProvider>
  )
}
