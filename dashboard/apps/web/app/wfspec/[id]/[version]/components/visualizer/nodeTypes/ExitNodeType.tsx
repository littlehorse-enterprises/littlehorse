import { Handle, Position } from 'reactflow'
import LabelsUtils from '../LabelsUtils'

export default function ExitNodeType({ data }) {

    return (
        <div className="viznode-canvas">
            <div className="ring">
                <div className={`node tEXIT ${(!data.isWfSpecVisualization && !data.nodeHasRun) ? 'opacity50' : ''}`} id={`id${data.label}`} >
                    <img alt='Exit' src="/EXIT.svg" />
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
