import { cn } from "@/components/utils";
import { LHStatus } from "littlehorse-client/proto";

export function Status({ status }: { status: LHStatus }) {
    const color = statusColors[status] ?? "bg-gray-500"

    return <div className={cn("text-xs w-full max-w-48 bg-gray-300 rounded-lg py-1 text-center border border-black", color)}>
        {status}
    </div>
}

const statusColors: Partial<Record<LHStatus, string>> = {
    [LHStatus.RUNNING]: "bg-blue-500",
    [LHStatus.COMPLETED]: "bg-green-300",
    [LHStatus.ERROR]: "bg-red-300",
    [LHStatus.EXCEPTION]: "bg-orange-500",
    [LHStatus.HALTING]: "bg-yellow-500",
};

