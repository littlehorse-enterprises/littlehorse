import React, { type FC, useCallback } from 'react'
import { getSmoothStepPath, EdgeLabelRenderer, BaseEdge, type EdgeProps, Position } from 'reactflow'
import { CircleAlertIcon } from 'lucide-react'
import { useModal } from '../../hooks/useModal'
import { Edge as EdgeProto, VariableValue } from 'littlehorse-client/proto'
import { EdgeBranchLabel, EdgeConditionLabel } from './EdgeConditionLabel'

export type EdgeData = EdgeProto & {
  isElseEdge?: boolean
  conditionOnSourceNode?: boolean
  branchLabel?: 'true' | 'false'
  fade?: boolean
  nodeOutputValues?: Record<string, VariableValue>
}

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
  const [edgePath, labelX, labelY] = getSmoothStepPath({
    sourceX,
    sourceY,
    sourcePosition,
    targetX,
    targetY,
    targetPosition,
    borderRadius: 0,
  })

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
            {data?.branchLabel ? (
              <EdgeBranchLabel branch={data.branchLabel} fade={data.fade} />
            ) : (
              data?.edgeCondition && (
                <div
                  className="inline-flex w-max max-w-xs"
                  style={{ transform: 'scale(0.88)', transformOrigin: 'center' }}
                >
                  <EdgeConditionLabel edge={data} />
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
