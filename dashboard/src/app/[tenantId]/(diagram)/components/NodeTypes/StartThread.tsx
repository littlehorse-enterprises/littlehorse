import { getVariable } from '@/app/utils'
import { PlusIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { useThread } from '../../hooks/useThread'
import { NodeRunsList } from '../NodeRunsList'
import { Fade } from './Fade'
import { NodeDetails } from './NodeDetails'

const Node: FC<NodeProps> = ({ data }) => {
  const { fade, nodeRunsList } = data
  const { setThread } = useThread()
  if (data.startThread === undefined) return
  const variables = Object.entries(data.startThread.variables)
  return (
    <>
      <NodeDetails>
        <div className="flex items-center gap-1 text-nowrap">
          <h3 className="font-bold">StartThread</h3>
          <button
            className="whitespace-nowrap text-blue-500 hover:underline"
            onClick={() => setThread({ name: data.startThread?.threadSpecName || '', number: 0 })}
          >
            {data.startThread?.threadSpecName}
          </button>
        </div>
        {variables.length > 0 && (
          <div className="mt-2">
            <h2 className="font-bold">Variables</h2>
            <ul>
              {variables.map(([name, value]) => (
                <li key={name}>
                  {`{${name}}`} = {getVariable(value)}
                </li>
              ))}
            </ul>
          </div>
        )}
        <NodeRunsList nodeRuns={nodeRunsList} />
      </NodeDetails>

      <Fade fade={fade} status={data.nodeRun?.status}>
        <div className="relative cursor-pointer">
          <div className="ml-1 flex h-6 w-6 rotate-45 items-center justify-center border-[2px] border-gray-500 bg-gray-200">
            <PlusIcon className="h-5 w-5 rotate-45 fill-gray-500" />
          </div>
        </div>
        <Handle type="source" position={Position.Right} className="bg-transparent" />
        <Handle type="target" position={Position.Left} className="bg-transparent" />
      </Fade>
    </>
  )
}

export const StartThread = memo(Node)
