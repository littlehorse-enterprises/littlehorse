import { Edge } from 'littlehorse-client/proto'
import { Variable } from 'lucide-react'
import { FC } from 'react'
import { Mutation } from './Mutation'

export const Mutations: FC<Pick<Edge, 'variableMutations'>> = ({ variableMutations }) => {
  if (variableMutations.length === 0) return

  return (
    <div className="ml-4 border-l pl-4 pt-2">
      <div className="flex cursor-pointer items-center gap-2">
        <Variable className="h-4 w-4 flex-none text-gray-400" />
        <h5 className="grow text-sm text-gray-400">Variable Mutations</h5>
      </div>
      <div className="ml-2 border-l pl-4 pt-2">
        {variableMutations.map(mutation => (
          <Mutation key={mutation.lhsName} mutation={mutation} />
        ))}
      </div>
    </div>
  )
}
