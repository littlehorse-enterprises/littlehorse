import { getSmartEdge } from '@tisoap/react-flow-smart-edge'
import { BezierEdge, useNodes } from 'reactflow'


export function CustomSmartEdgeType(props) {
    const NODE_PADDING_THAT_SHOW_EDGES_IN_A_REASONABLE_POSITION_IN_THE_SCREEN = 60

    const {
        sourcePosition,
        targetPosition,
        sourceX,
        sourceY,
        targetX,
        targetY,
        style,
        markerStart,
        markerEnd,
        label
    } = props

    const nodes = useNodes()
    const getSmartEdgeResponse = getSmartEdge({
        sourcePosition,
        targetPosition,
        sourceX,
        sourceY,
        targetX,
        targetY,
        nodes,
        options: {
            nodePadding: NODE_PADDING_THAT_SHOW_EDGES_IN_A_REASONABLE_POSITION_IN_THE_SCREEN,
            gridRatio: 10
        }
    })

    // If the value returned is null, it means "getSmartEdge" was unable to find
    // a valid path, and you should do something else instead
    if (getSmartEdgeResponse === null) {
        return <BezierEdge {...props} />
    }

    const { edgeCenterX, edgeCenterY, svgPathString } = getSmartEdgeResponse

    return (
        <>
            <path
                className='react-flow__edge-path'
                d={svgPathString}
                markerEnd={markerEnd}
                markerStart={markerStart}
                style={style}
            />
            <foreignObject
                height={20}
                requiredExtensions='http://www.w3.org/1999/xhtml'
                width={500}
                x={(edgeCenterX - 100 / 2) - 50}
                y={edgeCenterY - 20 / 2}
            >
                {label}
            </foreignObject>
        </>
    )
}
