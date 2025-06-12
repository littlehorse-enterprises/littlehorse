'use client'

import { useState } from 'react'
import { ChevronDown, ChevronUp, Maximize2 } from 'lucide-react'

interface ExpandableTextProps {
  text?: string
  maxLength?: number
  isCode?: boolean
}

export default function ExpandableText({ text, maxLength = 50, isCode = false }: ExpandableTextProps) {
  const [isExpanded, setIsExpanded] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false)
  if (text === '') {
    return <span className="text-gray-500">N/A</span>
  }

  const shouldTruncate = text.length > maxLength
  const displayText = shouldTruncate && !isExpanded ? `${text.substring(0, maxLength)}...` : text

  return (
    <div className="relative">
      <div
        className={`${
          isCode ? 'rounded bg-gray-50 p-1 font-mono' : ''
        } ${isExpanded ? 'break-words whitespace-pre-wrap' : 'truncate'} relative`}
      >
        {displayText}

        {/* Absolute positioned Full View button */}
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-opacity-70 absolute right-1 rounded-full bg-white p-0.5 text-blue-500 backdrop-blur-lg hover:text-blue-700"
          title="Full View"
        >
          <Maximize2 className="h-3 w-3" />
        </button>
      </div>

      {shouldTruncate && (
        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="mt-1 flex items-center text-xs text-blue-500 hover:text-blue-700"
        >
          {isExpanded ? (
            <>
              <ChevronUp className="mr-1 h-3 w-3" /> Show Less
            </>
          ) : (
            <>
              <ChevronDown className="mr-1 h-3 w-3" /> Show More
            </>
          )}
        </button>
      )}

      {/* Modal for full view */}
      {isModalOpen && (
        <div className="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black p-4">
          <div className="flex max-h-[80vh] w-full max-w-3xl flex-col rounded-lg bg-white shadow-lg">
            <div className="flex items-center justify-between border-b p-4">
              <h3 className="font-medium">Full Content</h3>
              <button onClick={() => setIsModalOpen(false)} className="text-gray-500 hover:text-gray-700">
                ✕
              </button>
            </div>
            <div className="flex-1 overflow-auto p-4">
              <pre className={`${isCode ? 'font-mono text-xs' : 'whitespace-pre-wrap'}`}>{text}</pre>
            </div>
            <div className="flex justify-end border-t p-4">
              <button
                onClick={() => setIsModalOpen(false)}
                className="rounded bg-gray-200 px-4 py-2 text-sm hover:bg-gray-300"
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
