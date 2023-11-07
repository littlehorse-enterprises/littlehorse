import { Handle, Position } from 'reactflow'
import LabelsUtils from '../LabelsUtils'

export default function SpawnThreadNodeType({ data }) {

  return (
    <div className="viznode-canvas">
      <div className="ring">
        <div className={`node ${(!data.isWfSpecVisualization && !data.nodeHasRun) ? 'opacity50' : ''}`} id={`id${data.label}`} >
          <img alt='Start Thread' src="/START_THREAD.svg" />
          <div>
            {LabelsUtils.extractLabel(data.label)}
          </div>
        </div>
      </div>
      <Handle position={Position.Bottom} type="source" />
      <Handle position={Position.Top} type="target" />
    </div>
  )
}