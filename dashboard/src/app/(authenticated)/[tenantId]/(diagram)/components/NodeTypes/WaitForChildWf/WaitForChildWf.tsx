import { WaitForChildWfNode } from 'littlehorse-client/proto'
import { Merge, Workflow } from 'lucide-react'
import { FC, memo } from 'react'
import { NodeProps } from '..'
import { DiagramNodeDiamond, DiagramNodeShell } from '../DiagramNodeChrome'
import { DiagramNodeHandles } from '../DiagramNodeHandles'
import { roseNodeTheme } from '../nodeThemes'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'waitForChildWf', WaitForChildWfNode>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList, sourceHandleCount, targetHandleCount } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell id={id} label="Wait For Child Wf" icon={Workflow} theme={roseNodeTheme}>
          <div className="relative flex cursor-pointer items-center">
            <DiagramNodeDiamond selected={selected} theme={roseNodeTheme}>
              <Merge
                className="h-5 w-5 shrink-0 stroke-rose-950"
                strokeWidth={1.5}
                style={{ transform: 'rotate(90deg)', marginLeft: 3 }}
              />
              <div className="absolute -bottom-1 -right-1 grid h-4 w-4 place-items-center rounded border border-rose-500 bg-rose-200">
                <Workflow className="h-2.5 w-2.5 stroke-rose-950" strokeWidth={1.5} />
              </div>
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

export const WaitForChildWf = memo(Node)
