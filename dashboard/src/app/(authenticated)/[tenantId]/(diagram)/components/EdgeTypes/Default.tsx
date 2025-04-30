import { Edge as EdgeProto } from 'littlehorse-client/proto'
import { CircleAlertIcon } from 'lucide-react'
import { FC, memo, useCallback } from 'react'
import { BaseEdge, EdgeLabelRenderer, Position, SmoothStepEdgeProps, getSmoothStepPath } from '@xyflow/react'
import { useModal } from '../../hooks/useModal'

type DefaultEdgeProps = SmoothStepEdgeProps & {
  data: EdgeProto;
  animated?: boolean;
  selectable?: boolean;
  deletable?: boolean;
  sourceHandleId?: string;
  targetHandleId?: string;
}

const Default: FC<DefaultEdgeProps> = ({
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
  animated,
  selectable,
  deletable,
  sourceHandleId,
  targetHandleId,
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
  const { setModal, setShowModal } = useModal()
  const onClick = useCallback(() => {
    if (!data) return
    setModal({ type: 'edge', data })
    setShowModal(true)
  }, [data, setModal, setShowModal])

  return (
    <>
      <BaseEdge
        id={id}
        path={path}
        style={style}
        {...(animated ? { animated: animated.toString() } : {})}
        {...(selectable ? { selectable: selectable.toString() } : {})}
        {...(deletable ? { deletable: deletable.toString() } : {})}
        {...rest}
      />
      <EdgeLabelRenderer>
        <div
          style={{
            position: 'absolute',
            transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px) `,
            pointerEvents: 'all',
          }}
        >
          <div onClick={onClick} className="flex cursor-pointer flex-col items-center">
            {(data?.variableMutations?.length ?? 0) > 0 && (
              <CircleAlertIcon size={16} className={`fill-gray-200 ${label ? 'mb-1' : 'mb-6'}`} />
            )}
            <div className="rounded-md bg-gray-200 px-2 text-center text-xs text-gray-600">{label}</div>
          </div>
        </div>
      </EdgeLabelRenderer>
    </>
  )
}

export const DefaultEdge = memo(Default)
