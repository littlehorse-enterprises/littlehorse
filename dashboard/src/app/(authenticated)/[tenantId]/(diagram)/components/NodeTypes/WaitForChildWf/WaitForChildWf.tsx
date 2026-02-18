import { WaitForChildWfNode } from 'littlehorse-client/proto'
import { Merge, Spool } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const SALMON_BG = '#fecaca'
const SALMON_BORDER = '#fca5a5'
const SALMON_DARK = '#f87171'

const Node: FC<NodeProps<'waitForChildWf', WaitForChildWfNode>> = ({ data }) => {
  const { fade, nodeRunsList } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="flex cursor-pointer items-center">
          <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
          <div className="flex flex-col items-center">
            <div className="relative grid h-10 w-10 place-items-center">
              <div
                className="absolute inset-0"
                style={{
                  clipPath: 'polygon(100% 50%, 50% 100%, 0 50%, 50% 0)',
                  background: SALMON_DARK,
                }}
              />
              <div
                className="absolute inset-[2px]"
                style={{
                  clipPath: 'polygon(100% 50%, 50% 100%, 0 50%, 50% 0)',
                  background: `linear-gradient(135deg, ${SALMON_BG}, ${SALMON_BORDER})`,
                }}
              />
              <Merge
                className="relative z-10 h-5 w-5 shrink-0 stroke-black"
                strokeWidth={1.5}
                style={{ transform: 'rotate(90deg)', marginLeft: 3 }}
              />
              <div
                className="absolute -bottom-1 -right-1 z-10 grid h-4 w-4 place-items-center rounded"
                style={{
                  background: SALMON_BG,
                  border: `1px solid ${SALMON_DARK}`,
                }}
              >
                <Spool className="h-2.5 w-2.5 stroke-black" strokeWidth={1.5} />
              </div>
            </div>
          </div>
          <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const WaitForChildWf = memo(Node)
