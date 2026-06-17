import { FC } from 'react'
import { Handle, Position } from 'reactflow'

type DiagramNodeHandlesProps = {
  sourceCount: number
  targetCount: number
}

const handleOffset = (index: number, count: number): number | undefined => {
  if (count <= 1) return undefined
  return ((index + 1) / (count + 1)) * 100
}

export const DiagramNodeHandles: FC<DiagramNodeHandlesProps> = ({ sourceCount, targetCount }) => (
  <>
    {Array.from({ length: targetCount }, (_, index) => (
      <Handle
        key={`target-${index}`}
        type="target"
        position={Position.Left}
        id={`target-${index}`}
        className="bg-transparent"
        style={
          targetCount > 1
            ? { top: `${handleOffset(index, targetCount)}%` }
            : undefined
        }
      />
    ))}
    {Array.from({ length: sourceCount }, (_, index) => (
      <Handle
        key={`source-${index}`}
        type="source"
        position={Position.Right}
        id={`source-${index}`}
        className="bg-transparent"
        style={
          sourceCount > 1
            ? { top: `${handleOffset(index, sourceCount)}%` }
            : undefined
        }
      />
    ))}
  </>
)
