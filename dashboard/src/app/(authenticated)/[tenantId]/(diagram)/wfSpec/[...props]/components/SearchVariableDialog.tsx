import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { WithBookmark } from '@/types'
import { useInfiniteQuery } from '@tanstack/react-query'
import { VariableDef, VariableValue, WfSpec } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, Fragment, useState } from 'react'
import { useDebounce } from 'use-debounce'
import { searchVariables } from '../actions/searchVariables'
import { getVariableDefType } from '@/app/utils/variables'

type Props = {
  spec: WfSpec
}

const LIMIT = 10

export const SearchVariableDialog: FC<Props> = ({ spec }) => {
  const variables = Object.keys(spec.threadSpecs)
    .flatMap(threadSpec =>
      spec.threadSpecs[threadSpec].variableDefs
        .filter(variableDef => variableDef.searchable)
        .map(variableDef => variableDef.varDef)
    )
    .filter(value => value !== undefined) as VariableDef[]
  const [variable, setVariable] = useState(variables[0])
  const [variableValue, setVariableValue] = useState('')
  const [variableValueDebounced] = useDebounce(variableValue, 250)
  const [limit, setLimit] = useState(LIMIT)
  const tenantId = useParams().tenantId as string

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['searchVariables', tenantId, limit, variable, variableValueDebounced],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: WithBookmark) => lastPage.bookmark,
    queryFn: async ({ pageParam }) => {
      if (!variableValueDebounced) return { results: [], bookmark: undefined }
      return await searchVariables({
        wfSpecName: spec.id!.name,
        wfSpecMajorVersion: spec.id!.majorVersion,
        wfSpecRevision: spec.id!.revision,
        tenantId,
        limit,
        bookmark: pageParam,
        varName: variable.name,
        value: convertToVariableValue({
          type: getVariableDefType(variable).toLowerCase(),
          value: variableValueDebounced,
        }),
      })
    },
  })
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button>Search By Variable</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Search WfRuns by Variable</DialogTitle>
          <DialogDescription>
            Search for WfRuns by variable name and value. The search is case-sensitive.
          </DialogDescription>
        </DialogHeader>

        <Select
          onValueChange={value => setVariable(variables.find(v => v.name == value) ?? variable)}
          value={variable.name}
        >
          <SelectTrigger>
            <SelectValue placeholder="Select Variable" />
          </SelectTrigger>
          <SelectContent>
            {variables.map(varDef => (
              <SelectItem key={varDef.name} value={varDef.name}>
                {varDef.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Input
          placeholder="Variable Value..."
          onChange={e => {
            setVariableValue(e.target.value)
          }}
        />

        {isPending ? (
          <div className="flex items-center justify-center">
            <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
          </div>
        ) : data?.pages[0].results.length ? (
          data?.pages.map((page, i) => (
            <Fragment key={i}>
              {page.results.map(variableId => (
                <div key={variableId.name}>
                  <LinkWithTenant
                    className="py-2 text-blue-500 hover:underline"
                    href={`/wfRun/${variableId.wfRunId?.id}?threadRunNumber=${variableId.threadRunNumber}`}
                  >
                    {variableId.wfRunId?.id}
                  </LinkWithTenant>
                </div>
              ))}
            </Fragment>
          ))
        ) : (
          <p className="text-center">
            No data. Try another value for <span className="font-bold">{variable.name}</span>
          </p>
        )}
        <SearchFooter
          currentLimit={limit}
          setLimit={setLimit}
          hasNextPage={hasNextPage}
          fetchNextPage={fetchNextPage}
        />
      </DialogContent>
    </Dialog>
  )
}

function convertToVariableValue({ type, value }: { type: string; value: string }): VariableValue {
  switch (type) {
    case 'str':
      return { str: value }
    case 'int':
      return { int: parseInt(value) }
    case 'bool':
      return { bool: value.toLowerCase() === 'true' }
    case 'double':
      return { double: parseFloat(value) }
    default:
      return { str: value }
  }
}
