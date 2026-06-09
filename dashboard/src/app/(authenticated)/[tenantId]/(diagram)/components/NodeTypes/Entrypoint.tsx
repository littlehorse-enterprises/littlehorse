import { cn } from '@/lib/utils'
import { EntrypointNode as EntrypointNodeProto } from 'littlehorse-client/proto'
import { PlayIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { DiagramNodeShell } from './DiagramNodeChrome'
import { greenNodeTheme } from './nodeThemes'
import { SelectedNode } from './SelectedNode'

const EntrypointNode: FC<NodeProps<'entrypoint', EntrypointNodeProto>> = ({ id, selected }) => {
  return (
    <>
      <SelectedNode />
      <DiagramNodeShell id={id} label="Entry" icon={PlayIcon} theme={greenNodeTheme}>
        <div
          className={cn(
            'relative h-6 w-6 cursor-pointer rounded-xl border-[1px]',
            greenNodeTheme.borderClass,
            greenNodeTheme.bgClass,
            selected && greenNodeTheme.selectedBgClass
          )}
        >
          <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
        </div>
      </DiagramNodeShell>
    </>
  )
}

export const Entrypoint = memo(EntrypointNode)
