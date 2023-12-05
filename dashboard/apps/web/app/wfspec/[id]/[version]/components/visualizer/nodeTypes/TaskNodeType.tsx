import { Handle, Position } from 'reactflow'
import LabelsUtils from '../LabelsUtils'

export default function TaskNodeType({ data }) {

    return (
        <div className="viznode-canvas">
            <div className="ring">
                <div className={`node tTASK ${(!data.isWfSpecVisualization && !data.nodeHasRun) ? 'opacity50' : ''}`} id={`id${data.nodeName}`} >
                    <img alt='Task' src="/TASK.svg" />
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
