import { Handle, Position } from 'reactflow'
import LabelsUtils from '../LabelsUtils'

export default function WaitForThreadsNodeType({ data }) {

  return (
    <div className="viznode-canvas">
      <div className="ring">
        <div className={`node ${(!data.isWfSpecVisualization && !data.nodeHasRun) ? 'opacity50' : ''}`} id={`id${data.label}`} >
          <img alt='Wait for Threads' src="/WAIT_FOR_THREADS.svg" />
          {Boolean(data.failureHandlers?.length) && <img alt="Failure Handler Logo" src="/EXCEPTION.svg" />}
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