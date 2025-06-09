import { memo } from "react"
import { Handle, Position } from "@xyflow/react"
import {
    NodeType,
    getBorderColor,
    getNodeIcon,
    getStatusBgColor,
    getStatusIcon
} from "@/utils/ui/node-utils"

export type NodeData = {
    label: string
    type: NodeType
    status?: 'COMPLETED' | 'ERROR' | 'RUNNING'
    nodeName?: string
}

interface NodeProps {
    data: NodeData;
    selected?: boolean;
}

function Node({ data, selected }: NodeProps) {
    return (
        <div className="flex flex-col items-center">
            <div className="text-[10px] font-medium mb-1 uppercase text-gray-400">
                {data.type}
            </div>

            <div className="relative">
                {selected && (
                    <div className="absolute inset-0 border-2 border-double border-blue-500 rounded-md -m-[5.25px]"></div>
                )}

                <div
                    className={`flex items-center justify-center bg-white border ${getBorderColor(data.type)} rounded-md shadow-sm w-12 h-12 ${selected ? "shadow-md" : ""
                        }`}
                >
                    {data.status && (
                        <div className="absolute top-0 right-0 -mr-1 -mt-1">
                            <div className={`flex items-center justify-center p-0.5 rounded-full ${getStatusBgColor(data.status)} border border-white shadow-sm`}>
                                {getStatusIcon(data.status)}
                            </div>
                        </div>
                    )}

                    <div>{getNodeIcon(data.type)}</div>
                </div>

                <Handle type="target" position={Position.Left} className="!bg-gray-400" />
                <Handle type="source" position={Position.Right} className="!bg-gray-400" />
            </div>

            {(data.type === 'TASK' || data.type === 'EXTERNAL_EVENT') && (
                <div className="absolute top-full mt-1 left-1/2 transform -translate-x-1/2 text-xs font-medium text-center text-gray-700 whitespace-nowrap">
                    {data.label.match(/^[^-]*-(.+)-[^-]*$/)?.[1] || data.label}
                </div>
            )}
        </div>
    )
}

export default memo(Node) 