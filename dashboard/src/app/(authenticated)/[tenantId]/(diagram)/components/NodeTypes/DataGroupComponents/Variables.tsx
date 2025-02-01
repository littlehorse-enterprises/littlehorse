import { OverflowText } from "@/app/(authenticated)/[tenantId]/components/OverflowText";
import { getVariableValue } from "@/app/utils/variables";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { VarNameAndVal } from "littlehorse-client/proto";
import {
    Dialog,
    DialogTrigger,
    DialogContent,
    DialogClose
} from '@/components/ui/dialog';
import { EyeIcon } from "lucide-react";

export function ViewVariables({ variables }: { variables: VarNameAndVal[] }) {
    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button variant="outline" className="flex items-center justify-center gap-2 text-xs m-0 p-0 px-0 py-2 h-fit w-full"><EyeIcon className="w-4 h-4" /> View Variables</Button>
            </DialogTrigger>
            <DialogContent>
                <div className="flex flex-col gap-3 w-fit max-h-[300px] overflow-y-auto">
                    {variables.map((variable) => (
                        <div key={variable.varName} className="w-full flex gap-1 items-center">
                            <p className="text-xs font-bold text-purple-500 border text-center border-purple-500 rounded-lg p-2">{variable.varName}</p>
                            <p> = </p>
                            <div className={"px-2 border h-8 rounded-lg text-center max-w-96 text-nowrap min-h-5"} >
                                <OverflowText text={String(getVariableValue(variable.value))} className="text-xs" />
                            </div>
                        </div>
                    ))}
                </div>
            </DialogContent>
        </Dialog >
    );
}