import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { getVariable, getVariableValue } from '@/app/utils/variables'
import { Button } from '@/components/ui/button'

import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/dialog'
import { VariableAssignment, VarNameAndVal } from 'littlehorse-client/proto'
import { EyeIcon } from 'lucide-react'

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
          {variables.map(({ value: variableValue, varName }) => {
            if (!variableValue) return null
            const variableType = variableValue.value?.$case
            return (
              <div key={varName} className="flex w-full items-center gap-1">
                {variableType && (
                  <div className="flex h-full items-center justify-center rounded-lg border border-black bg-yellow-100 p-1 text-xs font-semibold">
                    {variableType}
                  </div>
                )}
                <p className="rounded-lg border border-purple-500 p-2 text-center text-xs font-bold text-purple-500">
                  {varName}
                </p>
                <p> = </p>
                <div className={'h-8 min-h-5 max-w-96 text-nowrap rounded-lg border px-2 text-center'}>
                  <OverflowText text={String(getVariableValue(variableValue))} className="text-xs" />
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
              <div key={JSON.stringify(variable)} className="flex w-full items-center gap-1">
                <p className="rounded-lg border border-purple-500 p-2 text-center text-xs font-bold text-purple-500">
                  arg{i}
                </p>
                <p> = </p>
                <div
                  className={
                    'flex h-8 min-h-5 max-w-96 items-center justify-center text-nowrap rounded-lg border px-2 text-center'
                  }
                >
                  {getVariable(variable)}
                </div>
              </div>
            )
          })}
        </div>
      </DialogContent>
    </Dialog>
  )
}
