'use client'
import { Check, Copy } from 'lucide-react'
import { FC, useState } from 'react'

interface CopyToClipboardProps {
    textToCopy: string
    className?: string
}

export const CopyToClipboard: FC<CopyToClipboardProps> = ({ textToCopy, className = '' }) => {
    const [copied, setCopied] = useState(false)

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(textToCopy)
            setCopied(true)
            setTimeout(() => setCopied(false), 2000) // Reset after 2 seconds
        } catch (err) {
            console.error('Failed to copy text: ', err)
        }
    }

    return (
        <button
            onClick={handleCopy}
            className={`ml-2 inline-flex items-center justify-center w-6 h-6 rounded hover:bg-gray-100 transition-colors ${className}`}
            title="Copy to clipboard"
        >
            {copied ? (
                <Check className="w-4 h-4 text-green-500" />
            ) : (
                <Copy className="w-4 h-4 text-gray-500 hover:text-gray-700" />
            )}
        </button>
    )
}
