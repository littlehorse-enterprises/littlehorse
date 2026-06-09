import { ExternalEventNode as ExternalEventProto } from 'littlehorse-client/proto'
import { MailOpenIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { NodeProps } from '..'
import { DiagramNodeCircle, DiagramNodeShell } from '../DiagramNodeChrome'
import { DiagramNodeHandles } from '../DiagramNodeHandles'
import { blueNodeTheme } from '../nodeThemes'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'externalEvent', ExternalEventProto>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList, externalEventDefId, sourceHandleCount, targetHandleCount } = data
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

export const ExternalEvent = memo(Node)
