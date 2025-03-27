'use client'
import { ReactNode } from "react";

export function DiagramDataGroup({ label, from, children, index, indexes }: { label: string; index?: number; indexes?: number; from?: string; children?: ReactNode }) {
    return <div className="relative flex flex-col justify-around min-w-36 h-fit w-fit bg-white rounded-lg">
        <div className="absolute left-0 -top-5 w-fit px-3 py-1 bg-white rounded-lg font-semibold flex flex-nowrap gap-2">
            {label} {index !== undefined && indexes !== undefined && indexes > 1 ? `#${indexes - 1 - index}` : ''}
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