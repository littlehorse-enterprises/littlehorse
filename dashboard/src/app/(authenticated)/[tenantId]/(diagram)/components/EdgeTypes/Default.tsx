import React, { type FC, useCallback } from 'react';
import {
  getSmoothStepPath,
  EdgeLabelRenderer,
  BaseEdge,
  type EdgeProps,
  Position,
} from 'reactflow'
import { CircleAlertIcon } from 'lucide-react'
import { useModal } from '../../hooks/useModal'
import { Edge as EdgeProto } from 'littlehorse-client/proto'

const CustomEdge: FC<EdgeProps<EdgeProto>> = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition = Position.Bottom,
  targetPosition = Position.Top,
  label,
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
    borderRadius: 20,
  });

  const { setModal, setShowModal } = useModal()
  const onClick = useCallback(() => {
    if (!data) return
    setModal({ type: 'edge', data })
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
            {(data?.variableMutations?.length ?? 0) > 0 && (
              <CircleAlertIcon size={16} className={`fill-gray-200`} />
            )}
            {label && (
              <div className="rounded-md bg-gray-200 px-2 text-center text-xs text-gray-600">
                {label}
              </div>
            )}
          </div>
        </div>
      </EdgeLabelRenderer>
    </>
  );
};

export default CustomEdge;