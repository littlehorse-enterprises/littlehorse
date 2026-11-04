import { cn } from '@/lib/utils'
import { ExitNode as ExitNodeProto, LHStatus } from 'littlehorse-client/proto'
import { SquareIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { DiagramNodeShell } from './DiagramNodeChrome'
import { Fade } from './Fade'
import { greenNodeTheme, redNodeTheme } from './nodeThemes'
import { SelectedNode } from './SelectedNode'

const ExitNode: FC<NodeProps<'exit', ExitNodeProto>> = ({ id, data, selected }) => {
  const { fade } = data
  const failureDef = data.result?.oneofKind === 'failureDef' ? data.result.failureDef : undefined
  const theme = failureDef ? redNodeTheme : greenNodeTheme

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={failureDef ? LHStatus.EXCEPTION : undefined}>
        <DiagramNodeShell id={id} label="Exit" icon={SquareIcon} theme={theme}>
          <div
            className={cn(
              'relative h-6 w-6 cursor-pointer rounded-xl border-[3px]',
              theme.borderClass,
              theme.bgClass,
              selected && theme.selectedBgClass
            )}
          >
            <Handle type="target" id="target-0" position={Position.Left} className="bg-transparent" />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const Exit = memo(ExitNode)
