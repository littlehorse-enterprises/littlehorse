import { cn } from "@/components/utils";
import { LHStatus, TaskStatus } from "littlehorse-client/proto";

export function Status({ status }: { status: LHStatus | TaskStatus }) {
    const color = (statusColors as Record<string, string>)[status] ?? "bg-gray-500"

    return <div className={cn("text-xs w-full bg-status-running rounded-lg py-1 text-center border border-black", color)}>
        {status}
    </div>
}

const statusColors: Partial<Record<LHStatus | TaskStatus, string>> = {
    [LHStatus.RUNNING]: "bg-status-running",
    [LHStatus.COMPLETED]: "bg-status-success",
    [LHStatus.ERROR]: "bg-status-failed",
    [LHStatus.EXCEPTION]: "bg-status-exception",
    [LHStatus.HALTING]: "bg-status-halting",
    [TaskStatus.TASK_SCHEDULED]: "bg-gray-300",
    [TaskStatus.TASK_RUNNING]: "bg-status-running",
    [TaskStatus.TASK_SUCCESS]: "bg-status-success",
    [TaskStatus.TASK_FAILED]: "bg-status-failed",
    [TaskStatus.TASK_EXCEPTION]: "bg-status-exception",
    [TaskStatus.TASK_TIMEOUT]: "bg-status-halting",
    [TaskStatus.TASK_PENDING]: "bg-status-running",
} as const;

