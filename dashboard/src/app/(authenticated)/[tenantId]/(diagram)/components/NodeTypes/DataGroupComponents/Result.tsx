import { OverflowText } from "@/app/(authenticated)/[tenantId]/components/OverflowText";
import { getVariableValue } from "@/app/utils/variables";
import { cn } from "@/components/utils";
import { LHTaskError, LHTaskException, VariableValue } from "littlehorse-client/proto";
import { FC } from "react";

export function Result({ resultString, resultMessage, variant }: { resultString: string, resultMessage: string, variant?: "error" }) {
    return <div className="flex gap-2 w-full rounded-lg border border-black p-1">
        <div className={cn("bg-gray-300 rounded-lg py-1 text-center flex-1 text-xs flex items-center justify-center", {
            "bg-red-300": variant === "error",
        })}>
            {resultString}
        </div>
        <div className={"bg-gray-300 rounded-lg text-center border border-black flex-1 max-w-32 text-nowrap px-1"} >
            <OverflowText text={resultMessage} className="text-xs" variant={variant} />
        </div>
    </div>
}

