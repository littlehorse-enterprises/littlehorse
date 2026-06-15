import { StartMultipleThreadsNode as StartMultipleThreadsNodeProto } from 'littlehorse-client/proto'
import { PlusIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { NodeProps } from '.'
import { DiagramNodeDiamond, DiagramNodeShell } from './DiagramNodeChrome'
import { DiagramNodeHandles } from './DiagramNodeHandles'
import { grayNodeTheme } from './nodeThemes'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const Node: FC<NodeProps<'startMultipleThreads', StartMultipleThreadsNodeProto>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList, sourceHandleCount, targetHandleCount } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell id={id} label="Start Multiple" icon={PlusIcon} theme={grayNodeTheme}>
          <div className="relative flex">
            <DiagramNodeDiamond selected={selected} theme={grayNodeTheme} sizeClass="h-8 w-8" innerInsetClass="inset-[1px]">
              <PlusIcon className="h-4 w-4 fill-gray-600" />
            </DiagramNodeDiamond>
            <DiagramNodeHandles
              sourceCount={sourceHandleCount ?? 1}
              targetCount={targetHandleCount ?? 1}
            />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const StartMultipleThreads = memo(Node)
