import { getVariable } from '@/app/utils'
import { PlusIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { useThread } from '../../hooks/useThread'
import { Fade } from './Fade'
import { NodeDetails } from './NodeDetails'
import { DiagramDataGroup } from './DataGroupComponents/DiagramDataGroup'
const Node: FC<NodeProps> = ({ data }) => {
  const { fade } = data
  const { setThread } = useThread()
  if (data.startMultipleThreads === undefined) return
  const variables = Object.entries(data.startMultipleThreads.variables)
  return (
    <>
      <NodeDetails nodeRunList={data.nodeRunsList}>
        <DiagramDataGroup label="StartMultipleThreads">
          <div className="flex items-center gap-1 text-nowrap">
            {data.nodeRun === undefined ? (
              <button
                className="block whitespace-nowrap text-blue-500 hover:underline"
                onClick={() => setThread({ name: data.startMultipleThreads?.threadSpecName || '', number: 0 })}
              >
                {data.startMultipleThreads?.threadSpecName}
              </button>
            ) : (
              <div>{data.startMultipleThreads?.threadSpecName}</div>
            )}
          </div>
          <div className="">
            <span className="font-bold">Iterable:</span> {getVariable(data.startMultipleThreads.iterable)}
          </div>
          {variables.length > 0 && (
            <div className="mt-2">
              <h2 className="font-bold">Variables</h2>
              <ul>
                {variables.map(([name, value]) => (
                  <li key={name}>
                    {`{${name}}`} {getVariable(value)}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {data.nodeRun && (
            <div className="mt-2">
              <h2 className="font-bold">Thread Runs</h2>
              <ul>
                {data.nodeRun.startMultipleThreads?.childThreadIds.map(number => (
                  <li
                    className="cursor-pointer text-blue-500 hover:underline"
                    onClick={() => {
                      setThread({ name: data.startMultipleThreads?.threadSpecName || '', number })
                    }}
                    key={number}
                  >
                    {data.nodeRun?.startMultipleThreads?.threadSpecName}-{number}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </DiagramDataGroup>
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

export const StartMultipleThreads = memo(Node)
