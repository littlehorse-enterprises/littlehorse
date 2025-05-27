import { memo } from "react"
import { Handle, Position, type NodeProps } from "reactflow"
import { CheckCircle, AlertCircle, Loader2 } from "lucide-react"
import { NodeRun } from "littlehorse-client/proto"
import { getNodeType, getIconColor, getBorderColor, getNodeIcon } from "./node-utils"

function TaskNode({ data, selected }: NodeProps<NodeRun>) {
  console.log("data", data)

  const getStatusIcon = () => {
    switch (data.status) {
      case "COMPLETED":
        return <CheckCircle className="h-4 w-4 text-green-500" />
      case "ERROR":
        return <AlertCircle className="h-4 w-4 text-red-500" />
      case "RUNNING":
        return <Loader2 className="h-4 w-4 text-blue-500 animate-spin" />
      default:
        return null
    }
  }

  const nodeType = getNodeType(data.nodeName);

  // Determine status background color
  const getStatusBgColor = () => {
    switch (data.status) {
      case "COMPLETED":
        return "bg-green-100"
      case "ERROR":
        return "bg-red-100"
      case "RUNNING":
        return "bg-blue-100"
      default:
        return "bg-gray-100"
    }
  }

  return (
    <div className="flex flex-col items-center">
      {/* Node type label above the box - muted gray and smaller */}
      <div className="text-[10px] font-medium mb-1 uppercase text-gray-400">
        {nodeType}
      </div>

      {/* Square node box with centered icon */}
      <div className="relative">
        {/* Selection border - only visible when selected */}
        {selected && (
          <div className="absolute inset-0 border-2 border-double border-blue-500 rounded-md -m-[5.25px]"></div>
        )}

        {/* Actual node box */}
        <div
          className={`flex items-center justify-center bg-white border ${getBorderColor(nodeType)} rounded-md shadow-sm w-12 h-12 ${selected ? "shadow-md" : ""
            }`}
        >
          {/* Status icon in top right with rounded background */}
          <div className="absolute top-0 right-0 -mr-1 -mt-1">
            <div className={`flex items-center justify-center p-0.5 rounded-full ${getStatusBgColor()} border border-white shadow-sm`}>
              {getStatusIcon()}
            </div>
          </div>

          {/* Main icon centered with color based on node type */}
          <div>{getNodeIcon(nodeType)}</div>
        </div>

        {/* Connection handles */}
        <Handle type="target" position={Position.Left} className="!bg-gray-400" />
        <Handle type="source" position={Position.Right} className="!bg-gray-400" />
      </div>

      {/* Node name below the box (only for TASK type) */}
      {(nodeType === "TASK" || nodeType === "EXTERNAL_EVENT") && (
        <div className="mt-1 text-xs text-center text-gray-700 max-w-24 truncate">
          {RegExp(/^[^-]+-(.+)-[^-]+$/).exec(data.nodeName)?.[1]}
        </div>
      )}
    </div>
  )
}

export default memo(TaskNode)
