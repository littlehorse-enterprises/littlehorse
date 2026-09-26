import { Edge as EdgeProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { conditionLabelParts } from './edgeLabel'

const variableBadgeClass = 'rounded px-1 py-0.5 text-[10px] font-mono bg-gray-100 text-fuchsia-500'

export const EdgeConditionLabel: FC<{ edge: EdgeProto }> = ({ edge }) => {
  const parts = conditionLabelParts(edge)
  if (parts.length === 0) return null
  return (
    <span className="inline-flex items-center gap-1">
      {parts.map((part, index) => (
        <span key={index} className={part.badge ? variableBadgeClass : 'text-[10px] text-gray-500'}>
          {part.text}
        </span>
      ))}
    </span>
  )
}
