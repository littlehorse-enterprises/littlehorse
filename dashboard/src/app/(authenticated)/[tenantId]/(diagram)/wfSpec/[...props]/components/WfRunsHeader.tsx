import { TIME_RANGES, TIME_RANGES_NAMES, TimeRange, WF_RUN_STATUSES } from '@/app/constants'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { VariableDef, WfSpec } from 'littlehorse-client/proto'
import { ClockIcon, XIcon } from 'lucide-react'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { FC, useEffect, useMemo, useState } from 'react'
import { usePathname } from 'next/navigation'
import { LHStatus } from 'littlehorse-client/proto'

const searchableVarDefs = (spec: WfSpec): VariableDef[] => {
  return Object.keys(spec.threadSpecs)
    .flatMap(thread =>
      spec.threadSpecs[thread].variableDefs
        .filter(d => d.searchable)
        .map(d => d.varDef)
        .filter((v): v is VariableDef => v !== undefined)
    )
}

type VariableFilter = {
  varDef: VariableDef
  value: string
}

type Props = {
  spec: WfSpec
  currentStatus: LHStatus | 'ALL'
  currentWindow: TimeRange
  setWindow: (window: TimeRange) => void
  variableFilter?: VariableFilter | null
  onVariableFilterChange?: (filter: VariableFilter | null) => void
}

export const WfRunsHeader: FC<Props> = ({
  spec,
  currentStatus,
  currentWindow,
  setWindow,
  variableFilter,
  onVariableFilterChange,
}) => {
  const hasVariableFilter = onVariableFilterChange !== undefined
  const pathname = usePathname()
  const pathWithoutTenant = pathname.replace(/^\/[^/]+/, '')

  const variables = useMemo(() => searchableVarDefs(spec), [spec])
  const [open, setOpen] = useState(false)
  const [selectedName, setSelectedName] = useState(() => variableFilter?.varDef.name ?? variables[0]?.name ?? '')
  const [valueDraft, setValueDraft] = useState(variableFilter?.value ?? '')

  useEffect(() => {
    if (variableFilter) {
      setSelectedName(variableFilter.varDef.name)
      setValueDraft(variableFilter.value)
    }
  }, [variableFilter])

  const selectedDef = useMemo(
    () => variables.find(v => v.name === selectedName) ?? variables[0],
    [variables, selectedName]
  )

  const applyFilter = () => {
    if (!hasVariableFilter || !selectedDef || !valueDraft.trim()) return
    onVariableFilterChange({ varDef: selectedDef, value: valueDraft.trim() })
    setOpen(false)
  }

  const clearFilter = () => {
    if (!hasVariableFilter) return
    setValueDraft('')
    onVariableFilterChange(null)
  }

  return (
    <div className="mb-4 flex min-h-[2.5rem] flex-wrap items-center gap-2">
      <Select value={currentWindow.toString()} onValueChange={value => setWindow(parseInt(value) as TimeRange)}>
        <SelectTrigger className="w-[150px] min-w-fit">
          <div className="flex items-center gap-2">
            <ClockIcon className="h-5 w-5 fill-none stroke-black" />
            <SelectValue>
              {currentWindow !== -1 ? `Last ${TIME_RANGES_NAMES[currentWindow]}` : TIME_RANGES_NAMES[currentWindow]}
            </SelectValue>
          </div>
        </SelectTrigger>
        <SelectContent>
          {TIME_RANGES.map(time => (
            <SelectItem key={time} value={time.toString()}>
              {time !== -1 ? `Last ${TIME_RANGES_NAMES[time]}` : TIME_RANGES_NAMES[time]}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <div className="flex min-w-0">
        {['ALL', ...WF_RUN_STATUSES].map(status => (
          <LinkWithTenant
            key={status}
            href={status === 'ALL' ? pathWithoutTenant : `${pathWithoutTenant}?status=${status}`}
            replace
            scroll={false}
            className={`flex items-center border-y-2 border-l-2 p-2 text-xs first-of-type:rounded-l-lg first-of-type:border-l-2 last-of-type:rounded-r-lg last-of-type:border-r-2 ${
              status === currentStatus ? 'border-blue-500 bg-blue-500 text-white' : ' text-gray-500'
            }`}
          >
            {status}
          </LinkWithTenant>
        ))}
      </div>

      {hasVariableFilter && variables.length > 0 && (
        <>
          {variableFilter && (
            <span
              className="max-w-[14rem] truncate text-xs text-muted-foreground"
              title={`${variableFilter.varDef.name} = ${variableFilter.value}`}
            >
              <span className="font-mono text-foreground">{variableFilter.varDef.name}</span> ={' '}
              <span className="font-mono text-foreground">{variableFilter.value}</span>
            </span>
          )}
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
              <Button type="button" variant="secondary" size="sm" className="shrink-0">
                Search by variable
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Search WfRuns by variable</DialogTitle>
                <DialogDescription>
                  Filters use server search on entrypoint (thread 0) variables. Matching is case-sensitive.
                </DialogDescription>
              </DialogHeader>
              <div className="flex flex-col gap-3">
                <Select value={selectedDef?.name ?? selectedName} onValueChange={name => setSelectedName(name)}>
                  <SelectTrigger>
                    <SelectValue placeholder="Variable" />
                  </SelectTrigger>
                  <SelectContent>
                    {variables.map(v => (
                      <SelectItem key={v.name} value={v.name}>
                        {v.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Input
                  placeholder="Variable value…"
                  value={valueDraft}
                  onChange={e => setValueDraft(e.target.value)}
                  onKeyDown={e => {
                    if (e.key === 'Enter') applyFilter()
                  }}
                />
                <div className="flex flex-wrap gap-2">
                  <Button type="button" onClick={applyFilter} disabled={!selectedDef || !valueDraft.trim()}>
                    Apply
                  </Button>
                  {variableFilter && (
                    <Button type="button" variant="ghost" onClick={clearFilter} className="gap-1">
                      <XIcon className="h-4 w-4" />
                      Clear
                    </Button>
                  )}
                </div>
              </div>
            </DialogContent>
          </Dialog>
        </>
      )}
    </div>
  )
}
