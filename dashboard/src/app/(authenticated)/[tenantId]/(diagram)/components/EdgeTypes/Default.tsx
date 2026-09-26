import React, { type FC, useCallback } from 'react'
import { getSmoothStepPath, EdgeLabelRenderer, BaseEdge, type EdgeProps, Position } from 'reactflow'
import { cn } from '@/lib/utils'
import { CircleAlertIcon, GitBranch } from 'lucide-react'
import { useModal } from '../../hooks/useModal'
import { EdgeConditionLabel } from './EdgeConditionLabel'
import { pathReachesTarget, routeLabelPoint, routeToPath, snapEdgeRoute } from './elkRoute'
import { edgeLabelSize } from './edgeLabel'
import type { DiagramEdgeData } from './extractEdges'

const CustomEdge: FC<EdgeProps<DiagramEdgeData>> = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition = Position.Bottom,
  targetPosition = Position.Top,
  data,
  style,
  markerEnd,
  ...rest
}) => {
  const route = data?.route
  let edgePath: string
  let labelX: number
  let labelY: number
  let paths: { path: string; terminal: boolean }[]
  if (route) {
    const snapped = snapEdgeRoute(
      route,
      { x: sourceX, y: sourceY },
      { x: targetX, y: targetY },
      sourcePosition,
      targetPosition
    )
    edgePath = routeToPath(snapped.points)
    const labelPoint = snapped.labelPoint ?? routeLabelPoint(snapped.points)
    labelX = labelPoint.x
    labelY = labelPoint.y
    paths = snapped.paths.map(path => ({
      path: routeToPath(path),
      terminal: pathReachesTarget(path, snapped.points),
    }))
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
    paths = [{ path: edgePath, terminal: true }]
  }

  const size = edgeLabelSize(data)
  const hasMutation = (data?.variableMutations.length ?? 0) > 0
  const hasCondition = data?.edgeCondition.oneofKind !== undefined
  const hasChip = hasCondition || data?.isElseEdge
  const { setModal, setShowModal } = useModal()
  const onClick = useCallback(() => {
    if (!data) return
    setModal({ type: 'edge', data })
    setShowModal(true)
  }, [data, setModal, setShowModal])

  return (
    <>
      {paths.map(({ path, terminal }, index) => (
        <BaseEdge
          key={index}
          id={`${id}-${index}`}
          path={path}
          style={style}
          {...rest}
          interactionWidth={0}
          markerEnd={terminal ? markerEnd : undefined}
        />
      ))}
      {/* Keep each logical edge selectable even when another edge owns its trunk. */}
      <path d={edgePath} className="react-flow__edge-interaction" fill="none" strokeOpacity={0} strokeWidth={20} />
      {size && (
        <EdgeLabelRenderer>
          <div
            data-testid="edge-label"
            data-edge-id={id}
            style={{
              position: 'absolute',
              width: size.w,
              height: size.h,
              transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
              pointerEvents: 'all',
            }}
          >
            <div onClick={onClick} className="flex h-full cursor-pointer flex-col items-center">
              {hasMutation && <CircleAlertIcon size={16} className="shrink-0 fill-gray-200" />}
              {hasChip && (
                <div className="relative w-full flex-1">
                  <div
                    className={cn(
                      'absolute left-1/2 top-1/2 flex items-center justify-center whitespace-nowrap rounded-md border border-violet-200 bg-white px-1.5 py-1',
                      data?.fade && 'opacity-25'
                    )}
                    style={{ transform: 'translate(-50%, -50%) scale(0.75)' }}
                  >
                    {hasCondition ? (
                      <EdgeConditionLabel edge={data} />
                    ) : (
                      <span className="inline-flex items-center gap-0.5">
                        <GitBranch className="h-2.5 w-2.5 shrink-0 text-violet-600" aria-hidden />
                        <span className="text-[8px] font-bold uppercase tracking-wide text-violet-800">Else</span>
                      </span>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </EdgeLabelRenderer>
      )}
    </>
  )
}

export default CustomEdge
