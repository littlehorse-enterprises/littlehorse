"use client"

import ExpandableText from "./expandable-text"

interface VariableDisplayProps {
  name: string
  type: string
  value: unknown
}

export default function VariableDisplay({ name, type, value }: VariableDisplayProps) {
  // Get color based on type
  const getTypeColor = () => {
    switch (type.toUpperCase()) {
      case "STRING":
        return "text-green-600"
      case "NUMBER":
      case "INT":
      case "INTEGER":
      case "DOUBLE":
      case "FLOAT":
        return "text-blue-600"
      case "BOOLEAN":
        return "text-purple-600"
      case "OBJECT":
      case "ARRAY":
        return "text-orange-600"
      default:
        return "text-gray-600"
    }
  }

  // Format value for display
  const formatValue = () => {
    if (value === null || value === undefined) {
      return "null"
    }

    if (typeof value === "string") {
      return `"${value}"`
    }

    if (typeof value === "object") {
      return JSON.stringify(value, null, 2)
    }

    return String(value)
  }

  return (
    <div className="font-mono text-xs mb-1">
      <span className={`${getTypeColor()} font-bold`}>{type}</span> <span className="text-gray-800">{name}</span>{" "}
      <span className="text-gray-500">=</span> <ExpandableText text={formatValue()} isCode={true} />
    </div>
  )
}
