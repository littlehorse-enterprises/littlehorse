import { OverflowText } from "@/app/(authenticated)/[tenantId]/components/OverflowText";
import { cn } from "@/components/utils";
export function ErrorMessage({ errorMessage }: { errorMessage: string | undefined }) {
    return <div className={cn("text-xs max-w-48 w-full bg-gray-300 rounded-lg py-1 text-center border border-black", { "text-red-500 bg-red-300": !!errorMessage })}>
        <OverflowText variant="error" className="py-0 px-2" text={errorMessage ?? "NO ERROR MESSAGE"} />
    </div>
}