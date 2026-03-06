import { RunChildWfNode } from 'littlehorse-client/proto'
import { TrendingUpDown, Workflow } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const MINT_BG = '#a7f3d0'
const MINT_BORDER = '#6ee7b7'
const MINT_DARK = '#34d399'

const Node: FC<NodeProps<'runChildWf', RunChildWfNode>> = ({ data }) => {
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
                  clipPath: 'polygon(50% 0, 100% 50%, 50% 100%, 0 50%)',
                  background: MINT_DARK,
                }}
              />
              <div
                className="absolute inset-[2px]"
                style={{
                  clipPath: 'polygon(50% 0, 100% 50%, 50% 100%, 0 50%)',
                  background: `linear-gradient(135deg, ${MINT_BG}, ${MINT_BORDER})`,
                }}
              />
              <TrendingUpDown className="relative z-10 h-5 w-5 shrink-0 stroke-black" strokeWidth={1.5} />
              <div
                className="absolute -bottom-1 -right-1 z-10 grid h-4 w-4 place-items-center rounded"
                style={{
                  background: MINT_BG,
                  border: `1px solid ${MINT_DARK}`,
                }}
              >
                <Workflow className="h-2.5 w-2.5 stroke-black" strokeWidth={1.5} />
              </div>
            </div>
          </div>
          <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const RunChildWf = memo(Node)
