'use client'
import { ReactNode } from "react"
import { DiagramDataGroupIndexer } from "./DiagramDataGroupIndexer";

export function DiagramDataGroup({ label, from, tab, children, }: { label: string; from?: string; tab?: string; children?: ReactNode }) {
    return <div className="relative flex flex-col justify-around min-h-32 min-w-36 h-full w-fit bg-white rounded-lg">
        <div className="absolute left-0 -top-5 w-fit px-3 py-1 bg-white rounded-lg font-semibold flex flex-nowrap gap-2">
            {label}
        </div>
        {from && (
            <div className="absolute left-0 -top-8 w-fit font-semibold text-gray-500 text-[8px]">
                ( From: {from} )
            </div>
        )}
        <div className="flex flex-col gap-1 p-2 z-10 ">
            {children}
        </div>
    </div>
}