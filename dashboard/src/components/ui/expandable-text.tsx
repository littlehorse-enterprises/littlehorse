"use client"

import { useState } from "react"
import { ChevronDown, ChevronUp, Maximize2 } from "lucide-react"

interface ExpandableTextProps {
  text?: string
  maxLength?: number
  isCode?: boolean
}

export default function ExpandableText({ text, maxLength = 50, isCode = false }: ExpandableTextProps) {
  const [isExpanded, setIsExpanded] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false)
  if (text === "") {
    return <span className="text-gray-500">N/A</span>
  }

  const shouldTruncate = text.length > maxLength
  const displayText = shouldTruncate && !isExpanded ? `${text.substring(0, maxLength)}...` : text

  return (
    <div className="relative">
      <div
        className={`${isCode ? "font-mono bg-gray-50 p-1 rounded" : ""
          } ${isExpanded ? "whitespace-pre-wrap break-words" : "truncate"} relative`}
      >
        {displayText}

        {/* Absolute positioned Full View button */}
        <button
          onClick={() => setIsModalOpen(true)}
          className="absolute right-1 text-blue-500 hover:text-blue-700 bg-white bg-opacity-70 rounded-full p-0.5 backdrop-blur-lg"
          title="Full View"
        >
          <Maximize2 className="h-3 w-3" />
        </button>
      </div>

      {shouldTruncate && (
        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="text-xs flex items-center text-blue-500 hover:text-blue-700 mt-1"
        >
          {isExpanded ? (
            <>
              <ChevronUp className="h-3 w-3 mr-1" /> Show Less
            </>
          ) : (
            <>
              <ChevronDown className="h-3 w-3 mr-1" /> Show More
            </>
          )}
        </button>
      )}

      {/* Modal for full view */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-lg max-w-3xl w-full max-h-[80vh] flex flex-col">
            <div className="flex justify-between items-center p-4 border-b">
              <h3 className="font-medium">Full Content</h3>
              <button onClick={() => setIsModalOpen(false)} className="text-gray-500 hover:text-gray-700">
                ✕
              </button>
            </div>
            <div className="p-4 overflow-auto flex-1">
              <pre className={`${isCode ? "font-mono text-xs" : "whitespace-pre-wrap"}`}>{text}</pre>
            </div>
            <div className="border-t p-4 flex justify-end">
              <button
                onClick={() => setIsModalOpen(false)}
                className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 text-sm"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
