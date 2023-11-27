import { Handle, Position } from 'reactflow'
import LabelsUtils from '../LabelsUtils'

export default function NopNodeType({ data }) {

    return (
        <div className="viznode-canvas">
            <div className="ring">
                <div className={`node tNOP ${(!data.isWfSpecVisualization && !data.nodeHasRun) ? 'opacity50' : ''}`} id={`id${data.label}`} >
                    <img alt='Nop' src="/NOP.svg" />
                    <div>
                        {LabelsUtils.extractLabel(data.label)}
                    </div>
                </div>
            </div>
            <Handle position={Position.Bottom} type="source" />
            <Handle position={Position.Top} type="target" />
            <Handle id="targetLeft" position={Position.Left} type="target"/>
            <Handle id="targetRight" position={Position.Right} type="target"/>
            <Handle id="sourceLeft" position={Position.Left} type="source"/>
            <Handle id="sourceRight" position={Position.Right} type="source"/>
        </div>
    )
}