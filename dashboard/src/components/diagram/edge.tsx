import {
    getSmoothStepPath,
    EdgeLabelRenderer,
    BaseEdge,
    Position,
    EdgeProps,
} from '@xyflow/react'

export function CustomEdgeComponent({
    id,
    sourceX,
    sourceY,
    targetX,
    targetY,
    sourcePosition = Position.Bottom,
    targetPosition = Position.Top,
    label,
    style,
}: EdgeProps) {
    const [edgePath, labelX, labelY] = getSmoothStepPath({
        sourceX,
        sourceY,
        sourcePosition,
        targetX,
        targetY,
        targetPosition,
        borderRadius: 0,
    });

    return (
        <>
            <BaseEdge id={id} path={edgePath} style={style} />
            <EdgeLabelRenderer>
                <div
                    style={{
                        position: 'absolute',
                        transform: `translate(${labelX}px,${labelY}px) translate(-50%, -50%)`,
                        pointerEvents: 'all',
                    }}
                >
                    {label && (
                        <div className="rounded-md bg-white border border-gray-200 px-2 py-1 text-center text-xs font-medium text-gray-700 shadow-sm">
                            {label}
                        </div>
                    )}
                </div>
            </EdgeLabelRenderer>
        </>
    );
};