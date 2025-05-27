import { memo } from "react"
import { Handle, Position, type NodeProps } from "reactflow"

function DecisionNode({ data, selected }: NodeProps) {
  return (
    <div
      className={`flex h-16 w-16 rotate-45 items-center justify-center rounded-md border ${selected ? "border-blue-500 shadow-md" : "border-yellow-500"} bg-yellow-100 shadow-sm`}
    >
      <div className="-rotate-45 text-center text-sm font-medium text-yellow-700">{data.label}</div>
      <Handle type="target" position={Position.Top} className="!bg-yellow-500" />
      <Handle type="source" position={Position.Left} className="!bg-yellow-500" />
      <Handle type="source" position={Position.Right} className="!bg-yellow-500" />
    </div>
  )
}

export default memo(DecisionNode)
