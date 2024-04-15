import { FC, memo } from 'react'
import { BaseEdge, EdgeLabelRenderer, Position, SmoothStepEdgeProps, getSmoothStepPath } from 'reactflow'

const Default: FC<SmoothStepEdgeProps> = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  label,
  pathOptions,
  style,
  data,
  sourcePosition = Position.Bottom,
  targetPosition = Position.Top,
  ...rest
}) => {
  const [path, labelX, labelY] = getSmoothStepPath({
    sourceX,
    sourceY,
    sourcePosition,
    targetX,
    targetY,
    targetPosition,
    borderRadius: pathOptions?.borderRadius,
    offset: pathOptions?.offset,
  })

  return (
    <>
      <BaseEdge id={id} path={path} style={style} {...rest} />
      <EdgeLabelRenderer>
        <div
          className="rounded-md bg-gray-200 px-2 text-xs text-gray-600"
          style={{
            position: 'absolute',
            transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
            pointerEvents: 'all',
          }}
        >
          {label}
        </div>
      </EdgeLabelRenderer>
    </>
  )
}

export const DefaultEdge = memo(Default)
