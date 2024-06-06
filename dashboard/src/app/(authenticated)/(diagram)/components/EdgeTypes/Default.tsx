import { Edge as EdgeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { CircleAlertIcon } from 'lucide-react'
import { FC, memo, useCallback } from 'react'
import { BaseEdge, EdgeLabelRenderer, Position, SmoothStepEdgeProps, getSmoothStepPath } from 'reactflow'
import { useModal } from '../../hooks/useModal'

const Default: FC<SmoothStepEdgeProps<EdgeProto>> = ({
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
            transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px) `,
            pointerEvents: 'all',
          }}
        >
          {(data?.variableMutations.length ?? 0) > 0 && (
            <CircleAlertIcon size={16} className="absolute bottom-2 cursor-pointer" onClick={onClick} />
          )}
          <div className="rounded-md bg-red-500 px-2 text-center text-xs text-gray-600">{label}</div>
        </div>
      </EdgeLabelRenderer>
    </>
  )
}

export const DefaultEdge = memo(Default)
