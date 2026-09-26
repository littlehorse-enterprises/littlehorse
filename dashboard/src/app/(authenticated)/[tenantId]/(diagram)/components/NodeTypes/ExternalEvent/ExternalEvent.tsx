import { ExternalEventNode as ExternalEventProto } from 'littlehorse-client/proto'
import { MailOpenIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { DiagramNodeCircle, DiagramNodeShell } from '../DiagramNodeChrome'
import { Fade } from '../Fade'
import { blueNodeTheme } from '../nodeThemes'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'externalEvent', ExternalEventProto>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList, externalEventDefId } = data
  const nodeRun = nodeRunsList?.[0]
  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell
          id={id}
          label="External Event"
          icon={MailOpenIcon}
          theme={blueNodeTheme}
          subtitle={externalEventDefId?.name}
        >
          <div className="relative">
            <DiagramNodeCircle
              selected={selected}
              theme={blueNodeTheme}
              icon={MailOpenIcon}
              iconClass="fill-none stroke-blue-600"
            />
            <Handle type="source" id="source-0" position={Position.Right} className="bg-transparent" />
            <Handle type="target" id="target-0" position={Position.Left} className="bg-transparent" />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const ExternalEvent = memo(Node)
