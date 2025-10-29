import { Copy, Expand } from 'lucide-react'

export const NodeErrorMessage = ({ message = '' }: { message?: string }) => {
  return (
    <div className=" mt-1 h-full w-full p-1">
      <div className="flex justify-between text-sm font-bold ">
        <span> Error Message</span>
        <div className="flex gap-4">
          <Copy size={15} />
          <Expand size={15} />
        </div>
      </div>
      <div className="px-1 py-2 shadow-none  text-slate-400">
        {message }
      </div>
    </div>
  )
}

