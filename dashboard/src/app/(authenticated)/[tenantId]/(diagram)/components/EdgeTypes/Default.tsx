import React, { type FC, useCallback, useMemo } from 'react'
import { getSmoothStepPath, EdgeLabelRenderer, BaseEdge, type EdgeProps, Position } from 'reactflow'
import { CircleAlertIcon } from 'lucide-react'
import { useModal } from '../../hooks/useModal'
import { Edge as EdgeProto } from 'littlehorse-client/proto'
import { EdgeConditionLabel } from './EdgeConditionLabel'

type EdgeData = EdgeProto & {
  isElseEdge?: boolean
  layoutPath?: string
  layoutLabelPoint?: { x: number; y: number }
}

const CustomEdge: FC<EdgeProps<EdgeData>> = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition = Position.Right,
  targetPosition = Position.Left,
  data,
  style,
  ...rest
}) => {
  const fallback = useMemo(
    () =>
      getSmoothStepPath({
        sourceX,
        sourceY,
        sourcePosition,
        targetX,
        targetY,
        targetPosition,
        borderRadius: 0,
      }),
    [sourcePosition, sourceX, sourceY, targetPosition, targetX, targetY]
  )

  const path = data?.layoutPath || fallback[0]
  const labelPoint = data?.layoutLabelPoint ?? { x: fallback[1], y: fallback[2] }

  const { setModal, setShowModal } = useModal()
  const onClick = useCallback(() => {
    if (!data) return
    setModal({ type: 'edge', data: data as EdgeProto })
    setShowModal(true)
  }, [data, setModal, setShowModal])

  return (
    <>
      <BaseEdge id={id} path={path} style={style} {...rest} />
      <EdgeLabelRenderer>
        <div
          style={{
            position: 'absolute',
            transform: `translate(-50%, -50%) translate(${labelPoint.x}px,${labelPoint.y}px)`,
            pointerEvents: 'all',
          }}
        >
          <div onClick={onClick} className="flex cursor-pointer flex-col items-center">
            {(data?.variableMutations?.length ?? 0) > 0 && <CircleAlertIcon size={16} className={`fill-gray-200`} />}
            {data?.edgeCondition ? (
              <div
                className="flex items-center justify-center rounded-md bg-gray-200 px-2 py-1 text-gray-600"
                style={{ transform: 'scale(0.75)', transformOrigin: 'center' }}
              >
                <EdgeConditionLabel edge={data} />
              </div>
            ) : (
              data?.isElseEdge && (
                <div
                  className="flex items-center justify-center rounded-md bg-gray-200 px-2 py-1 text-gray-600"
                  style={{ transform: 'scale(0.75)', transformOrigin: 'center' }}
                >
                  <span className="text-[10px] text-gray-600">else</span>
                </div>
              )
            )}
          </div>
        </div>
      </EdgeLabelRenderer>
    </>
  )
}

export default CustomEdge
