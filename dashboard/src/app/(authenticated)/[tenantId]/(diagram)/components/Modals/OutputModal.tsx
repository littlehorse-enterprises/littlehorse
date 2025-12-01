import { FC, useState } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Check, Copy } from 'lucide-react'

export const OutputModal: FC<Modal<{ message: string; label: string }>> = ({ data }) => {
  const { message, label } = data
  const { showModal, setShowModal } = useModal()
  const [copied, setCopied] = useState(false)

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(message)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000) // Reset after 2 seconds
    } catch (err) {
      console.error('Failed to copy text: ', err)
    }
  }
  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className="min-w-[900px] ">
        <DialogHeader>
          <DialogTitle>{label}</DialogTitle>
        </DialogHeader>
        <pre className="mb-2 overflow-auto rounded border-b border-slate-200 bg-slate-50 p-2 pb-2 font-mono">
          <div className="absolute right-8">
            <button
              onClick={handleCopy}
              className={`ml-2 inline-flex h-6 w-6 cursor-pointer items-center justify-center rounded bg-gray-100  transition-colors hover:bg-gray-200`}
            >
              {copied ? (
                <Check className="h-4 w-4 text-green-500" />
              ) : (
                <Copy className="h-4 w-4 text-gray-500 hover:text-gray-700" />
              )}
            </button>
          </div>
          <code>{message}</code>
        </pre>
      </DialogContent>
    </Dialog>
  )
}
