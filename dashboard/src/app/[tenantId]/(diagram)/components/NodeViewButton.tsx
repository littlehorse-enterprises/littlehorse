import { EyeIcon } from 'lucide-react'

export const NodeViewButton = ({ text, callback }: { text: string; callback: () => void }) => (
  <div className="mt-2 flex justify-center">
    <button className="flex items-center gap-1 p-1 text-blue-500 hover:bg-gray-200" onClick={callback}>
      <EyeIcon className="h-4 w-4" />
      {text}
    </button>
  </div>
)
