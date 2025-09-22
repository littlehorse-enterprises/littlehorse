import { StartThreadNode as StartThreadNodeProto } from 'littlehorse-client/proto'
import { ScanIcon } from 'lucide-react'
import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { VariableAssignment } from '../Components'

export const StartThreadNode: FC<{ node: StartThreadNodeProto }> = ({ node }) => {
  const { setThread } = useDiagram()
  const { threadSpecName, variables } = node

  return (
    <div className="flex max-w-full flex-1 flex-col">
      <small className="text-[0.75em] text-slate-400">StartThread</small>
      <div className="mb-2 flex items-center">
        <p className="flex-grow truncate text-lg font-medium">{threadSpecName}</p>
        <ScanIcon
          className="ml-1 h-4 w-4 cursor-pointer hover:text-slate-600"
          onClick={() => {
            setThread({ name: threadSpecName, number: 0 })
          }}
        />
      </div>
      {variables && Object.keys(variables).length > 0 && (
        <div className="flex flex-col gap-2">
          <small className="text-[0.75em] text-slate-400">Inputs</small>
          {Object.entries(variables).map(([key, value]) => (
            <div key={JSON.stringify(value)} className="flex">
              <span className="bg-gray-200 px-2 truncate flex-1 font-mono">{key}</span>
              <VariableAssignment variableAssigment={value} />
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
