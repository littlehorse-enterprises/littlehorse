import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { getVariableValue } from '@/app/utils/variables'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { VariableAssignment, VarNameAndVal } from 'littlehorse-client/proto'
import { Dialog, DialogTrigger, DialogContent, DialogClose } from '@/components/ui/dialog'
import { EyeIcon } from 'lucide-react'
import { VARIABLE_TYPES } from '@/app/constants'

export function ViewVariables({ variables }: { variables: VarNameAndVal[] }) {
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button
          variant="outline"
          className="m-0 flex h-fit w-full items-center justify-center gap-2 p-0 px-0 py-2 text-xs"
        >
          <EyeIcon className="h-4 w-4" /> View Variables
        </Button>
      </DialogTrigger>
      <DialogContent>
        <div className="flex max-h-[300px] w-fit flex-col gap-3 overflow-y-auto">
          {variables.map(variable => {
            const variableType =
              VARIABLE_TYPES[Object.keys(variable.value!)[0]?.toUpperCase() as keyof typeof VARIABLE_TYPES]
            return (
              <div key={variable.varName} className="flex w-full items-center gap-1">
                {variableType && (
                  <div className="flex h-full items-center justify-center rounded-lg border border-black bg-yellow-100 p-1 text-xs font-semibold">
                    {variableType}
                  </div>
                )}
                <p className="rounded-lg border border-purple-500 p-2 text-center text-xs font-bold text-purple-500">
                  {variable.varName}
                </p>
                <p> = </p>
                <div className={'h-8 min-h-5 max-w-96 text-nowrap rounded-lg border px-2 text-center'}>
                  <OverflowText text={String(getVariableValue(variable.value))} className="text-xs" />
                </div>
              </div>
            )
          })}
        </div>
      </DialogContent>
    </Dialog>
  )
}

export function ViewVariableAssignments({ variables }: { variables: VariableAssignment[] }) {
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button
          variant="outline"
          className="m-0 flex h-fit w-full items-center justify-center gap-2 p-0 px-0 py-2 text-xs"
        >
          <EyeIcon className="h-4 w-4" /> View Variables
        </Button>
      </DialogTrigger>
      <DialogContent>
        <div className="flex max-h-[300px] w-fit flex-col gap-3 overflow-y-auto">
          {variables.map((variable, i) => {
            return (
              <div key={variable.variableName} className="flex w-full items-center gap-1">
                <p className="rounded-lg border border-purple-500 p-2 text-center text-xs font-bold text-purple-500">
                  arg{i}
                </p>
                <p> = </p>
                <div
                  className={
                    'flex h-8 min-h-5 max-w-96 items-center justify-center text-nowrap rounded-lg border px-2 text-center'
                  }
                >
                  {`{${variable.variableName}}`}
                </div>
              </div>
            )
          })}
        </div>
      </DialogContent>
    </Dialog>
  )
}
