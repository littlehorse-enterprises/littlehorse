import { TagIcon } from "lucide-react";

export default function VersionTag({ label }: { label: string }) {
    return (
        <div className="flex items-center gap-2 rounded bg-blue-200 px-2 font-mono text-sm text-gray-500">
            <TagIcon className="h-4 w-4 fill-none stroke-gray-500 stroke-1" />{label}
        </div>
    )
}