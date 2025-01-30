import { ReactNode } from "react"

export function DiagramDataGroup({ label, tab, children }: { label: string; tab?: string, children?: ReactNode }) {
    return <div className="relative flex flex-col min-h-32 min-w-36 h-fit w-fit bg-white rounded-lg">
        <div className="absolute left-0 -top-5 w-fit px-3 py-1 bg-white rounded-lg font-semibold">
            {label}
        </div>
        <div className="flex flex-col gap-1 p-2 z-10">
            {children}
        </div>
    </div>
}