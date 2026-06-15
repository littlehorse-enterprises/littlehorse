import { Node } from 'littlehorse-client/proto'
import { DiamondIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { getNopSourceHandleConfigs } from '../../layout/nopHandles'
import { DiagramNodeDiamond, DiagramNodeShell } from './DiagramNodeChrome'
import { grayNodeTheme } from './nodeThemes'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const sideStyle = (position: Position, offset: number): React.CSSProperties => {
  if (position === Position.Top || position === Position.Bottom) {
    return { left: `${offset * 100}%` }
  }
  return { top: `${offset * 100}%` }
}

const NopNode: FC<NodeProps<'entrypoint', Node>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList } = data
  const nodeRun = nodeRunsList?.[0]
  const sourceHandles = getNopSourceHandleConfigs(data.outgoingEdges ?? [])

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell id={id} label="Nop" icon={DiamondIcon} theme={grayNodeTheme}>
          <div className="relative flex">
            <DiagramNodeDiamond selected={selected} theme={grayNodeTheme} sizeClass="h-8 w-8" innerInsetClass="inset-[1px]" />
            <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
            {sourceHandles.map(config => (
              <Handle
                key={config.id}
                type="source"
                position={config.position}
                id={config.id}
                className="bg-transparent"
                style={sideStyle(config.position, config.offset)}
              />
            ))}
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const Nop = memo(NopNode)
