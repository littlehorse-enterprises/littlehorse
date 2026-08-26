import React, { type FC, useCallback } from 'react'
import { getSmoothStepPath, EdgeLabelRenderer, BaseEdge, type EdgeProps, Position } from 'reactflow'
import { CircleAlertIcon } from 'lucide-react'
import { useModal } from '../../hooks/useModal'
import { Edge as EdgeProto } from 'littlehorse-client/proto'
import { EdgeConditionLabel } from './EdgeConditionLabel'
import { routeLabelPoint, routeToPath, type RoutePoint } from './elkRoute'

type EdgeData = EdgeProto & { isElseEdge?: boolean; elkRoute?: RoutePoint[] }

const CustomEdge: FC<EdgeProps<EdgeData>> = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition = Position.Bottom,
  targetPosition = Position.Top,
  data,
  style,
  ...rest
}) => {
  // Prefer the orthogonal route ELK computed for this edge — it respects the
  // configured edge-edge and edge-node clearances, which a path re-derived
  // from handle positions cannot. The smooth-step fallback only covers the
  // frames before the first layout pass has attached routes.
  const route = data?.elkRoute
  let edgePath: string
  let labelX: number
  let labelY: number
  if (route !== undefined && route.length >= 2) {
    edgePath = routeToPath(route)
    const labelPoint = routeLabelPoint(route)
    labelX = labelPoint.x
    labelY = labelPoint.y
  } else {
    ;[edgePath, labelX, labelY] = getSmoothStepPath({
      sourceX,
      sourceY,
      sourcePosition,
      targetX,
      targetY,
      targetPosition,
      borderRadius: 0,
    })
  }

  const { setModal, setShowModal } = useModal()
  const onClick = useCallback(() => {
    if (!data) return
    setModal({ type: 'edge', data: data as EdgeProto })
    setShowModal(true)
  }, [data, setModal, setShowModal])

  return (
    <>
      <BaseEdge id={id} path={edgePath} style={style} {...rest} />
      <EdgeLabelRenderer>
        <div
          style={{
            position: 'absolute',
            transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
            pointerEvents: 'all',
          }}
        >
          <div onClick={onClick} className="flex cursor-pointer flex-col items-center">
            {(data?.variableMutations?.length ?? 0) > 0 && <CircleAlertIcon size={16} className={`fill-gray-200`} />}
            {data?.edgeCondition?.oneofKind !== undefined ? (
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
