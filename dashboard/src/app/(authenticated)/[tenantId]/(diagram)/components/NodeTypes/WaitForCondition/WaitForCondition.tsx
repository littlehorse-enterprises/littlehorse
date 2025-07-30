import { getVariable } from '@/app/utils'
import { WaitForConditionNode } from 'littlehorse-client/proto'
import { CircleEqualIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'

import { Fade } from '../Fade'
import { NodeDetails } from '../NodeDetails'

import { NodeRunsList } from '../../NodeRunsList'

import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'
import { Condition } from './Condition'
const Node: FC<NodeProps<'waitForCondition', WaitForConditionNode>> = ({ data }) => {
  const { fade, nodeNeedsToBeHighlighted, condition } = data
  if (!condition) return null
  return (
    <>
      <NodeDetails nodeRunList={data.nodeRunsList}>
        <DiagramDataGroup label="WaitForCondition">
          <div>
            <div className="flex items-center gap-1 text-nowrap">
              <h3 className="mb-2 font-bold">WaitForCondition</h3>
            </div>
            {
              <div className="flex flex-col gap-1 text-nowrap">
                <div className="flex flex-col">
                  <div className="font-semibold">Left Side Variable:</div>
                  <div className="w-fit bg-gray-200 px-1">{condition.left ? getVariable(condition.left) : 'N/A'}</div>
                </div>
                <div className="flex flex-col justify-center">
                  <div className="font-semibold">Comparator:</div>
                  <div className="w-fit bg-gray-200 px-1">{getComparatorSymbol(condition.comparator)}</div>
                </div>
                <div className="flex flex-col">
                  <div className="font-semibold">Right Side Value:</div>
                  <div className="w-fit bg-gray-200 px-1">{condition.right ? getVariable(condition.right) : 'N/A'}</div>
                </div>
              </div>
            }
          </div>
          <NodeRunsList nodeRuns={data.nodeRunsList} />
        </DiagramDataGroup>
      </NodeDetails>
      <Fade fade={fade} status={data.nodeRunsList[data.nodeRunsList.length - 1].status}>
        <div className="relative cursor-pointer items-center justify-center text-xs">
          <div
            className={
              'items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-[1px] text-xs' +
              (nodeNeedsToBeHighlighted ? ' shadow-lg shadow-blue-500' : '')
            }
          >
            <div className="items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-2 text-xs">
              <CircleEqualIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
            </div>
          </div>
          <Handle type="source" position={Position.Right} className="h-2 w-2 bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
          <div className="absolute flex w-full items-center justify-center whitespace-nowrap text-center"></div>
        </div>
      </Fade>
      <Condition {...condition} />
    </>
  )
}

export const WaitForCondition = memo(Node)
