import { TagIcon } from 'lucide-react'

export default function VersionTag({ label }: { label: string }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-100 px-2.5 py-1 font-mono text-xs font-medium text-blue-700">
      <TagIcon className="h-3 w-3 fill-none stroke-blue-700 stroke-1" />
      {label}
    </span>
  )
}
