import { GitBranch } from 'lucide-react'
import { FC } from 'react'
import { conditionLabelParts, LabelEdge, LabelPartKind } from './edgeLabel'

const chipClass: Record<Exclude<LabelPartKind, 'keyword' | 'operator'>, string> = {
  source: 'bg-fuchsia-50 text-fuchsia-800 ring-fuchsia-200/80',
  output: 'bg-emerald-50 text-emerald-800 ring-emerald-200/80',
  literal: 'bg-slate-50 text-slate-700 ring-slate-200/80',
}

export const EdgeConditionLabel: FC<{ edge: LabelEdge }> = ({ edge }) => {
  const parts = conditionLabelParts(edge)
  if (parts.length === 0) return null
  return (
    <span
      className="inline-flex items-center gap-1 whitespace-nowrap"
      title={parts.map(part => part.title ?? part.text).join(' ')}
    >
      {parts.map((part, index) => {
        if (part.kind === 'keyword') {
          return (
            <span key={index} className="inline-flex items-center gap-0.5">
              <GitBranch className="h-2.5 w-2.5 shrink-0 text-violet-600" aria-hidden />
              <span className="text-[8px] font-bold uppercase tracking-wide text-violet-800">{part.text}</span>
            </span>
          )
        }
        if (part.kind === 'operator') {
          return (
            <span
              key={index}
              className="shrink-0 rounded-full bg-violet-100 px-1 py-px text-[8px] font-semibold leading-none text-violet-800"
              aria-label={part.title}
            >
              {part.text}
            </span>
          )
        }
        return (
          <span
            key={index}
            className={`max-w-[5.5rem] truncate rounded px-1 py-px font-mono text-[9px] leading-tight ring-1 ${chipClass[part.kind]}`}
            title={part.title ?? part.text}
          >
            {part.text}
          </span>
        )
      })}
    </span>
  )
}
