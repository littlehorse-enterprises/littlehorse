import { Node } from 'littlehorse-client/proto'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

type HandleConfig = {
  id: string
  position: Position
  style?: React.CSSProperties
}

const NopNode: FC<NodeProps<'entrypoint', Node>> = props => {
  const { data } = props
  const { fade, nodeRunsList } = data
  const nodeRun = nodeRunsList?.[0]

  // Helper to create a handle element
  const createHandle = (config: HandleConfig, type: 'source' | 'target') => (
    <Handle
      key={config.id}
      type={type}
      position={config.position}
      id={config.id}
      className="bg-transparent"
      style={config.style}
    />
  )

  // Helper to distribute handles evenly along a position
  const distributeHandles = (
    count: number,
    position: Position,
    startIndex: number,
    axis: 'left' | 'top' = 'left'
  ): HandleConfig[] => {
    return Array.from({ length: count }, (_, i) => ({
      id: `source-${startIndex + i}`,
      position,
      style: { [axis]: `${((i + 1) * 100) / (count + 1)}%` },
    }))
  }

  const generateSourceHandles = () => {
    const edges = data.outgoingEdges || []
    const cycleEdges = edges.filter(edge => edge.sinkNodeName.startsWith('cycle-'))
    const regularEdges = edges.filter(edge => !edge.sinkNodeName.startsWith('cycle-'))
    const hasCycle = cycleEdges.length > 0
    const regularCount = regularEdges.length
    const totalCount = edges.length

    let configs: HandleConfig[] = []

    if (hasCycle) {
      // With cycle: handle regular edges, then add cycle at bottom
      if (regularCount === 1) {
        configs = [
          { id: 'source-0', position: Position.Right },
          { id: 'source-1', position: Position.Bottom },
        ]
      } else if (regularCount === 2) {
        configs = [
          { id: 'source-0', position: Position.Top, style: { left: '33%' } },
          { id: 'source-1', position: Position.Top, style: { left: '66%' } },
          { id: 'source-2', position: Position.Bottom },
        ]
      } else {
        // 3+ regular edges
        const isOdd = regularCount % 2 !== 0
        if (isOdd) {
          const halfCount = Math.floor(regularCount / 2)
          configs = [
            ...distributeHandles(halfCount, Position.Top, 0),
            { id: `source-${halfCount}`, position: Position.Right },
            { id: `source-${regularCount}`, position: Position.Bottom },
          ]
        } else {
          const halfCount = regularCount / 2
          configs = [
            ...distributeHandles(halfCount, Position.Top, 0),
            ...distributeHandles(halfCount, Position.Right, halfCount, 'top'),
            { id: `source-${regularCount}`, position: Position.Bottom },
          ]
        }
      }
    } else {
      // No cycle: standard distribution
      if (totalCount === 1) {
        configs = [{ id: 'source-0', position: Position.Right }]
      } else if (totalCount === 2) {
        configs = [
          { id: 'source-0', position: Position.Top },
          { id: 'source-1', position: Position.Bottom },
        ]
      } else if (totalCount === 3) {
        configs = [
          { id: 'source-0', position: Position.Top },
          { id: 'source-1', position: Position.Right },
          { id: 'source-2', position: Position.Bottom },
        ]
      } else {
        // 4+ edges
        const isOdd = totalCount % 2 !== 0
        if (isOdd) {
          const halfCount = Math.floor(totalCount / 2)
          configs = [
            ...distributeHandles(halfCount, Position.Top, 0),
            { id: `source-${halfCount}`, position: Position.Right },
            ...distributeHandles(halfCount, Position.Bottom, halfCount + 1),
          ]
        } else {
          const halfCount = totalCount / 2
          configs = [
            ...distributeHandles(halfCount, Position.Top, 0),
            ...distributeHandles(halfCount, Position.Bottom, halfCount),
          ]
        }
      }
    }

    return <>{configs.map(config => createHandle(config, 'source'))}</>
  }

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="flex">
          <div className="cursor-pointer1 relative grid h-8 w-8 place-items-center">
            <div className="absolute inset-0 bg-gray-400 [clip-path:polygon(50%_0,100%_50%,50%_100%,0_50%)]"></div>
            <div className="absolute inset-[1px] bg-gray-200 [clip-path:polygon(50%_0,100%_50%,50%_100%,0_50%)]"></div>
          </div>
          <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
          {generateSourceHandles()}
        </div>
      </Fade>
    </>
  )
}

export const Nop = memo(NopNode)
