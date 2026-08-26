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
    // ELK routed against deterministic node footprints (nodeDimensions.ts),
    // which over-reserve width for boxed nodes; the rendered node can be
    // narrower, leaving a visible gap between its border and the route's
    // endpoint. Snap the endpoints to the REAL handle positions reactflow
    // measured — a short straight stub bridges estimate and reality exactly.
    const snapped = [...route]
    const first = snapped[0]
    const last = snapped[snapped.length - 1]
    // Keep stubs axis-aligned: extend along the route's own first/last lane
    // rather than jogging to the handle's exact center, so the orthogonal
    // look survives the snap.
    if (Math.abs(first.x - sourceX) > 1 && Math.abs(first.y - sourceY) < 8) {
      snapped.unshift({ x: sourceX, y: first.y })
    } else if (Math.abs(first.x - sourceX) > 1 || Math.abs(first.y - sourceY) > 1) {
      snapped.unshift({ x: sourceX, y: sourceY })
    }
    if (Math.abs(last.x - targetX) > 1 && Math.abs(last.y - targetY) < 8) {
      snapped.push({ x: targetX, y: last.y })
    } else if (Math.abs(last.x - targetX) > 1 || Math.abs(last.y - targetY) > 1) {
      snapped.push({ x: targetX, y: targetY })
    }
    edgePath = routeToPath(snapped)
    const labelPoint = routeLabelPoint(snapped)
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
